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
      </execution>
    </executions>
  </plugin>
```

and build your project, similarly to:

```
mvn verify
```

Below is the output of running the above command.

```
CONTEXT              TYPE                                                         FIELD/METHOD                        NON-PUBLIC REFERENCE

Constructor param    o.a.a.c.c.AccumuloSecurityException                          (...)                               o.a.a.c.c.impl.thrift.SecurityErrorCode
Constructor param    o.a.a.c.c.AccumuloSecurityException                          (...)                               o.a.a.c.c.impl.thrift.SecurityErrorCode
Constructor param    o.a.a.c.c.AccumuloSecurityException                          (...)                               o.a.a.c.c.impl.thrift.SecurityErrorCode
Constructor param    o.a.a.c.c.AccumuloSecurityException                          (...)                               o.a.a.c.c.impl.thrift.ThriftSecurityException
Constructor param    o.a.a.c.c.AccumuloSecurityException                          (...)                               o.a.a.c.c.impl.thrift.SecurityErrorCode
Method return        o.a.a.c.c.AccumuloSecurityException                          asThriftException(...)              o.a.a.c.c.impl.thrift.ThriftSecurityException
Constructor param    o.a.a.c.c.IsolatedScanner                                    (...)                               o.a.a.c.c.IsolatedScanner$RowBufferFactory
Constructor param    o.a.a.c.c.NamespaceExistsException                           (...)                               o.a.a.c.c.impl.thrift.ThriftTableOperationException
Constructor param    o.a.a.c.c.NamespaceNotEmptyException                         (...)                               o.a.a.c.c.impl.thrift.ThriftTableOperationException
Constructor param    o.a.a.c.c.NamespaceNotFoundException                         (...)                               o.a.a.c.c.impl.thrift.ThriftTableOperationException
Constructor param    o.a.a.c.c.TableExistsException                               (...)                               o.a.a.c.c.impl.thrift.ThriftTableOperationException
Constructor param    o.a.a.c.c.TableNotFoundException                             (...)                               o.a.a.c.c.impl.thrift.ThriftTableOperationException
Method param         o.a.a.c.c.ZooKeeperInstance                                  lookupInstanceName(...)             o.a.a.fate.zookeeper.ZooCache
Method return        o.a.a.c.c.admin.ActiveCompaction                             getExtent(...)                      o.a.a.c.data.KeyExtent
Method return        o.a.a.c.c.admin.ActiveScan                                   getExtent(...)                      o.a.a.c.data.KeyExtent
Method param         o.a.a.c.c.admin.NamespaceOperations                          getIteratorSetting(...)             o.a.a.c.iterators.IteratorUtil$IteratorScope
Method param         o.a.a.c.c.admin.SecurityOperations                           hasSystemPermission(...)            o.a.a.c.security.SystemPermission
Method param         o.a.a.c.c.admin.SecurityOperations                           hasTablePermission(...)             o.a.a.c.security.TablePermission
Method param         o.a.a.c.c.admin.SecurityOperations                           hasNamespacePermission(...)         o.a.a.c.security.NamespacePermission
Method param         o.a.a.c.c.admin.SecurityOperations                           grantSystemPermission(...)          o.a.a.c.security.SystemPermission
Method param         o.a.a.c.c.admin.SecurityOperations                           grantTablePermission(...)           o.a.a.c.security.TablePermission
Method param         o.a.a.c.c.admin.SecurityOperations                           grantNamespacePermission(...)       o.a.a.c.security.NamespacePermission
Method param         o.a.a.c.c.admin.SecurityOperations                           revokeSystemPermission(...)         o.a.a.c.security.SystemPermission
Method param         o.a.a.c.c.admin.SecurityOperations                           revokeTablePermission(...)          o.a.a.c.security.TablePermission
Method param         o.a.a.c.c.admin.SecurityOperations                           revokeNamespacePermission(...)      o.a.a.c.security.NamespacePermission
Method param         o.a.a.c.c.admin.TableOperations                              getIteratorSetting(...)             o.a.a.c.iterators.IteratorUtil$IteratorScope
Method param         o.a.a.c.c.lexicoder.PairLexicoder                            encode(...)                         o.a.a.c.util.ComparablePair
Constructor param    o.a.a.c.c.mock.IteratorAdapter                               (...)                               o.a.a.c.iterators.SortedKeyValueIterator
Method return        o.a.a.c.c.mock.MockBatchDeleter                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockBatchDeleter                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method return        o.a.a.c.c.mock.MockBatchScanner                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockBatchScanner                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockConfiguration                             get(...)                            o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getProperties(...)                  o.a.a.c.conf.AccumuloConfiguration$PropertyFilter
Method param         o.a.a.c.c.mock.MockConfiguration                             getTimeInMillis(...)                o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getCount(...)                       o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getBoolean(...)                     o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getPath(...)                        o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getPort(...)                        o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getAllPropertiesWithPrefix(...)     o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getMemoryInBytes(...)               o.a.a.c.conf.Property
Method param         o.a.a.c.c.mock.MockConfiguration                             getFraction(...)                    o.a.a.c.conf.Property
Method return        o.a.a.c.c.mock.MockConfiguration                             getDefaultConfiguration(...)        o.a.a.c.conf.DefaultConfiguration
Method return        o.a.a.c.c.mock.MockConfiguration                             getTableConfiguration(...)          o.a.a.c.conf.AccumuloConfiguration
Method param         o.a.a.c.c.mock.MockConfiguration                             instantiateClassProperty(...)       o.a.a.c.conf.Property
Public class         o.a.a.c.c.mock.MockConfiguration                             N/A                                 o.a.a.c.conf.AccumuloConfiguration$PrefixFilter
Public class         o.a.a.c.c.mock.MockConfiguration                             N/A                                 o.a.a.c.conf.AccumuloConfiguration$AllFilter
Public class         o.a.a.c.c.mock.MockConfiguration                             N/A                                 o.a.a.c.conf.AccumuloConfiguration$PropertyFilter
Method param         o.a.a.c.c.mock.MockNamespaceOperations                       getIteratorSetting(...)             o.a.a.c.iterators.IteratorUtil$IteratorScope
Method return        o.a.a.c.c.mock.MockScanner                                   createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockScanner                                   createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method return        o.a.a.c.c.mock.MockScannerBase                               createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockScannerBase                               createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockSecurityOperations                        hasSystemPermission(...)            o.a.a.c.security.SystemPermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        hasTablePermission(...)             o.a.a.c.security.TablePermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        hasNamespacePermission(...)         o.a.a.c.security.NamespacePermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        grantSystemPermission(...)          o.a.a.c.security.SystemPermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        grantTablePermission(...)           o.a.a.c.security.TablePermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        grantNamespacePermission(...)       o.a.a.c.security.NamespacePermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        revokeSystemPermission(...)         o.a.a.c.security.SystemPermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        revokeTablePermission(...)          o.a.a.c.security.TablePermission
Method param         o.a.a.c.c.mock.MockSecurityOperations                        revokeNamespacePermission(...)      o.a.a.c.security.NamespacePermission
Method param         o.a.a.c.c.mock.MockTableOperations                           getIteratorSetting(...)             o.a.a.c.iterators.IteratorUtil$IteratorScope
Constructor param    o.a.a.c.c.security.tokens.DelegationToken                    (...)                               o.a.a.c.security.AuthenticationTokenIdentifier
Constructor param    o.a.a.c.c.security.tokens.DelegationToken                    (...)                               o.a.a.c.security.AuthenticationTokenIdentifier
Constructor param    o.a.a.c.c.security.tokens.DelegationToken                    (...)                               o.a.a.c.security.AuthenticationTokenIdentifier
Method return        o.a.a.c.c.security.tokens.DelegationToken                    getIdentifier(...)                  o.a.a.c.security.AuthenticationTokenIdentifier
Constructor param    o.a.a.c.data.Mutation                                        (...)                               o.a.a.c.data.thrift.TMutation
Method return        o.a.a.c.data.Mutation                                        toThrift(...)                       o.a.a.c.data.thrift.TMutation
Constructor param    o.a.a.c.data.Key                                             (...)                               o.a.a.c.data.thrift.TKey
Method return        o.a.a.c.data.Key                                             toThrift(...)                       o.a.a.c.data.thrift.TKey
Method return        o.a.a.c.data.ConditionalMutation                             toThrift(...)                       o.a.a.c.data.thrift.TMutation
Constructor param    o.a.a.c.data.Range                                           (...)                               o.a.a.c.data.thrift.TRange
Method return        o.a.a.c.data.Range                                           toThrift(...)                       o.a.a.c.data.thrift.TRange
Constructor param    o.a.a.c.data.Column                                          (...)                               o.a.a.c.data.thrift.TColumn
Method return        o.a.a.c.data.Column                                          toThrift(...)                       o.a.a.c.data.thrift.TColumn
Method return        o.a.a.c.c.ClientConfiguration$ClientProperty                 getType(...)                        o.a.a.c.conf.PropertyType
Method return        o.a.a.c.c.ClientConfiguration$ClientProperty                 getAccumuloProperty(...)            o.a.a.c.conf.Property
Method param         o.a.a.c.c.ClientSideIteratorScanner$ScannerTranslator        init(...)                           o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.ClientSideIteratorScanner$ScannerTranslator        init(...)                           o.a.a.c.iterators.IteratorEnvironment
Method return        o.a.a.c.c.ClientSideIteratorScanner$ScannerTranslator        deepCopy(...)                       o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.ClientSideIteratorScanner$ScannerTranslator        deepCopy(...)                       o.a.a.c.iterators.IteratorEnvironment
Method return        o.a.a.c.c.IsolatedScanner$MemoryRowBufferFactory             newBuffer(...)                      o.a.a.c.c.IsolatedScanner$RowBuffer
Method return        o.a.a.c.c.IteratorSetting$Column                             swap(...)                           o.a.a.c.util.Pair
Method return        o.a.a.c.c.IteratorSetting$Column                             fromEntry(...)                      o.a.a.c.util.Pair

Non Public API classes referenced in API :

org.apache.accumulo.core.client.IsolatedScanner$RowBuffer
org.apache.accumulo.core.client.IsolatedScanner$RowBufferFactory
org.apache.accumulo.core.client.impl.thrift.SecurityErrorCode
org.apache.accumulo.core.client.impl.thrift.ThriftSecurityException
org.apache.accumulo.core.client.impl.thrift.ThriftTableOperationException
org.apache.accumulo.core.conf.AccumuloConfiguration
org.apache.accumulo.core.conf.AccumuloConfiguration$AllFilter
org.apache.accumulo.core.conf.AccumuloConfiguration$PrefixFilter
org.apache.accumulo.core.conf.AccumuloConfiguration$PropertyFilter
org.apache.accumulo.core.conf.DefaultConfiguration
org.apache.accumulo.core.conf.Property
org.apache.accumulo.core.conf.PropertyType
org.apache.accumulo.core.data.KeyExtent
org.apache.accumulo.core.data.thrift.TColumn
org.apache.accumulo.core.data.thrift.TKey
org.apache.accumulo.core.data.thrift.TMutation
org.apache.accumulo.core.data.thrift.TRange
org.apache.accumulo.core.iterators.IteratorEnvironment
org.apache.accumulo.core.iterators.IteratorUtil$IteratorScope
org.apache.accumulo.core.iterators.SortedKeyValueIterator
org.apache.accumulo.core.security.AuthenticationTokenIdentifier
org.apache.accumulo.core.security.NamespacePermission
org.apache.accumulo.core.security.SystemPermission
org.apache.accumulo.core.security.TablePermission
org.apache.accumulo.core.util.ComparablePair
org.apache.accumulo.core.util.Pair
org.apache.accumulo.fate.zookeeper.ZooCache

Non Accumulo classes referenced in API :

org.apache.commons.configuration.Configuration
org.apache.commons.configuration.event.ConfigurationErrorListener
org.apache.commons.configuration.event.ConfigurationListener
org.apache.commons.configuration.interpol.ConfigurationInterpolator
org.apache.commons.lang.text.StrSubstitutor
org.apache.commons.logging.Log
org.apache.hadoop.conf.Configuration
org.apache.hadoop.fs.FileSystem
org.apache.hadoop.fs.Path
org.apache.hadoop.io.Text
org.apache.hadoop.io.Writable
org.apache.hadoop.io.WritableComparable
org.apache.hadoop.io.WritableComparator
org.apache.hadoop.mapred.InputSplit
org.apache.hadoop.mapred.JobConf
org.apache.hadoop.mapred.RecordReader
org.apache.hadoop.mapred.RecordWriter
org.apache.hadoop.mapred.Reporter
org.apache.hadoop.mapreduce.InputSplit
org.apache.hadoop.mapreduce.Job
org.apache.hadoop.mapreduce.JobContext
org.apache.hadoop.mapreduce.OutputCommitter
org.apache.hadoop.mapreduce.RecordReader
org.apache.hadoop.mapreduce.RecordWriter
org.apache.hadoop.mapreduce.TaskAttemptContext
org.apache.hadoop.mapreduce.TaskInputOutputContext
org.apache.hadoop.security.UserGroupInformation
org.apache.hadoop.security.token.Token
org.apache.hadoop.util.Progressable
org.apache.log4j.Level
```

