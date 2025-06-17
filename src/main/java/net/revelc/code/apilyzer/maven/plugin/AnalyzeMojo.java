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

import com.google.common.reflect.ClassPath;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import net.revelc.code.apilyzer.Apilyzer;
import net.revelc.code.apilyzer.PublicApi;
import net.revelc.code.apilyzer.problems.Problem;
import net.revelc.code.apilyzer.util.ClassUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Analyzes declared public API in a Maven build.
 */
@Mojo(name = "analyze", defaultPhase = LifecyclePhase.VERIFY,
    requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class AnalyzeMojo extends AbstractMojo {

  @Parameter(defaultValue = "${project}", readonly = true)
  private MavenProject project;

  /**
   * The classes to include in your public API definition. These classes will be included in the
   * analysis. The format is java regular expressions. Any classes on the classpath whose
   * fully-qualified class name matches any of these regular expressions, and does not match any of
   * those in the excludes, will be included for analysis.
   *
   * <p>Matching is done with the regular expression anchored to the beginning and end of the
   * fully-qualified class name, so there is no need to prefix with {@code ^} or suffix with
   * {@code $}. To match a partial class name, you will need to add {@code .*} as a prefix and/or
   * suffix.
   *
   * <p>If an include pattern matches a class, then that class along with all of its public or
   * protected inner classes are added to the public API definition. If you do not wish for a
   * particular inner class to be in the public API then you can add a more specific exclusion for
   * it. For example could include {@code com.foo.C} and exclude {@code com.foo.C$I1} if the inner
   * class {@code C$I1} ends up in the API when it's not wanted.
   *
   * <p>Example:
   *
   * <pre>
   * {@code
   * <configuration>
   *   ...
   *   <includes>
   *     <include>org[.]apache[.].*</include>
   *     <include>com[.]example[.]myproject[.].*</include>
   *   </includes>
   *   ...
   * </configuration>
   * }
   * </pre>
   *
   * @since 1.0.0
   */
  @Parameter(alias = "includes")
  private List<String> includes = Collections.emptyList();

  /**
   * The classes to exclude from your public API definition, which may have otherwise matched your
   * includes. The format is the same as {@link #includes}.
   *
   * <p>Example:
   *
   * <pre>
   * {@code
   * <configuration>
   *   ...
   *   <excludes>
   *     <exclude>.*[.]impl[.].*</exclude>
   *   </excludes>
   *   ...
   * </configuration>
   * }
   * </pre>
   *
   * @since 1.0.0
   */
  @Parameter(alias = "excludes")
  private List<String> excludes = Collections.emptyList();

  /**
   * The additional classes, which are allowed to be referenced in your public API, but are not,
   * themselves, declared as part of your API. For example, these may be objects from a standard
   * library, which you utilize as parameters in your API methods.
   *
   * <p>These follow the same format as {@link #includes} and {@link #excludes}.
   *
   * <p>Example:
   *
   * <pre>
   * {@code
   * <configuration>
   *   ...
   *   <allows>
   *     <allow>com[.]google[.]common[.].*</allow>
   *   </allows>
   *   ...
   * </configuration>
   * }
   * </pre>
   *
   * @since 1.0.0
   */
  @Parameter(alias = "allows")
  private List<String> allows = Collections.emptyList();

  /**
   * Allows skipping execution of this plugin. This may be useful for testing, or if you find that
   * analysis is taking too long.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "skip", property = "apilyzer.skip", defaultValue = "false")
  private boolean skip;

  /**
   * Controls whether API items marked with the {@link Deprecated} annotation are ignored. By
   * default, these are ignored (excluded from analysis). One useful way to make use of this plugin
   * is to use it to help identify API methods which should be deprecated (and eventually removed)
   * because they are using unexpected and problematic classes. Once found, they can be deprecated
   * and excluded from future analysis.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "ignoreDeprecated", property = "apilyzer.ignoreDeprecated",
      defaultValue = "true")
  private boolean ignoreDeprecated;

  /**
   * The absolute path for the report file.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "outputFile", property = "apilyzer.outputFile",
      defaultValue = "${project.build.directory}/apilyzer.txt")
  private String outputFile;

  /**
   * Allows ignoring the problems found. If this is set to true, then the report will still be
   * created, but the plugin will not cause the build to fail.
   *
   * @since 1.0.0
   */
  @Parameter(alias = "ignoreProblems", property = "apilyzer.ignoreProblems", defaultValue = "false")
  private boolean ignoreProblems;

  /**
   * This option enables including classes in your public API definition based on class level
   * annotations. This option takes one or more regular expression. Annotations are discovered using
   * reflection, so annotations scoped to compile may not be seen. For each regular expression
   * {@link java.lang.String#matches(java.lang.String)} is called to compare with each annotation's
   * class name prefixed with the '@' character. If any annotation matches any regular expression
   * and it does not match any exclusion, then its included as an API type.
   *
   * <p>This section of the configuration is ORed with the {@code <includes>} section. So if a class
   * matches something in either section (and its not excluded), then its included in the API
   * definition.
   *
   * <p>This section has the same behavior with inner classes as {@code <includes>}.
   *
   * <p>Example:
   *
   * <pre>
   * {@code
   * <configuration>
   *   ...
   *   <includeAnnotations>
   *     <include>[@]com[.]proj42[.]Public.*</include>
   *   </includeAnnotations>
   *   ...
   * </configuration>
   * }
   * </pre>
   *
   * @since 1.1.0
   */
  @Parameter(alias = "includeAnnotations")
  private List<String> includeAnnotations = Collections.emptyList();

  /**
   * Exclude classes from public API definition using annotation.
   *
   * <p>Example:
   *
   * <pre>
   * {@code
   * <configuration>
   *   ...
   *   <excludeAnnotations>
   *     <exclude>[@]com[.]proj42[.]Alpha.*</exclude>
   *   </excludeAnnotations>
   *   ...
   * </configuration>
   * }
   * </pre>
   *
   * @see AnalyzeMojo#includeAnnotations
   * @since 1.1.0
   */
  @Parameter(alias = "excludeAnnotations")
  private List<String> excludeAnnotations = Collections.emptyList();

  private static final String FORMAT = "  %-20s %-60s %-35s %s%n";

  @Override
  public void execute() throws MojoFailureException, MojoExecutionException {

    if (skip) {
      getLog().info("APILyzer execution skipped");
      return;
    }

    ClassPath classPath;
    try {
      classPath = ClassUtils.getClassPath(project.getCompileClasspathElements());
    } catch (IOException | DependencyResolutionRequiredException | IllegalArgumentException e) {
      throw new MojoExecutionException("Error resolving project classpath", e);
    }

    try (PrintStream out = new PrintStream(new File(outputFile))) {

      out.println("Includes: " + includes);
      out.println("IncludeAnnotations: " + includeAnnotations);
      out.println("ExcludesAnnotations: " + excludeAnnotations);
      out.println("Excludes: " + excludes);
      out.println("Allowed: " + allows);

      PublicApi publicApi = PublicApi.fromClassPath(classPath, includes, excludes,
          includeAnnotations, excludeAnnotations);

      if (publicApi.isEmpty()) {
        throw new MojoExecutionException("No public API types were matched");
      }

      out.println();
      out.println("Public API:");
      publicApi.nameStream().map(item -> "  " + item).forEach(out::println);
      out.println();
      out.println("Problems : ");
      out.println();
      out.printf(FORMAT, "CONTEXT", "TYPE", "FIELD/METHOD", "NON-PUBLIC REFERENCE");
      out.println();

      AtomicLong problemCounter = new AtomicLong(0);

      // look for public API methods/fields/subclasses that use classes not in public API
      Consumer<Problem> problemConsumer = problem -> {
        problemCounter.incrementAndGet();
        out.printf(FORMAT, problem.problemType, problem.contextClass.getName(), problem.memberName,
            problem.nonPublicType.getName());
      };
      new Apilyzer(publicApi, allows, ignoreDeprecated, problemConsumer).check();

      long problemCount = problemCounter.get();

      out.println();
      out.println("Total : " + problemCount);

      String msg =
          "APILyzer found " + problemCount + " problem" + (problemCount == 1 ? "" : "s") + ".";
      msg += " See " + outputFile + " for details.";
      if (problemCount < 0) {
        throw new AssertionError("Inconceivable!");
      } else if (problemCount == 0) {
        getLog().info(msg);
      } else if (problemCount > 0 && ignoreProblems) {
        getLog().warn(msg);
      } else {
        getLog().error(msg);
        throw new MojoFailureException(msg);
      }
    } catch (FileNotFoundException e) {
      throw new MojoExecutionException("Bad configuration: cannot create specified outputFile", e);
    }
  }

}
