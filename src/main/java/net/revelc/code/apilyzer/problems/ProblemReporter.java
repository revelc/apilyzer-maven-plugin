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

package net.revelc.code.apilyzer.problems;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * A utility for reporting the various problem types.
 */
public class ProblemReporter {

  private Consumer<Problem> consumer;

  public ProblemReporter(Consumer<Problem> consumer) {
    this.consumer = consumer;
  }

  /**
   * Report a problem with a constructor exception's type.
   */
  public void constructorException(Class<?> contextClass, Class<?> nonPublicException) {
    Problem p =
        new Problem(ProblemType.CTOR_EXCEPTION, contextClass, "(...) throws", nonPublicException);
    consumer.accept(p);
  }

  /**
   * Report a problem with a constructor parameter's type.
   */
  public void constructorParameter(Class<?> contextClass, Class<?> nonPublicParam) {
    Problem p = new Problem(ProblemType.CTOR_PARAM, contextClass, "(...)", nonPublicParam);
    consumer.accept(p);
  }

  /**
   * Report a problem with a field's type.
   */
  public void field(Class<?> contextClass, Field field) {
    Problem p = new Problem(ProblemType.FIELD, contextClass, field.getName(), field.getType());
    consumer.accept(p);
  }

  /**
   * Report a problem within an inner class.
   */
  public void innerClass(Class<?> contextClass, Class<?> nonPublicType) {
    Problem p = new Problem(ProblemType.INNER_CLASS, contextClass, "N/A", nonPublicType);
    consumer.accept(p);
  }

  /**
   * Report a problem with a method's exception type.
   */
  public void methodException(Class<?> contextClass, Method method, Class<?> nonPublicException) {
    Problem p = new Problem(ProblemType.METHOD_EXCEPTION, contextClass,
        method.getName() + "(...) throws", nonPublicException.getClass());
    consumer.accept(p);
  }

  /**
   * Report a problem with a method parameter's type.
   */
  public void methodParameter(Class<?> contextClass, Method method, Class<?> nonPublicParam) {
    Problem p = new Problem(ProblemType.METHOD_PARAM, contextClass, method.getName() + "(...)",
        nonPublicParam);
    consumer.accept(p);
  }

  /**
   * Report a problem with a method's return type.
   */
  public void methodReturn(Class<?> contextClass, Method method) {
    Problem p = new Problem(ProblemType.METHOD_RETURN, contextClass, method.getName() + "(...)",
        method.getReturnType());
    consumer.accept(p);
  }

}
