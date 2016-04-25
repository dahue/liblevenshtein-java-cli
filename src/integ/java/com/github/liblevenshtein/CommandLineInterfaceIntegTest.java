package com.github.liblevenshtein;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

import lombok.extern.slf4j.Slf4j;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.github.liblevenshtein.assertion.ProcessAssertions;
import static com.github.liblevenshtein.assertion.ProcessAssertions.assertThat;

@Slf4j
public class CommandLineInterfaceIntegTest {

  private static final String STANDARD_OUTPUT_WITH_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispleling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"mispleling\", \"misplacing\") = [2]\n"
  + "| d(\"mispleling\", \"misplaying\") = [2]\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"distnace\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"distnace\", \"disgrace\") = [2]\n"
  + "| d(\"distnace\", \"displace\") = [2]\n"
  + "| d(\"distnace\", \"distance\") = [2]\n"
  + "| d(\"distnace\", \"distract\") = [2]\n";

  private static final String STANDARD_OUTPUT_WITHOUT_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispleling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"mispleling\" ~ \"misplacing\"\n"
  + "| \"mispleling\" ~ \"misplaying\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"distnace\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"distnace\" ~ \"disgrace\"\n"
  + "| \"distnace\" ~ \"displace\"\n"
  + "| \"distnace\" ~ \"distance\"\n"
  + "| \"distnace\" ~ \"distract\"\n";

  private static final String TRANSPOSITION_OUTPUT_WITH_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispleling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"mispleling\", \"dispelling\") = [2]\n"
  + "| d(\"mispleling\", \"misplacing\") = [2]\n"
  + "| d(\"mispleling\", \"misplaying\") = [2]\n"
  + "| d(\"mispleling\", \"misspelling\") = [2]\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"distnace\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"distnace\", \"disgrace\") = [2]\n"
  + "| d(\"distnace\", \"displace\") = [2]\n"
  + "| d(\"distnace\", \"distance\") = [1]\n"
  + "| d(\"distnace\", \"distract\") = [2]\n"
  + "| d(\"distnace\", \"distanced\") = [2]\n"
  + "| d(\"distnace\", \"distances\") = [2]\n";

  private static final String TRANSPOSITION_OUTPUT_WITHOUT_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispleling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"mispleling\" ~ \"dispelling\"\n"
  + "| \"mispleling\" ~ \"misplacing\"\n"
  + "| \"mispleling\" ~ \"misplaying\"\n"
  + "| \"mispleling\" ~ \"misspelling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"distnace\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"distnace\" ~ \"disgrace\"\n"
  + "| \"distnace\" ~ \"displace\"\n"
  + "| \"distnace\" ~ \"distance\"\n"
  + "| \"distnace\" ~ \"distract\"\n"
  + "| \"distnace\" ~ \"distanced\"\n"
  + "| \"distnace\" ~ \"distances\"\n";

  private static final String MERGE_AND_SPLIT_OUTPUT_WITH_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispleling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"mispleling\", \"middling\") = [2]\n"
  + "| d(\"mispleling\", \"mingling\") = [2]\n"
  + "| d(\"mispleling\", \"miscuing\") = [2]\n"
  + "| d(\"mispleling\", \"misdoing\") = [2]\n"
  + "| d(\"mispleling\", \"misusing\") = [2]\n"
  + "| d(\"mispleling\", \"modeling\") = [2]\n"
  + "| d(\"mispleling\", \"misfiling\") = [2]\n"
  + "| d(\"mispleling\", \"misruling\") = [2]\n"
  + "| d(\"mispleling\", \"misbilling\") = [2]\n"
  + "| d(\"mispleling\", \"miscalling\") = [2]\n"
  + "| d(\"mispleling\", \"misdealing\") = [2]\n"
  + "| d(\"mispleling\", \"mishearing\") = [2]\n"
  + "| d(\"mispleling\", \"misleading\") = [2]\n"
  + "| d(\"mispleling\", \"mismeeting\") = [2]\n"
  + "| d(\"mispleling\", \"misplacing\") = [2]\n"
  + "| d(\"mispleling\", \"misplaying\") = [2]\n"
  + "| d(\"mispleling\", \"misreading\") = [2]\n"
  + "| d(\"mispleling\", \"misterming\") = [2]\n"
  + "| d(\"mispleling\", \"mistitling\") = [2]\n"
  + "| d(\"mispleling\", \"displeasing\") = [2]\n"
  + "| d(\"mispleling\", \"misalleging\") = [2]\n"
  + "| d(\"mispleling\", \"mislabeling\") = [2]\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"distnace\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| d(\"distnace\", \"deface\") = [2]\n"
  + "| d(\"distnace\", \"disbar\") = [2]\n"
  + "| d(\"distnace\", \"dismal\") = [2]\n"
  + "| d(\"distnace\", \"dismay\") = [2]\n"
  + "| d(\"distnace\", \"distal\") = [2]\n"
  + "| d(\"distnace\", \"disuse\") = [2]\n"
  + "| d(\"distnace\", \"menace\") = [2]\n"
  + "| d(\"distnace\", \"discase\") = [2]\n"
  + "| d(\"distnace\", \"disease\") = [2]\n"
  + "| d(\"distnace\", \"distich\") = [2]\n"
  + "| d(\"distnace\", \"pinnace\") = [2]\n"
  + "| d(\"distnace\", \"disgrace\") = [2]\n"
  + "| d(\"distnace\", \"dispense\") = [2]\n"
  + "| d(\"distnace\", \"displace\") = [2]\n"
  + "| d(\"distnace\", \"distance\") = [2]\n"
  + "| d(\"distnace\", \"distaste\") = [2]\n"
  + "| d(\"distnace\", \"distichs\") = [2]\n"
  + "| d(\"distnace\", \"distract\") = [2]\n"
  + "| d(\"distnace\", \"distracts\") = [2]\n"
  + "| d(\"distnace\", \"dissonance\") = [2]\n";

  private static final String MERGE_AND_SPLIT_OUTPUT_WITHOUT_DISTANCES =
    "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"mispleling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"mispleling\" ~ \"middling\"\n"
  + "| \"mispleling\" ~ \"mingling\"\n"
  + "| \"mispleling\" ~ \"miscuing\"\n"
  + "| \"mispleling\" ~ \"misdoing\"\n"
  + "| \"mispleling\" ~ \"misusing\"\n"
  + "| \"mispleling\" ~ \"modeling\"\n"
  + "| \"mispleling\" ~ \"misfiling\"\n"
  + "| \"mispleling\" ~ \"misruling\"\n"
  + "| \"mispleling\" ~ \"misbilling\"\n"
  + "| \"mispleling\" ~ \"miscalling\"\n"
  + "| \"mispleling\" ~ \"misdealing\"\n"
  + "| \"mispleling\" ~ \"mishearing\"\n"
  + "| \"mispleling\" ~ \"misleading\"\n"
  + "| \"mispleling\" ~ \"mismeeting\"\n"
  + "| \"mispleling\" ~ \"misplacing\"\n"
  + "| \"mispleling\" ~ \"misplaying\"\n"
  + "| \"mispleling\" ~ \"misreading\"\n"
  + "| \"mispleling\" ~ \"misterming\"\n"
  + "| \"mispleling\" ~ \"mistitling\"\n"
  + "| \"mispleling\" ~ \"displeasing\"\n"
  + "| \"mispleling\" ~ \"misalleging\"\n"
  + "| \"mispleling\" ~ \"mislabeling\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| Spelling Candidates for Query Term: \"distnace\"\n"
  + "+-------------------------------------------------------------------------------\n"
  + "| \"distnace\" ~ \"deface\"\n"
  + "| \"distnace\" ~ \"disbar\"\n"
  + "| \"distnace\" ~ \"dismal\"\n"
  + "| \"distnace\" ~ \"dismay\"\n"
  + "| \"distnace\" ~ \"distal\"\n"
  + "| \"distnace\" ~ \"disuse\"\n"
  + "| \"distnace\" ~ \"menace\"\n"
  + "| \"distnace\" ~ \"discase\"\n"
  + "| \"distnace\" ~ \"disease\"\n"
  + "| \"distnace\" ~ \"distich\"\n"
  + "| \"distnace\" ~ \"pinnace\"\n"
  + "| \"distnace\" ~ \"disgrace\"\n"
  + "| \"distnace\" ~ \"dispense\"\n"
  + "| \"distnace\" ~ \"displace\"\n"
  + "| \"distnace\" ~ \"distance\"\n"
  + "| \"distnace\" ~ \"distaste\"\n"
  + "| \"distnace\" ~ \"distichs\"\n"
  + "| \"distnace\" ~ \"distract\"\n"
  + "| \"distnace\" ~ \"distracts\"\n"
  + "| \"distnace\" ~ \"dissonance\"\n";

  private static final String HELP_TEXT =
    "usage: liblevenshtein-java-cli [-a\n"
  + "       <STANDARD|TRANSPOSITION|MERGE_AND_SPLIT>] [--colorize] [-d\n"
  + "       <PATH|URI>] [-h] [-i] [-m <INTEGER>] [-q <STRING> <STRING> <...>]\n"
  + "       [-s] [--serialize <PATH>] [--source-format\n"
  + "       <PROTOBUF|BYTECODE|PLAIN_TEXT>] [--target-format\n"
  + "       <PROTOBUF|BYTECODE|PLAIN_TEXT>]\n"
  + "Command-Line Interface to liblevenshtein (Java)\n"
  + "\n"
  + " -a,--algorithm <STANDARD|TRANSPOSITION|MERGE_AND_SPLIT>   Levenshtein\n"
  + "                                                           algorithm to\n"
  + "                                                           use (Default:\n"
  + "                                                           TRANSPOSITION)\n"
  + "    --colorize                                             Colorize output\n"
  + " -d,--dictionary <PATH|URI>                                Filesystem path\n"
  + "                                                           or\n"
  + "                                                           Java-compatible\n"
  + "                                                           URI to a\n"
  + "                                                           dictionary of\n"
  + "                                                           terms (Default:\n"
  + "                                                           jar:file:///hom\n"
  + "                                                           e/dylon/Workspa\n"
  + "                                                           ce/liblevenshte\n"
  + "                                                           in-java/java-cl\n"
  + "                                                           i/build/install\n"
  + "                                                           /liblevenshtein\n"
  + "                                                           -java-cli/lib/l\n"
  + "                                                           iblevenshtein-j\n"
  + "                                                           ava-cli-2.2.1.j\n"
  + "                                                           ar!/wordsEn.txt\n"
  + "                                                           )\n"
  + " -h,--help                                                 print this help\n"
  + "                                                           text\n"
  + " -i,--include-distance                                     Include the\n"
  + "                                                           Levenshtein\n"
  + "                                                           distance with\n"
  + "                                                           each spelling\n"
  + "                                                           candidate\n"
  + "                                                           (Default:\n"
  + "                                                           false)\n"
  + " -m,--max-distance <INTEGER>                               Maximun,\n"
  + "                                                           Levenshtein\n"
  + "                                                           distance a\n"
  + "                                                           spelling\n"
  + "                                                           candidatemay be\n"
  + "                                                           from the query\n"
  + "                                                           term (Default:\n"
  + "                                                           2)\n"
  + " -q,--query <STRING> <STRING> <...>                        Terms to query\n"
  + "                                                           against the\n"
  + "                                                           dictionary\n"
  + " -s,--is-sorted                                            Specifies that\n"
  + "                                                           the dictionary\n"
  + "                                                           is sorted\n"
  + "                                                           lexicographical\n"
  + "                                                           ly, in\n"
  + "                                                           ascending order\n"
  + "                                                           (Default:\n"
  + "                                                           false)\n"
  + "    --serialize <PATH>                                     Path to save\n"
  + "                                                           the serialized\n"
  + "                                                           dictionary\n"
  + "    --source-format <PROTOBUF|BYTECODE|PLAIN_TEXT>         Format of the\n"
  + "                                                           source\n"
  + "                                                           dictionary\n"
  + "                                                           (Default:\n"
  + "                                                           adaptive)\n"
  + "    --target-format <PROTOBUF|BYTECODE|PLAIN_TEXT>         Format of the\n"
  + "                                                           serialized\n"
  + "                                                           dictionary\n"
  + "                                                           (Default:\n"
  + "                                                           PROTOBUF)\n"
  + "\n"
  + "Example: liblevenshtein-java-cli \\\n"
  + "  --algorithm TRANSPOSITION \\\n"
  + "  --max-distance 2 \\\n"
  + "  --include-distance \\\n"
  + "  --query mispelled mispelling \\\n"
  + "  --colorize\n";

  /** Strips syntax coloring from output. */
  private static final Pattern RE_COLOR =
    Pattern.compile("\\e\\[(?:\\d+(?:;\\d+)*)?m");

  private static final Joiner SPACES = Joiner.on(" ");

  @Test
  public void testHelp() throws IOException, InterruptedException {
    assertThat(exec(false, false, false))
      .failed()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "-h"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--help"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--error"))
      .failed()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--query", "foo", "-h"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
    assertThat(exec(false, false, false, "--query", "foo", "--help"))
      .succeeded()
      .printed(HELP_TEXT)
        .toStandardOutput();
  }

  @SuppressWarnings("unchecked")
  @DataProvider(name="sourceToTargetProvider")
  public Object[][] sourceToTargetProvider() {
    try {
      final boolean[] booleans = {false, true};
      final boolean[] includeDistances = booleans;
      final boolean[] colorizes = booleans;
      final boolean[] isSorteds = booleans;
      final String[] algorithms = {"STANDARD", "TRANSPOSITION", "MERGE_AND_SPLIT"};
      final String[] formats = {"PLAIN_TEXT", "PROTOBUF", "BYTECODE"};

      final List<Object[]> provider =
        new ArrayList<>(
          includeDistances.length
          * colorizes.length
          * isSorteds.length
          * algorithms.length
          * formats.length
          * formats.length);

      for (final String algorithm : algorithms) {
        for (final boolean includeDistance : includeDistances) {
          final String output;

          switch (algorithm) {
            case "STANDARD":
              output = includeDistance
                ? STANDARD_OUTPUT_WITH_DISTANCES
                : STANDARD_OUTPUT_WITHOUT_DISTANCES;
              break;
            case "TRANSPOSITION":
              output = includeDistance
                ? TRANSPOSITION_OUTPUT_WITH_DISTANCES
                : TRANSPOSITION_OUTPUT_WITHOUT_DISTANCES;
              break;
            case "MERGE_AND_SPLIT":
              output = includeDistance
                ? MERGE_AND_SPLIT_OUTPUT_WITH_DISTANCES
                : MERGE_AND_SPLIT_OUTPUT_WITHOUT_DISTANCES;
              break;
            default:
              final String message =
                String.format("Unrecognized algorithm [%s]", algorithm);
              throw new IllegalArgumentException(message);
          }

          for (final boolean isSorted : isSorteds) {
            for (final boolean colorize : colorizes) {
              for (final String sourceFormat : formats) {
                for (final String targetFormat : formats) {
                  provider.add(new Object[] {
                    isSorted,
                    algorithm,
                    includeDistance,
                    output,
                    colorize,
                    sourceFormat,
                    targetFormat,
                  });
                }
              }
            }
          }
        }
      }

      return provider.toArray(new Object[0][0]);
    }
    catch (final Throwable thrown) {
      log.error("Failed to generate sourceToTargetProvider", thrown);
      throw thrown;
    }
  }

  @Test(dataProvider="sourceToTargetProvider")
  public void testWithDistanceAndDictionaryFormatAndConversionFormat(
      final boolean isSorted,
      final String algorithm,
      final boolean includeDistance,
      final String output,
      final boolean colorize,
      final String dictionaryFormat,
      final String conversionFormat)
      throws IOException, InterruptedException {

      // add option for sorted

    final Path dictionaryPath = tmp("dictionary-", "." + dictionaryFormat);
    final Path conversionPath = tmp("dictionary-", "." + conversionFormat);

    try {
      ProcessAssertions assertions;

      // Pull the dictionary from the Jar
      assertThat(exec(
            isSorted,
            includeDistance,
            colorize,
            "--serialize", dictionaryPath,
            "--target-format", dictionaryFormat))
        .succeeded();

      // Convert the dictionary to some other format
      assertions =
        assertThat(
          exec(
            isSorted,
            includeDistance,
            colorize,
            "--dictionary", dictionaryPath,
            "--source-format", dictionaryFormat,
            "--serialize", conversionPath,
            "--target-format", conversionFormat));

      if (isSorted && "PLAIN_TEXT".equals(dictionaryFormat)) {
        assertions.failed();
        return; // the dictionary was not converted, so don't proceed
      }
      else {
        assertions.succeeded();
      }

      // Test formatted parsing

      assertions =
        assertThat(
          exec(
            isSorted,
            includeDistance,
            colorize,
            "--dictionary", conversionPath,
            "--source-format", conversionFormat,
            "--algorithm", algorithm,
            "--query", "mispleling", "distnace"));

      if (isSorted && "PLAIN_TEXT".equals(conversionFormat)) {
        assertions.failed();
      }
      else {
        assertions.succeeded()
          .printed(output)
            .stripping(RE_COLOR)
            .toStandardOutput();
      }

      // Test adaptive parsing

      assertions =
        assertThat(
          exec(
            isSorted,
            includeDistance,
            colorize,
            "--dictionary", conversionPath,
            "--algorithm", algorithm,
            "--query", "mispleling", "distnace"));

      if (isSorted && "PLAIN_TEXT".equals(conversionFormat)) {
        assertions.failed();
      }
      else {
        assertions.succeeded()
          .printed(output)
            .stripping(RE_COLOR)
            .toStandardOutput();
      }
    }
    finally {
      Files.delete(conversionPath);
      Files.delete(dictionaryPath);
    }
  }

  private Path tmp(final String prefix, final String suffix) throws IOException {
    final Path tmp = Files.createTempFile(prefix, suffix);
    tmp.toFile().deleteOnExit();
    return tmp;
  }

  private Process exec(
      final boolean isSorted,
      final boolean includeDistance,
      final boolean colorize,
      final Object... args) throws IOException, InterruptedException {
    final List<String> command = new LinkedList<>();
    command.add(
      String.format(
        "%s/build/install/liblevenshtein-java-cli/bin/liblevenshtein-java-cli",
        System.getProperty("user.dir")));
    for (final Object arg : args) {
      command.add(arg.toString());
    }
    if (isSorted) {
      command.add("--is-sorted");
    }
    if (includeDistance) {
      command.add("--include-distance");
    }
    if (colorize) {
      command.add("--colorize");
    }
    final ProcessBuilder builder = new ProcessBuilder(command);
    final Map<String, String> env = builder.environment();
    env.put("LIBLEVENSHTEIN_JAVA_CLI_OPTS", "-DLOGGING=OFF");
    log.info("Executing [{}]", SPACES.join(builder.command()));
    final Process proc = builder.start();
    proc.waitFor();
    return proc;
  }
}
