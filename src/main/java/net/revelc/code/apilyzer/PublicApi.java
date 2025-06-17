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

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
import net.revelc.code.apilyzer.util.ClassUtils;

/**
 * An object representing the public API for the analysis target.
 */
public class PublicApi {

  /**
   * Construct a public API definition object from a class path object and includes/excludes.
   */
  public static PublicApi fromClassPath(ClassPath classPath, List<String> includes,
      List<String> excludes, List<String> includeAnnotations, List<String> excludeAnnotations) {
    PublicApi api = new PublicApi(includes, excludes, includeAnnotations, excludeAnnotations);

    classLoop: for (ClassInfo classInfo : classPath.getAllClasses()) {

      // Do this check before possibly attempting any annotation checks as these require class
      // loading. If the class is excluded by a pattern, then no need to load class.
      if (api.excludes(classInfo)) {
        continue;
      }

      Annotation[] annotations = api.getAnnotations(classInfo);
      for (Annotation annotation : annotations) {
        if (api.includes(annotation)) {
          if (!api.annotationExcludes(annotations)) {
            api.addPublicApiType(classInfo);
          }
          continue classLoop;
        }
      }

      if (api.includes(classInfo) && !api.annotationExcludes(annotations)) {
        api.addPublicApiType(classInfo);
      }
    }
    return api;
  }

  private PatternSet includesPs;
  private PatternSet excludesPs;
  private PatternSet includeAnnotationsPs;
  private PatternSet excludeAnnotationsPs;
  private final List<Class<?>> publicApiClasses = new ArrayList<>();
  private final TreeSet<String> publicSet = new TreeSet<>();

  private PublicApi(List<String> includes, List<String> excludes, List<String> includeAnnotations,
      List<String> excludeAnnotations) {
    this.includesPs = new PatternSet(includes);
    this.includeAnnotationsPs = new PatternSet(includeAnnotations);
    this.excludesPs = new PatternSet(excludes);
    this.excludeAnnotationsPs = new PatternSet(excludeAnnotations);
  }

  private void addPublicApiType(ClassInfo classInfo) {
    Class<?> clazz = classInfo.load();
    if (ClassUtils.isPublicOrProtected(clazz) && !publicSet.contains(clazz.getName())) {
      publicApiClasses.add(clazz);
      publicSet.add(clazz.getName());

      addPublicInnerClasses(publicApiClasses, publicSet, clazz);
    }
  }

  private void addPublicInnerClasses(List<Class<?>> publicApiClasses, TreeSet<String> publicSet,
      Class<?> clazz) {

    Class<?>[] innerClasses = clazz.getDeclaredClasses();
    for (Class<?> ic : innerClasses) {
      // If a class is in the Public API then all of its public inner class are also considered
      // to be in the public API unless explicitly excluded.
      if (ClassUtils.isPublicOrProtected(ic) && !publicSet.contains(ic.getName())
          && !annotationExcludes(ic.getDeclaredAnnotations())
          && !excludesPs.anyMatch(ic.getName())) {
        publicApiClasses.add(ic);
        publicSet.add(ic.getName());

        addPublicInnerClasses(publicApiClasses, publicSet, ic);
      }
    }
  }

  private static String formatAnnotation(Annotation annotation) {
    return "@" + annotation.annotationType().getName();
  }

  private boolean annotationExcludes(Annotation[] annotations) {
    return !excludeAnnotationsPs.isEmpty() && Arrays.stream(annotations)
        .anyMatch(annotation -> excludeAnnotationsPs.anyMatch(formatAnnotation(annotation)));
  }

  Stream<Class<?>> classStream() {
    return publicApiClasses.stream();
  }

  boolean contains(String fqName) {
    return publicSet.contains(fqName);
  }

  boolean excludes(Class<?> classToCheck) {
    return excludesPs.anyMatch(classToCheck.getName())
        || annotationExcludes(classToCheck.getDeclaredAnnotations());
  }

  private boolean excludes(ClassInfo classInfo) {
    return excludesPs.anyMatch(classInfo.getName());
  }

  private Annotation[] getAnnotations(ClassInfo classInfo) {
    if (includeAnnotationsPs.isEmpty() && excludeAnnotationsPs.isEmpty()) {
      return new Annotation[0];
    }
    // ignore annotations from java itself, to avoid ClassNotFoundExceptions
    String name = classInfo.getName();
    return (name.startsWith("com.sun") || name.startsWith("java.")) ? new Annotation[0]
        : classInfo.load().getDeclaredAnnotations();
  }

  private boolean includes(Annotation annotation) {
    return includeAnnotationsPs.anyMatch(formatAnnotation(annotation));
  }

  private boolean includes(ClassInfo classInfo) {
    return includesPs.anyMatch(classInfo.getName());
  }

  public boolean isEmpty() {
    return publicSet.isEmpty();
  }

  public Stream<String> nameStream() {
    return publicSet.stream();
  }

}
