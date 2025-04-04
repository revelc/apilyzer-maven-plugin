/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.revelc.code.apilyzer;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.revelc.code.apilyzer.problems.Problem;
import net.revelc.code.apilyzer.problems.ProblemReporter;
import net.revelc.code.apilyzer.util.ClassUtils;

/**
 * The entry point to this library.
 */
public class Apilyzer {

  private final ProblemReporter problemReporter;
  private final PatternSet allowsPs;
  private final boolean ignoreDeprecated;
  private final PublicApi publicApi;

  /**
   * Analyze a given public API definition to ensure it exposes only types available in itself and
   * in an allowed set of external APIs.
   */
  public Apilyzer(PublicApi publicApi, List<String> allows, boolean ignoreDeprecated,
      Consumer<Problem> problemConsumer) {
    this.problemReporter = new ProblemReporter(problemConsumer);
    this.allowsPs = new PatternSet(allows);
    this.ignoreDeprecated = ignoreDeprecated;
    this.publicApi = publicApi;
  }

  private boolean allowedExternalApi(String fqName) {
    // TODO make default allows configurable?
    if (fqName.startsWith("java.")) {
      return true;
    }
    return allowsPs.anyMatch(fqName);
  }

  private boolean deprecatedToIgnore(AnnotatedElement element) {
    return ignoreDeprecated && element.isAnnotationPresent(Deprecated.class);
  }

  private boolean isOk(Class<?> clazz) {

    while (clazz.isArray()) {
      clazz = clazz.getComponentType();
    }

    if (clazz.isPrimitive()) {
      return true;
    }

    String fqName = clazz.getName();
    return publicApi.contains(fqName) || allowedExternalApi(fqName);
  }

  private boolean checkClass(Class<?> clazz, Set<Class<?>> innerChecked) {

    boolean ok = true;

    if (deprecatedToIgnore(clazz)) {
      return true;
    }

    // TODO check generic type parameters

    for (Field field : ClassUtils.getFields(clazz)) {

      if (deprecatedToIgnore(field)) {
        continue;
      }

      if (!field.getDeclaringClass().getName().equals(clazz.getName())
          && isOk(field.getDeclaringClass())) {
        continue;
      }

      if (!isOk(field.getType())) {
        problemReporter.field(clazz, field);
        ok = false;
      }
    }

    Constructor<?>[] constructors = clazz.getConstructors();
    for (Constructor<?> constructor : constructors) {

      if (constructor.isSynthetic()) {
        continue;
      }

      if (deprecatedToIgnore(constructor)) {
        continue;
      }

      Class<?>[] params = constructor.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(param)) {
          problemReporter.constructorParameter(clazz, param);
          ok = false;
        }
      }

      Class<?>[] exceptions = constructor.getExceptionTypes();
      for (Class<?> exception : exceptions) {
        if (!isOk(exception)) {
          problemReporter.constructorException(clazz, exception);
          ok = false;
        }
      }
    }

    for (Method method : ClassUtils.getMethods(clazz)) {

      if (method.isSynthetic() || method.isBridge()) {
        continue;
      }

      if (deprecatedToIgnore(method)) {
        continue;
      }

      if (!method.getDeclaringClass().getName().equals(clazz.getName())
          && isOk(method.getDeclaringClass())) {
        continue;
      }

      if (!isOk(method.getReturnType())) {
        problemReporter.methodReturn(clazz, method);
        ok = false;
      }

      Class<?>[] params = method.getParameterTypes();
      for (Class<?> param : params) {
        if (!isOk(param)) {
          problemReporter.methodParameter(clazz, method, param);
          ok = false;
        }
      }

      Class<?>[] exceptions = method.getExceptionTypes();
      for (Class<?> exception : exceptions) {
        if (!isOk(exception)) {
          problemReporter.methodException(clazz, method, exception);
          ok = false;
        }
      }
    }

    for (Class<?> class1 : ClassUtils.getInnerClasses(clazz)) {

      if (innerChecked.contains(class1)) {
        continue;
      }

      innerChecked.add(class1);

      if (deprecatedToIgnore(class1)) {
        continue;
      }

      if (publicApi.excludes(class1)) {
        // this inner class is explicitly excluded from API so do not check it
        continue;
      }

      if (!isOk(class1) && !checkClass(class1, innerChecked)) {
        problemReporter.innerClass(clazz, class1);
        ok = false;
      }
    }

    return ok;
  }

  public void check() {
    publicApi.classStream().forEach(c -> checkClass(c, new HashSet<Class<?>>()));
  }

}
