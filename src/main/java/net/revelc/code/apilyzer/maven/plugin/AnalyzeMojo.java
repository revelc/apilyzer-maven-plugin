/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package net.revelc.code.apilyzer.maven.plugin;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Analyzes declared public API.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY,
    requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class AnalyzeMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /**
   * The classes to include in your public API definition. These classes will be included in the
   * analysis. The format is java regular expressions. Any classes on the classpath whose
   * fully-qualified class name matches any of these regular expressions, and does not match any of
   * those in the excludes, will be included for analysis.
   *
   * <p>
   * Matching is done with the regular expression anchored to the beginning and end of the
   * fully-qualified class name, so there is no need to prefix with {@code ^} or suffix with
   * {@code $}. To match a partial class name, you will need to add {@code .*} as a prefix and/or
   * suffix.
   *
   * <p>
   * If an include pattern matches a class, then that class along with all of its public or
   * protected inner classes are added to the public API definition. If you do not wish for a
   * particular inner class to be in the public API then you can add a more specific exclusion for
   * it. For example could include {@code com.foo.C} and exclude {@code com.foo.C$I1} if the inner
   * class {@code C$I1} ends up in the API when its not wanted.
   *
   * <p>
   * Example:
   *
   * <pre>
   * &lt;configuration&gt;
   * &nbsp;&nbsp;...
   * &nbsp;&nbsp;&lt;includes&gt;
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;include&gt;org[.]apache[.].*&lt;/include&gt;
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;include&gt;com[.]example[.]myproject[.].*&lt;/include&gt;
   * &nbsp;&nbsp;&lt;/includes&gt;
   * &nbsp;&nbsp;...
   * &lt;/configuration&gt;
   * </pre>
   *
   * @since 1.0.0
   */
  @Parameter(alias = "includes", required = true)
  private List<String> includes;

  /**
   * The classes to exclude from your public API definition, which may have otherwise matched your
   * includes. The format is the same as {@link #includes}.
   *
   * <p>
   * Example:
   *
   * <pre>
   * &lt;configuration&gt;
   * &nbsp;&nbsp;...
   * &nbsp;&nbsp;&lt;excludes&gt;
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;exclude&gt;.*[.]impl[.].*&lt;/exclude&gt;
   * &nbsp;&nbsp;&lt;/excludes&gt;
   * &nbsp;&nbsp;...
   * &lt;/configuration&gt;
   * </pre>
   *
   * @since 1.0.0
   */
  @Parameter(alias = "excludes")
  private List<String> excludes;

  /**
   * The additional classes, which are allowed to be referenced in your public API, but are not,
   * themselves, declared as part of your API. For example, these may be objects from a standard
   * library, which you utilize as parameters in your API methods.
   *
   * <p>
   * These follow the same format as {@link #includes} and {@link #excludes}.
   *
   * <p>
   * Example:
   *
   * <pre>
   * &lt;configuration&gt;
   * &nbsp;&nbsp;...
   * &nbsp;&nbsp;&lt;allows&gt;
   * &nbsp;&nbsp;&nbsp;&nbsp;&lt;allow&gt;com[.]google[.]common[.].*&lt;/allow&gt;
   * &nbsp;&nbsp;&lt;/allows&gt;
   * &nbsp;&nbsp;...
   * &lt;/configuration&gt;
   * </pre>
   *
   * @since 1.0.0
   */
  @Parameter(alias = "allows")
  private List<String> allows;

  /**
   * Allows skipping execution of this plugin. This may be useful for testing, or if you find that
   * analysis is taking too long.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "skip", property = "apilyzer.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Controls whether API items marked with the {@link Deprecated} annotation are ignored. By
   * default, these are ignored (excluded from analysis). One useful way to make use of this plugin
   * is to use it to help identify API methods which should be deprecated (and eventually removed)
   * because they are using unexpected and problematic classes. Once found, they can be deprecated
   * and excluded from future analysis.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "ignoreDeprecated", property = "apilyzer.ignoreDeprecated",
      defaultValue = "true")
  private boolean ignoreDeprecated;

  /**
   * The absolute path for the report file.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "outputFile", property = "apilyzer.outputFile",
      defaultValue = "${project.build.directory}/apilyzer.txt")
  private String outputFile;

  /**
   * Allows ignoring the problems found. If this is set to true, then the report will still be
   * created, but the plugin will not cause the build to fail.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "ignoreProblems", property = "apilyzer.ignoreProblems", defaultValue = "false")
  private boolean ignoreProblems;

  /**
   * This option enables including classes in your public API definition based on class level
   * annotations. This option takes one or more regular expression. Annotations are discovered using
   * reflection, so annotations scoped to compile may not be seen. For each regular expression
   * {@link String#matches(String)} is called on the output of {@link Annotation#toString()}. If any
   * annotation matches any regular expression and it does not match any exclusion, then its
   * included as an API type.
   * 
   * <p>
   * This section of the configuration is ORed with the {@code <includes>} section. So if a class
   * matches something in either section (and its not excluded), then its included in the API
   * definition.
   * 
   * <p>
   * This section has the same behavior with inner classes as {@code <includes>}.
   * 
   * <p>
   * The following example shows using annotations to analyze Hadoop's API. When trying to analyze a
   * classes annotations, the class must be loaded. In the example below some classes could not be
   * loaded because their dependencies were not on the classpath. However these classes were not in
   * a package we cared about. Excluding classes not in the {@code org.apache.hadoop} package fixed
   * this problem.
   * 
   * <pre>
   * {@code
   * <configuration>
   *  <!--Look for Public+Stable APIs using Types that are not Public+Stable-->
   *  <includes>
   *    <!-- This class has no annotation, but seems like it sould be in API -->
   *    <include>org[.]apache[.]hadoop[.]fs[.]RemoteIterator</include>
   *  </includes>
   *  <includeAnnotations>
   *    <include>
   *      [@]org[.]apache[.]hadoop[.]classification[.]InterfaceAudience[$]Public.*
   *    </include>
   *  </includeAnnotations>
   *  <excludeAnnotations>
   *    <exclude>
   *      [@]org[.]apache[.]hadoop[.]classification[.]InterfaceStability[$]Evolving.*
   *    </exclude>
   *    <exclude>
   *      [@]org[.]apache[.]hadoop[.]classification[.]InterfaceStability[$]Unstable.*
   *    </exclude>
   *  </excludeAnnotations>
   *  <excludes>
   *     <!-- Exclude all classes not in the org.apache.hadoop package -->
   *     <exclude>(?!org[.]apache[.]hadoop.*).*</exclude>      
   *  </excludes>
   * </configuration>
   * }
   * </pre>
   * 
   * @since 1.1.0
   */
  @Parameter(alias = "includeAnnotations")
  private List<String> includeAnnotations = Collections.emptyList();

  /**
   * Exclude classes from public API definition using annotation.
   * 
   * @see AnalyzeMojo#includeAnnotations
   * @since 1.1.0
   */
  @Parameter(alias = "excludeAnnotations")
  private List<String> excludeAnnotations = Collections.emptyList();

  private static final String FORMAT = "  %-20s %-60s %-35s %s\n";

  @Override
  public void execute() throws MojoFailureException, MojoExecutionException {

    AtomicLong counter = new AtomicLong(0);

    if (skip) {
      getLog().info("APILyzer execution skipped");
      return;
    }

    ClassPath classPath;
    try {
      classPath = getClassPath();
    } catch (IOException | DependencyResolutionRequiredException | IllegalArgumentException e) {
      throw new MojoExecutionException("Error resolving project classpath", e);
    }

    try (PrintStream out = new PrintStream(new File(outputFile))) {

      out.println("Includes: " + includes);
      out.println("IncludeAnnotations: " + includeAnnotations);
      out.println("ExcludesAnnotations: " + excludeAnnotations);
      out.println("Excludes: " + excludes);
      out.println("Allowed: " + allows);

      List<Class<?>> publicApiClasses = new ArrayList<Class<?>>();
      TreeSet<String> publicSet = new TreeSet<>();
      buildPublicSet(classPath, publicApiClasses, publicSet);

      out.println();
      out.println("Public API:");
      for (String item : publicSet) {
        out.println("  " + item);
      }

      out.println();
      out.println("Problems : ");

      out.println();
      out.printf(FORMAT, "CONTEXT", "TYPE", "FIELD/METHOD", "NON-PUBLIC REFERENCE");

      out.println();
      // look for public API methods/fields/subclasses that use classes not in public API
      for (Class<?> clazz : publicApiClasses) {
        checkClass(clazz, publicSet, out, counter);
      }

      out.println();
      out.println("Total : " + counter.get());

      String msg =
          "APILyzer found " + counter.get() + " problem" + (counter.get() == 1 ? "" : "s") + ".";
      msg += " See " + outputFile + " for details.";
      if (counter.get() < 0) {
        throw new AssertionError("Inconceivable!");
      } else if (counter.get() == 0) {
        getLog().info(msg);
      } else if (counter.get() > 0 && ignoreProblems) {
        getLog().warn(msg);
      } else {
        getLog().error(msg);
        throw new MojoFailureException(msg);
      }

    } catch (FileNotFoundException e) {
      throw new MojoExecutionException("Bad configuration: cannot create specified outputFile", e);
    }

  }

  private static enum ProblemType {
    INNER_CLASS, METHOD_PARAM, METHOD_RETURN, FIELD, CTOR_PARAM
  }

  private ClassPath getClassPath() throws DependencyResolutionRequiredException, IOException {
    ClassLoader cl;
    List<URL> urls =
        Lists.transform(project.getCompileClasspathElements(), new Function<String, URL>() {
          @Override
          public URL apply(String input) {
            try {
              return new File(input).toURI().toURL();
            } catch (MalformedURLException e) {
              throw new IllegalArgumentException("Unable to convert string (" + input + ") to URL",
                  e);
            }
          }
        });
    cl = new URLClassLoader(urls.toArray(new URL[0]), null);
    return ClassPath.from(cl);
  }

  private Annotation[] getAnnotations(ClassInfo classInfo) {
    if (classInfo.getName().startsWith("com.sun") || classInfo.getName().startsWith("java.")) {
      // was getting class not found exceptions when trying to get annotations for com.sun class...
      return new Annotation[0];
    }

    return getAnnotations(classInfo.load());
  }

  private Annotation[] getAnnotations(Class<?> clazz) {
    return clazz.getDeclaredAnnotations();
  }

  /**
   * Builds the set of Types that are in the public API.
   */
  private void buildPublicSet(ClassPath classPath, List<Class<?>> publicApiClasses,
      TreeSet<String> publicSet) {
    classLoop: for (ClassInfo classInfo : classPath.getAllClasses()) {
      // TODO handle empty includes case; maybe?

      // Do this check before possibly attempting any annotation checks as these require class
      // loading. If the class is excluded by a pattern, then no need to load class.
      if (patternExcludes(classInfo)) {
        continue;
      }

      Annotation[] annotations;
      if (includeAnnotations.size() > 0 || excludeAnnotations.size() > 0) {
        annotations = getAnnotations(classInfo);
      } else {
        annotations = new Annotation[0];
      }


      for (String pattern : includeAnnotations) {
        for (Annotation annotation : annotations) {
          if (annotation.toString().matches(pattern)) {
            if (!annotationExcludes(annotations)) {
              addPublicApiType(publicApiClasses, publicSet, classInfo);
            }
            continue classLoop;
          }
        }
      }


      for (String pattern : includes) {
        if (classInfo.getName().matches(pattern)) {
          if (!annotationExcludes(annotations)) {
            addPublicApiType(publicApiClasses, publicSet, classInfo);
          }
          break;
        }

      }
    }
  }

  private void addPublicApiType(List<Class<?>> publicApiClasses, TreeSet<String> publicSet,
      ClassInfo classInfo) {
    Class<?> clazz = classInfo.load();
    if (isPublicOrProtected(clazz)) {
      publicApiClasses.add(clazz);
      publicSet.add(clazz.getName());

      // for top level classes, add their inner classes
      if (clazz.getEnclosingClass() == null) {
        addPublicInnerClasses(publicApiClasses, publicSet, clazz);
      }
    }
  }

  private boolean patternExcludes(ClassInfo classInfo) {
    for (String excludePattern : excludes) {
      if (classInfo.getName().matches(excludePattern)) {
        return true;
      }
    }

    return false;
  }

  private boolean patternExcludes(Class<?> clazz) {
    for (String excludePattern : excludes) {
      if (clazz.getName().matches(excludePattern)) {
        return true;
      }
    }

    return false;
  }

  private boolean annotationExcludes(Annotation[] annotations) {
    for (String pattern : excludeAnnotations) {
      for (Annotation annotation : annotations) {
        if (annotation.toString().matches(pattern)) {
          return true;
        }
      }
    }

    return false;
  }

  private boolean annotationExcludes(Class<?> clazz) {
    if (excludeAnnotations.size() == 0) {
      return false;
    }

    Annotation[] annotations = getAnnotations(clazz);

    for (String pattern : excludeAnnotations) {
      for (Annotation annotation : annotations) {
        if (annotation.toString().matches(pattern)) {
          return true;
        }
      }
    }

    return false;
  }

  private void addPublicInnerClasses(List<Class<?>> publicApiClasses, TreeSet<String> publicSet,
      Class<?> clazz) {
    Class<?>[] innerClasses = clazz.getDeclaredClasses();
    for (Class<?> ic : innerClasses) {
      // If a class is in the Public API then all of its public inner class are also considered
      // to be in the public API unless explicitly excluded.
      if (isPublicOrProtected(ic) && !publicApiClasses.contains(ic) && !annotationExcludes(ic)
          && !patternExcludes(ic)) {
        publicApiClasses.add(ic);
        publicSet.add(ic.getName());

        addPublicInnerClasses(publicApiClasses, publicSet, ic);
      }
    }
  }

  private boolean isOk(Set<String> publicSet, Class<?> clazz) {

    while (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }

    if (clazz.isPrimitive()) {
      return true;
    }

    String fqName = clazz.getName();

    if (publicSet.contains(fqName)) {
      return true;
    }

    // TODO make default allows configurable
    if (fqName.startsWith("java.")) {
      return true;
    }

    if (allows != null) {
      for (String allowed : allows) {
        if (fqName.matches(allowed)) {
          return true;
        }
      }
    }

    return false;
  }

  // get public and protected fields
  private List<Field> getFields(Class<?> clazz) {
    ArrayList<Field> fields = new ArrayList<Field>(Arrays.asList(clazz.getFields()));

    // TODO need to get superlclasses protected fields, deduping on name
    for (Field f : clazz.getDeclaredFields()) {
      if ((f.getModifiers() & Modifier.PROTECTED) != 0) {
        fields.add(f);
      }
    }

    return fields;
  }

  // get public and protected methods
  private List<Method> getMethods(Class<?> clazz) {
    ArrayList<Method> methods = new ArrayList<Method>(Arrays.asList(clazz.getMethods()));

    // TODO need to get superlclasses protected methods, deduping on signature
    for (Method m : clazz.getDeclaredMethods()) {
      if ((m.getModifiers() & Modifier.PROTECTED) != 0) {
        methods.add(m);
      }
    }

    return methods;
  }

  private List<Class<?>> getInnerClasses(Class<?> clazz) {
    ArrayList<Class<?>> classes = new ArrayList<Class<?>>(Arrays.asList(clazz.getClasses()));

    // TODO need to get superclasses' protected classes, deduping on name
    for (Class<?> c : clazz.getDeclaredClasses()) {
      if ((c.getModifiers() & Modifier.PROTECTED) != 0) {
        classes.add(c);
      }
    }

    return classes;
  }

  private boolean checkClass(Class<?> clazz, Set<String> publicSet, PrintStream out,
      AtomicLong counter) {

    boolean ok = true;

    // TODO make configurable
    if (ignoreDeprecated && clazz.isAnnotationPresent(Deprecated.class)) {
      return true;
    }

    // TODO check generic type parameters

    for (Field field : getFields(clazz)) {

      if (ignoreDeprecated && field.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      if (!field.getDeclaringClass().getName().equals(clazz.getName())
          && isOk(publicSet, field.getDeclaringClass())) {
        continue;
      }

      if (!isOk(publicSet, field.getType())) {
        problem(out, counter, ProblemType.FIELD, clazz, field.getName(), field.getType().getName());
        ok = false;
      }
    }

    Constructor<?>[] constructors = clazz.getConstructors();
    for (Constructor<?> constructor : constructors) {

      if (constructor.isSynthetic()) {
        continue;
      }

      if (ignoreDeprecated && constructor.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      Class<?>[] params = constructor.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(publicSet, param)) {
          problem(out, counter, ProblemType.CTOR_PARAM, clazz, "(...)", param.getName());
          ok = false;
        }
      }
    }

    for (Method method : getMethods(clazz)) {

      if (method.isSynthetic() || method.isBridge()) {
        continue;
      }

      if (ignoreDeprecated && method.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      if (!method.getDeclaringClass().getName().equals(clazz.getName())
          && isOk(publicSet, method.getDeclaringClass())) {
        continue;
      }

      if (!isOk(publicSet, method.getReturnType())) {
        problem(out, counter, ProblemType.METHOD_RETURN, clazz, method.getName() + "(...)",
            method.getReturnType().getName());
        ok = false;
      }

      Class<?>[] params = method.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(publicSet, param)) {
          problem(out, counter, ProblemType.METHOD_PARAM, clazz, method.getName() + "(...)",
              param.getName());
          ok = false;
        }
      }
    }

    for (Class<?> class1 : getInnerClasses(clazz)) {

      if (ignoreDeprecated && class1.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      if (class1.equals(clazz)) {
        // what? see org.apache.hadoop.fs.Options$CreateOpts source in hadoop 2.7.1
        continue;
      }

      if (patternExcludes(class1) || annotationExcludes(class1)) {
        // this inner class is explicitly excluded from API so do not check it
        continue;
      }

      if (!isOk(publicSet, class1) && !checkClass(class1, publicSet, out, counter)) {
        problem(out, counter, ProblemType.INNER_CLASS, clazz, "N/A", class1.getName());
        ok = false;
      }
    }

    return ok;
  }

  private boolean isPublicOrProtected(Class<?> clazz) {
    return (clazz.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0;
  }

  private void problem(PrintStream out, AtomicLong counter, ProblemType type, Class<?> clazz,
      String member, String problemRef) {
    counter.incrementAndGet();
    out.printf(FORMAT, type, clazz.getName(), member, problemRef);
  }
}
