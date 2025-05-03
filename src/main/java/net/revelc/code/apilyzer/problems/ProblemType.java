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

/**
 * An enumeration of problem types that can be identified and reported.
 */
public enum ProblemType {

  /**
   * A non-public API type was found while checking an inner class.
   */
  INNER_CLASS,

  /**
   * A method parameter's type isn't in the public API or in the allowed set.
   */
  METHOD_PARAM,

  /**
   * A method's return type isn't in the public API or in the allowed set.
   */
  METHOD_RETURN,

  /**
   * A field's type isn't in the public API or in the allowed set.
   */
  FIELD,

  /**
   * A constructor parameter's type isn't in the public API or in the allowed set.
   */
  CTOR_PARAM,

  /**
   * A constructor exception's type isn't in the public API or in the allowed set.
   */
  CTOR_EXCEPTION,

  /**
   * A method exception's type isn't in the public API or in the allowed set.
   */
  METHOD_EXCEPTION

}
