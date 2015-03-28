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

View this plugin's documentation at: http://apilyzer.revelc.net

[APILyzer][2] is a maven plugin that detects illegal changes to a maven
projects Java API.  This accomplished by looking for deviations from the
following rules.

 * Public API members should use other Public API types.
 * Public API members should use an approved set of types.

This plugin allows easy declaration of Public API Types and approved types.
The plugin analyzes the public members of the public API types to ensure only
expected types are used.  Public members include public methods, public fields,
and public inner classes.  Protected classes and members are treated as public
during analysis. Deprecated parts of the public API are excluded from analysis.

[Analyzing imports][1] of public API types is insufficient, because it's ok for a
public API class to import a non public API class for use in its implementation.

To add this plugin to your project, configure the plugin similarly to:

```xml
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
                <!--Specify one or more regular expressions that define the
                    public API.  Each regex is matched agains all fully
                    qualified class names.  Any class that matches (and is
                    public) is added to the set of public API classes.-->
                <include>org[.]apache[.]accumulo[.]minicluster[.].*</include>
              </includes>
              <excludes>
                <!-- Specifiy zero or more regular expressions. Any regex that
                     matches will exclude a prevously included class from the
                     set of API classes -->
                <exclude>.*[.]impl[.].*</exclude>
                <exclude>.*Impl</exclude>
              </excludes>
              <allows>
                <!-- Specify zero or more regular expressions defining the set
                     of non-API classes thats it ok for public API members to
                     reference.  These regular expressions are matched against
                     fully qualified type names referenced by public API
                     members.  Conceptually, public API classes and Java classes
                     are automatically added to this set, so there is no need
                     to add those here. -->
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

[1]: http://checkstyle.sourceforge.net/config_imports.html#ImportControl
[2]: http://revelc.github.io/apilyzer-maven-plugin

