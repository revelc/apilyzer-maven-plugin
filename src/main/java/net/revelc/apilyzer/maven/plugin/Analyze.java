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
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

@Mojo(name = "analyze", defaultPhase = LifecyclePhase.INTEGRATION_TEST,
    requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME)
public class Analyze extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  private static final String format = "%-20s %-60s %-35s %-35s\n";

  private TreeSet<String> nonAccumulo = new TreeSet<String>();
  private TreeSet<String> nonPublic = new TreeSet<String>();

  private static String shorten(String className) {
    return className.replace("org.apache.accumulo.core.client", "o.a.a.c.c")
        .replace("org.apache.accumulo.core", "o.a.a.c").replace("org.apache.hadoop", "o.a.h")
        .replace("org.apache.accumulo", "o.a.a");
  }

  private boolean isOk(HashSet<String> publicSet, Class<?> clazz) {

    while (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }

    if (clazz.isPrimitive()) {
      return true;
    }

    if (publicSet.contains(clazz.getName())) {
      return true;
    }

    String pkg = clazz.getPackage().getName();

    if (!pkg.startsWith("org.apache.accumulo")) {
      if (!pkg.startsWith("java")) {
        nonAccumulo.add(clazz.getName());
      }
      return true;
    }

    nonPublic.add(clazz.getName());

    return false;
  }

  private void checkClass(Class<?> clazz, HashSet<String> publicSet) {

    if (clazz.isAnnotationPresent(Deprecated.class)) {
      return;
    }

    Field[] fields = clazz.getFields();
    for (Field field : fields) {
      if (field.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      if (!isOk(publicSet, field.getType())) {
        System.out.printf(format, "Field", shorten(clazz.getName()), field.getName(), shorten(field
            .getType().getName()));
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
          System.out.printf(format, "Constructor param", shorten(clazz.getName()), "(...)",
              shorten(param.getName()));
        }
      }
    }

    Method[] methods = clazz.getMethods();

    for (Method method : methods) {

      if (method.isAnnotationPresent(Deprecated.class)) {
        continue;
      }

      if (!isOk(publicSet, method.getReturnType())) {
        System.out.printf(format, "Method return", shorten(clazz.getName()), method.getName()
            + "(...)", shorten(method.getReturnType().getName()));
      }

      Class<?>[] params = method.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(publicSet, param)) {
          System.out.printf(format, "Method param", shorten(clazz.getName()), method.getName()
              + "(...)", shorten(param.getName()));
        }
      }
    }

    Class<?>[] classes = clazz.getClasses();
    for (Class<?> class1 : classes) {
      if (class1.isAnnotationPresent(Deprecated.class)) {
        continue;
      }
      if (!isOk(publicSet, class1)) {
        System.out.printf(format, "Public class", shorten(clazz.getName()), "N/A",
            shorten(class1.getName()));
      }
    }
  }

  @Override
  public void execute() throws MojoFailureException {
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

    for (ClassInfo classInfo : cp.getTopLevelClassesRecursive("org.apache.accumulo.core.client")) {
      if (classInfo.getName().contains(".impl.") || classInfo.getName().endsWith("Impl")) {
        continue;
      }
      publicApiClasses.add(classInfo.load());
    }

    for (ClassInfo classInfo : cp.getTopLevelClassesRecursive("org.apache.accumulo.minicluster")) {
      if (classInfo.getName().contains(".impl.") || classInfo.getName().endsWith("Impl")) {
        continue;
      }
      publicApiClasses.add(classInfo.load());
    }

    // add specific classes not in client or minicluster package
    try {
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.Mutation"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.Key"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.Value"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.Condition"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.ConditionalMutation"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.Range"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.security.ColumnVisibility"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.security.Authorizations"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.ByteSequence"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.PartialKey"));
      publicApiClasses.add(cl.loadClass("org.apache.accumulo.core.data.Column"));
    } catch (ClassNotFoundException e) {
      throw new MojoFailureException("Unable to find expected class", e);
    }

    // add subclasses of public API
    for (Class<?> clazz : new ArrayList<Class<?>>(publicApiClasses)) {
      Class<?>[] declaredClasses = clazz.getDeclaredClasses();
      for (Class<?> declaredClazz : declaredClasses) {
        if ((declaredClazz.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0) {
          publicApiClasses.add(declaredClazz);
        }
      }
    }

    HashSet<String> publicSet =
        new HashSet<String>(Collections2.transform(publicApiClasses,
            new Function<Class<?>, String>() {
              @Override
              public String apply(Class<?> input) {
                return input.getName();
              }
            }));

    System.out.printf(format, "CONTEXT", "TYPE", "FIELD/METHOD", "NON-PUBLIC REFERENCE");
    System.out.println();

    // look for public API methods/fields/subclasses that use classes not in public API
    for (Class<?> clazz : publicApiClasses) {
      checkClass(clazz, publicSet);
    }

    System.out.println();
    System.out.println("Non Public API classes referenced in API : ");
    System.out.println();

    for (String clazz : nonPublic) {
      System.out.println(clazz);
    }

    System.out.println();
    System.out.println("Non Accumulo classes referenced in API : ");
    System.out.println();

    for (String clazz : nonAccumulo) {
      System.out.println(clazz);
    }

  }
}
