package cael.uff;
/*
    This program goal is to classify the tests contained in a given repository in
        - Unit Tests
        - Integration Tests
        - System Tests
*/


import cael.uff.classification.analytic.AnalyticClassifier;
import cael.uff.ProjectInfo;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class Main {
    private final static String testFolderName = "test";

    // TODO: Proper argument parsing
    public static void main(String[] args) {
        /*if (args.length != 3) {
            System.out.println(
                    "Usage: java -jar HeuristicsClassificator.jar <path-to-repository> <keyword-json> <libraries-json>"
            );
            System.exit(1);
        }*/

        if (args.length == 0 || args.length > 2) {
            System.out.println("Usage: java -jar HeuristicsClassificator.jar <path-to-repository>");
            System.out.println("To analyse gradlew packages, use the '-g' flag");
            System.exit(1);
        }
        boolean useGradle = false;
        Path repoPath = null;

        if (args.length == 2) {
            if (args[1].equals("-g")) {
                useGradle = true;
                repoPath = Paths.get(args[0]);
            } else if (args[0].equals("-g")) {
                useGradle = true;
                repoPath = Paths.get(args[1]);
            } else {
                System.err.println("Invalid arguments");
                System.exit(2);
            }
        } else {
            repoPath = Paths.get(args[0]);
        }
        /*Path keywordPath = Paths.get(args[1]);
        Path librariesPath = Paths.get(args[2]);*/

        List<Path> testFolders;


        assert repoPath != null;
        ProjectInfo.INSTANCE.setProjectPath(repoPath.toString());

        AnalyticClassifier classifier = new AnalyticClassifier();
        classifier.startClassification(useGradle);

        classifier.dumpResults(Path.of("./results_dump.txt"));

    }
}