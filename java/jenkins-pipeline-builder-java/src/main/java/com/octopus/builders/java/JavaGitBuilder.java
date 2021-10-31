package com.octopus.builders.java;

import com.google.common.collect.ImmutableList;
import com.octopus.builders.GitBuilder;
import com.octopus.dsl.ArgType;
import com.octopus.dsl.Argument;
import com.octopus.dsl.Comment;
import com.octopus.dsl.Element;
import com.octopus.dsl.Function1ArgTrailingLambda;
import com.octopus.dsl.FunctionManyArgs;
import com.octopus.dsl.FunctionTrailingLambda;
import com.octopus.dsl.StringContent;
import com.octopus.repoclients.RepoClient;
import java.util.logging.Logger;
import lombok.NonNull;

/**
 * A utility class containing useful methods common to Java pipelines.
 */
public class JavaGitBuilder extends GitBuilder {

  private static final Logger LOG = Logger.getLogger(JavaGitBuilder.class.toString());

  /**
   * Creates the deployment stage.
   *
   * @param buildDir The directory holding the compiled artifacts.
   * @return A list with a single element representing the stage.
   */
  public Element createDeployStep(@NonNull final String buildDir, @NonNull final RepoClient accessor) {
    return Function1ArgTrailingLambda.builder()
        .name("stage")
        .arg("Repackage")
        .children(createStepsElement(new ImmutableList.Builder<Element>()
            .add(Comment.builder()
                .content(
                    "This scans through the build tool output directory and find the largest file, "
                        + "which we assume is the artifact that was intended to be deployed.\n"
                        + "The path to this file is saved in and environment variable called "
                        + "JAVA_ARTIFACT, which can be consumed by subsequent custom deployment "
                        + "steps.")
                .build())
            .add(FunctionTrailingLambda.builder()
                .name("script")
                .children(new ImmutableList.Builder<Element>()
                    .add(StringContent.builder()
                        .content(
                            "// Find the matching artifacts\n"
                                + "def extensions = ['jar', 'war']\n"
                                + "def files = []\n"
                                + "for(extension in extensions){\n"
                                + "    findFiles(glob: '" + buildDir
                                + "/**.' + extension).each{files << it}\n"
                                + "}\n"
                                + "echo 'Found ' + files.size() + ' potential artifacts'\n"
                                + "// Assume the largest file is the artifact we intend to deploy\n"
                                + "def largestFile = null\n"
                                + "for (i = 0; i < files.size(); ++i) {\n"
                                + "\tif (largestFile == null || files[i].length > largestFile.length) { \n"
                                + "\t\tlargestFile = files[i]\n"
                                + "\t}\n"
                                + "}\n"
                                + "if (largestFile != null) {\n"
                                + "\tenv.ORIGINAL_ARTIFACT = largestFile.path\n"
                                + "\t// Create a filename based on the repository name, the new version, and the original file extension. \n"
                                + "\tenv.ARTIFACTS = \"" + accessor.getRepoName().getOrElse(DEFAULT_APPLICATION) + ".\" + env.VERSION_SEMVER + largestFile.path.substring(largestFile.path.lastIndexOf(\".\"), largestFile.path.length())\n"
                                + "\techo 'Found artifact at ' + largestFile.path\n"
                                + "\techo 'This path is available from the ARTIFACTS environment variable.'\n"
                                + "}\n"
                        )
                        .build())
                    .build())
                .build())
            .add(Comment.builder()
                .content(
                    "Octopus requires files to have a specific naming format. So copy the original artifact into a file with the correct name.")
                .build())
            .add(FunctionManyArgs.builder()
                .name("sh")
                .args(new ImmutableList.Builder<Argument>()
                    .add(new Argument("script", "cp ${ORIGINAL_ARTIFACT} ${ARTIFACTS}",
                        ArgType.STRING))
                    .build())
                .build())
            .build()))
        .build();
  }
}
