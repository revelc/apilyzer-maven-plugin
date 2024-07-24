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

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A set of patterns to match classes on the class path.
 */
class PatternSet {
  private final List<Pattern> patterns;

  PatternSet(List<String> regexs) {
    patterns = regexs.isEmpty() ? Collections.emptyList()
        : regexs.stream().map(Pattern::compile).collect(Collectors.toList());
  }

  boolean anyMatch(String input) {
    return patterns.stream().anyMatch(p -> p.matcher(input).matches());
  }

  boolean isEmpty() {
    return patterns.isEmpty();
  }
}
