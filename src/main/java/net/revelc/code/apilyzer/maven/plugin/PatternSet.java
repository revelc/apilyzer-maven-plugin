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

package net.revelc.code.apilyzer.maven.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

class PatternSet {
  private final List<Pattern> patterns;

  PatternSet(List<String> regexs) {
    if (regexs.size() == 0) {
      patterns = Collections.emptyList();
    } else {
      patterns = new ArrayList<>();
      for (String regex : regexs) {
        patterns.add(Pattern.compile(regex));
      }
    }
  }

  boolean matchesAny(String input) {
    for (Pattern pattern : patterns) {
      if (pattern.matcher(input).matches()) {
        return true;
      }
    }

    return false;
  }

  public int size() {
    return patterns.size();
  }
}
