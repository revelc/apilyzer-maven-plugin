<!--
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

APILyzer: A simple API Analyzer
===============================

A simple maven plugin that analyzes types used by declared public API methods
and generates a report.

The program looks for references to non public API types used by the public API.
Analyzing imports of public API types is insufficient, because its ok for a
public API class to import a non public API class for use in its implementation.
All public methods, fields, and subclasses in the public API are analyzed.
Deprecated parts of the public API are excluded from analysis.

To add this plugin to your project, configure the plugin similarly to:

```
  <build>
    <plugins>
      <plugin>
        <groupId>net.revelc</groupId>
        <artifactId>apilyzer-maven-plugin</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <executions>
          <execution>
            <id>apilyzer</id>
            <goals>
              <goal>analyze</goal>
            </goals>
            <configuration>
              <includes>
                <include>org[.]apache[.]accumulo[.]minicluster[.].*</include>
              </includes>
              <excludes>
                <exclude>.*[.]impl[.].*</exclude>
                <exclude>.*Impl</exclude>
              </excludes>
              <allows>
                <allow>org[.]apache[.]accumulo[.]core[.]client[.].*</allow>
                <allow>org[.]apache[.]accumulo[.]core[.]data[.](Mutation|Key|Value|Condition|ConditionalMutation|Range|ByteSequence|PartialKey|Column)</allow>
                <allow>org[.]apache[.]accumulo[.]core[.]security[.](ColumnVisibility|Authorizations)</allow>
              </allows>
            </configuration>
          </execution>
        </executions>
      </plugin>
    <plugins>
  </build>
```

and build your project, similarly to (it runs at the verify phase by default):

```
mvn verify
```

Below is the output of running the above command.

```
Includes: [org[.]apache[.]accumulo[.]minicluster[.].*]
Excludes: [.*[.]impl[.].*, .*Impl]
Allowed: [org[.]apache[.]accumulo[.]core[.]client[.].*, org[.]apache[.]accumulo[.]core[.]data[.](Mutation|Key|Value|Condition|ConditionalMutation|Range|ByteSequence|PartialKey|Column), org[.]apache[.]accumulo[.]core[.]security[.](ColumnVisibility|Authorizations)]

Public API:
org.apache.accumulo.minicluster.MemoryUnit
org.apache.accumulo.minicluster.MiniAccumuloCluster
org.apache.accumulo.minicluster.MiniAccumuloConfig
org.apache.accumulo.minicluster.MiniAccumuloInstance
org.apache.accumulo.minicluster.MiniAccumuloRunner
org.apache.accumulo.minicluster.MiniAccumuloRunner$Opts
org.apache.accumulo.minicluster.MiniAccumuloRunner$PropertiesConverter
org.apache.accumulo.minicluster.ServerType

CONTEXT              TYPE                                                         FIELD/METHOD                        NON-PUBLIC REFERENCE

Method return        org.apache.accumulo.minicluster.MiniAccumuloInstance         getConfigProperties(...)            org.apache.commons.configuration.PropertiesConfiguration
Method param         org.apache.accumulo.minicluster.MiniAccumuloInstance         lookupInstanceName(...)             org.apache.accumulo.fate.zookeeper.ZooCache
```

