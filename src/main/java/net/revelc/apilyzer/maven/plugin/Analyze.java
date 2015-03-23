/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.revelc.apilyzer.maven.plugin;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY,
    requiresDependencyResolution = ResolutionScope.COMPILE)
public class Analyze extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  @Parameter(required = true, readonly = true)
  private List<String> includes;

  @Parameter(readonly = true)
  private List<String> excludes;

  @Parameter(readonly = true)
  private List<String> allows;

  @Parameter(defaultValue = "false", property = "apilyzer.skip", readonly = true)
  private String skip;

  @Parameter(defaultValue = "true", property = "apilyzer.ignoreDeprecated", readonly = true)
  private String ignoreDeprecated;

  @Parameter(defaultValue = "${project.build.directory}/apilyzer.txt",
      property = "apilyzer.outputFile", readonly = true)
  private String outputFile;

  private static final String FORMAT = "%-20s %-60s %-35s %s\n";

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

  private void checkClass(Class<?> clazz, Set<String> publicSet, PrintStream out) {

    // TODO make configurable
    if (clazz.isAnnotationPresent(Deprecated.class)) {
      return;
    }

    // TODO check superclasses/interfaces

    // TODO check generic type parameters

    Field[] fields = clazz.getDeclaredFields();
    for (Field field : fields) {

      if (!isPublicOrProtected(field)) {
        continue;
      }

      if (field.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      if (!isOk(publicSet, field.getType())) {
        out.printf(FORMAT, "Field", clazz.getName(), field.getName(), field.getType().getName());
      }
    }

    Constructor<?>[] constructors = clazz.getConstructors();
    for (Constructor<?> constructor : constructors) {
      if (constructor.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      Class<?>[] params = constructor.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(publicSet, param)) {
          out.printf(FORMAT, "Constructor param", clazz.getName(), "(...)", param.getName());
        }
      }
    }

    Method[] methods = clazz.getDeclaredMethods();
    for (Method method : methods) {

      if (!isPublicOrProtected(method)) {
        continue;
      }

      if (method.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      if (!isOk(publicSet, method.getReturnType())) {
        out.printf(FORMAT, "Method return", clazz.getName(), method.getName() + "(...)", method
            .getReturnType().getName());
      }

      Class<?>[] params = method.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(publicSet, param)) {
          out.printf(FORMAT, "Method param", clazz.getName(), method.getName() + "(...)",
              param.getName());
        }
      }
    }

    Class<?>[] classes = clazz.getDeclaredClasses();
    for (Class<?> class1 : classes) {
      if (!isPublicOrProtected(class1)) {
        continue;
      }

      // TODO recurse; actually, this is a bit redundant

      if (class1.isAnnotationPresent(Deprecated.class)) {
        continue;
      }
      if (!isOk(publicSet, class1)) {
        out.printf(FORMAT, "Public class", clazz.getName(), "N/A", class1.getName());
      }
    }
  }

  @Override
  public void execute() throws MojoFailureException, MojoExecutionException {

    if (!skip.equalsIgnoreCase("false")) {
      getLog().info("APILyzer execution skipped");
      return;
    }

    try (PrintStream out = new PrintStream(new File(outputFile))) {

      out.println("Includes: " + includes);
      out.println("Excludes: " + excludes);
      out.println("Allowed: " + allows);

      ClassPath cp;
      ClassLoader cl;
      try {
        List<URL> urls =
            Lists.transform(project.getCompileClasspathElements(), new Function<String, URL>() {
              @Override
              public URL apply(String input) {
                try {
                  return new File(input).toURI().toURL();
                } catch (MalformedURLException e) {
                  throw new IllegalArgumentException("Unable to convert string (" + input
                      + ") to URL", e);
                }
              }
            });
        cl = new URLClassLoader(urls.toArray(new URL[0]));
      } catch (DependencyResolutionRequiredException e) {
        throw new MojoFailureException("Unable to resolve project's compile-time classpath", e);
      } catch (IllegalArgumentException e) {
        throw new MojoFailureException(e.getMessage(), e);
      }
      try {
        cp = ClassPath.from(cl);
      } catch (IOException e) {
        throw new MojoFailureException("Unable to get classpath from classLoader", e);
      }

      List<Class<?>> publicApiClasses = new ArrayList<Class<?>>();

      for (ClassInfo classInfo : cp.getTopLevelClasses()) {
        // TODO handle empty includes case; maybe?
        for (String includePattern : includes) {
          if (classInfo.getName().matches(includePattern)) {
            boolean exclude = false;
            for (String excludePattern : excludes) {
              if (classInfo.getName().matches(excludePattern)) {
                exclude = true;
                break;
              }
            }
            if (!exclude) {
              publicApiClasses.add(classInfo.load());
            }
            break;
          }
        }
      }

      // add subclasses of public API
      for (Class<?> clazz : new ArrayList<Class<?>>(publicApiClasses)) {
        // TODO make recursive
        Class<?>[] declaredClasses = clazz.getDeclaredClasses();
        for (Class<?> declaredClazz : declaredClasses) {
          if (isPublicOrProtected(declaredClazz)) {
            publicApiClasses.add(declaredClazz);
          }
        }
      }

      TreeSet<String> publicSet =
          new TreeSet<String>(Collections2.transform(publicApiClasses,
              new Function<Class<?>, String>() {
                @Override
                public String apply(Class<?> input) {
                  return input.getName();
                }
              }));

      out.println();

      out.println("Public API:");
      for (String item : publicSet) {
        out.println(item);
      }
      out.println();

      out.printf(FORMAT, "CONTEXT", "TYPE", "FIELD/METHOD", "NON-PUBLIC REFERENCE");
      out.println();

      // look for public API methods/fields/subclasses that use classes not in public API
      for (Class<?> clazz : publicApiClasses) {
        checkClass(clazz, publicSet, out);
      }
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException(e.getMessage(), e);
    }

  }

  private boolean isPublicOrProtected(Class<?> clazz) {
    return (clazz.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0;
  }

  private boolean isPublicOrProtected(Member member) {
    return (member.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0;
  }
}
