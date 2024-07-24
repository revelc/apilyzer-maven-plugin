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

package net.revelc.code.apilyzer.util;

import com.google.common.reflect.ClassPath;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Some basic static utilities for searching and processing the class path.
 */
public class ClassUtils {

  private ClassUtils() {
    // do not permit instantiation
  }

  private static final Function<String, URL> TO_URL = item -> {
    URI uri = new File(item).toURI();
    try {
      return uri.toURL();
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Unable to convert string (" + item + ") to URL", e);
    }
  };

  /**
   * Construct a class path object from a list of local file system paths.
   */
  public static ClassPath getClassPath(List<String> paths) throws IOException {
    URL[] urls = paths.stream().map(TO_URL).toArray(URL[]::new);
    return ClassPath.from(new URLClassLoader(urls, null));
  }

  public static boolean isPublicOrProtected(Class<?> clazz) {
    return (clazz.getModifiers() & (Modifier.PUBLIC | Modifier.PROTECTED)) != 0;
  }

  /**
   * Get all inner classes and interfaces that are public (including inherited) or protected
   * (currently, doesn't include inherited).
   */
  public static List<Class<?>> getInnerClasses(Class<?> clazz) {
    // TODO need to also get inherited protected classes, deduping on name

    Stream<Class<?>> publicInners = Arrays.stream(clazz.getClasses());
    Stream<Class<?>> protectedInners = Arrays.stream(clazz.getDeclaredClasses())
        .filter(c -> Modifier.isProtected(c.getModifiers()));
    return Stream.concat(publicInners, protectedInners).collect(Collectors.toList());
  }

  /**
   * Get all public (including inherited) and protected (currently, excluding inherited) fields.
   */
  public static List<Field> getFields(Class<?> clazz) {
    // TODO need to also get inherited protected fields, deduping on name

    Stream<Field> publicFields = Arrays.stream(clazz.getFields());
    Stream<Field> protectedFields = Arrays.stream(clazz.getDeclaredFields())
        .filter(f -> Modifier.isProtected(f.getModifiers()));
    return Stream.concat(publicFields, protectedFields).collect(Collectors.toList());
  }

  /**
   * Get all public (including inherited) and protected (currently, excluding inherited) methods.
   */
  public static List<Method> getMethods(Class<?> clazz) {
    // TODO need to also get inherited protected methods, deduping on signature

    Stream<Method> publicFields = Arrays.stream(clazz.getMethods());
    Stream<Method> protectedFields = Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> Modifier.isProtected(m.getModifiers()));
    return Stream.concat(publicFields, protectedFields).collect(Collectors.toList());
  }

}
