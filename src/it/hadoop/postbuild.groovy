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

File reportFileStable = new File(basedir, "target/apilyzer-stable.txt");
assert reportFileStable.isFile()
def lastStable=reportFileStable.withReader{ r->r.eachLine{ it } }
assert lastStable=="Total : 67"

File reportFileStableEvolving = new File(basedir, "target/apilyzer-stable-evolving.txt");
assert reportFileStableEvolving.isFile()
def lastStableEvolving=reportFileStableEvolving.withReader{ r->r.eachLine{ it } }
assert lastStableEvolving=="Total : 305"
