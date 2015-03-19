Accumulo API Analyzer
=====================

A simple program that analyzes types used by public methods in Accumulo API and
generates a report.

The program looks for references to non public API types used by the Accumulo
public API.  Analyzing imports of public API types is insufficient, because its
ok for a public API class to import a non public API class for use in its
implementation.  All public methods, fields, and subclasses in the public API
are analyzed.  Deprecated parts of the public API are excluded from analysis.

To execute this program, run the following commands.

```
mvn package
mvn exec:java -Dexec.mainClass="aaa.Analyze" -Daccumulo.version=1.6.2
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
Method return        o.a.a.c.c.lexicoder.PairLexicoder                            decode(...)                         o.a.a.c.util.ComparablePair        
Method param         o.a.a.c.c.lexicoder.PairLexicoder                            encode(...)                         o.a.a.c.util.ComparablePair        
Constructor param    o.a.a.c.c.mock.IteratorAdapter                               (...)                               o.a.a.c.iterators.SortedKeyValueIterator
Method return        o.a.a.c.c.mock.MockBatchDeleter                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockBatchDeleter                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method return        o.a.a.c.c.mock.MockBatchScanner                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockBatchScanner                              createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockConfiguration                             get(...)                            o.a.a.c.conf.Property              
Method param         o.a.a.c.c.mock.MockConfiguration                             getProperties(...)                  o.a.a.c.conf.AccumuloConfiguration$PropertyFilter
Method param         o.a.a.c.c.mock.MockConfiguration                             getBoolean(...)                     o.a.a.c.conf.Property              
Method param         o.a.a.c.c.mock.MockConfiguration                             getPath(...)                        o.a.a.c.conf.Property              
Method param         o.a.a.c.c.mock.MockConfiguration                             getPort(...)                        o.a.a.c.conf.Property              
Method return        o.a.a.c.c.mock.MockConfiguration                             getDefaultConfiguration(...)        o.a.a.c.conf.DefaultConfiguration  
Method param         o.a.a.c.c.mock.MockConfiguration                             getTimeInMillis(...)                o.a.a.c.conf.Property              
Method param         o.a.a.c.c.mock.MockConfiguration                             getCount(...)                       o.a.a.c.conf.Property              
Method param         o.a.a.c.c.mock.MockConfiguration                             getMemoryInBytes(...)               o.a.a.c.conf.Property              
Method param         o.a.a.c.c.mock.MockConfiguration                             getFraction(...)                    o.a.a.c.conf.Property              
Method return        o.a.a.c.c.mock.MockConfiguration                             getTableConfiguration(...)          o.a.a.c.conf.AccumuloConfiguration 
Method param         o.a.a.c.c.mock.MockConfiguration                             instantiateClassProperty(...)       o.a.a.c.conf.Property              
Method param         o.a.a.c.c.mock.MockConfiguration                             getAllPropertiesWithPrefix(...)     o.a.a.c.conf.Property              
Public class         o.a.a.c.c.mock.MockConfiguration                             N/A                                 o.a.a.c.conf.AccumuloConfiguration$PrefixFilter
Public class         o.a.a.c.c.mock.MockConfiguration                             N/A                                 o.a.a.c.conf.AccumuloConfiguration$AllFilter
Public class         o.a.a.c.c.mock.MockConfiguration                             N/A                                 o.a.a.c.conf.AccumuloConfiguration$PropertyFilter
Method param         o.a.a.c.c.mock.MockNamespaceOperations                       getIteratorSetting(...)             o.a.a.c.iterators.IteratorUtil$IteratorScope
Method return        o.a.a.c.c.mock.MockScanner                                   createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockScanner                                   createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method return        o.a.a.c.c.mock.MockScannerBase                               createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockScannerBase                               createFilter(...)                   o.a.a.c.iterators.SortedKeyValueIterator
Method param         o.a.a.c.c.mock.MockShell                                     getClassLoader(...)                 o.a.a.c.util.shell.Shell           
Method param         o.a.a.c.c.mock.MockShell                                     printLines(...)                     o.a.a.c.util.shell.Shell$PrintLine 
Method param         o.a.a.c.c.mock.MockShell                                     printRecords(...)                   o.a.a.c.util.shell.Shell$PrintLine 
Method param         o.a.a.c.c.mock.MockShell                                     printBinaryRecords(...)             o.a.a.c.util.shell.Shell$PrintLine 
Public class         o.a.a.c.c.mock.MockShell                                     N/A                                 o.a.a.c.util.shell.Shell$PrintFile 
Public class         o.a.a.c.c.mock.MockShell                                     N/A                                 o.a.a.c.util.shell.Shell$PrintShell
Public class         o.a.a.c.c.mock.MockShell                                     N/A                                 o.a.a.c.util.shell.Shell$PrintLine 
Public class         o.a.a.c.c.mock.MockShell                                     N/A                                 o.a.a.c.util.shell.Shell$Command   
Method param         o.a.a.minicluster.MiniAccumuloInstance                       lookupInstanceName(...)             o.a.a.fate.zookeeper.ZooCache      
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
org.apache.accumulo.core.security.NamespacePermission
org.apache.accumulo.core.security.SystemPermission
org.apache.accumulo.core.security.TablePermission
org.apache.accumulo.core.util.ComparablePair
org.apache.accumulo.core.util.Pair
org.apache.accumulo.core.util.shell.Shell
org.apache.accumulo.core.util.shell.Shell$Command
org.apache.accumulo.core.util.shell.Shell$PrintFile
org.apache.accumulo.core.util.shell.Shell$PrintLine
org.apache.accumulo.core.util.shell.Shell$PrintShell
org.apache.accumulo.fate.zookeeper.ZooCache

Non Accumulo classes referenced in API : 

jline.console.ConsoleReader
org.apache.commons.cli.CommandLine
org.apache.commons.configuration.Configuration
org.apache.commons.configuration.PropertiesConfiguration
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
org.apache.hadoop.util.Progressable
org.apache.log4j.Level
org.apache.log4j.Logger
```

