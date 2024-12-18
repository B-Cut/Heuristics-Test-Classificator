package cael.uff;
/*
    This program goal is to classify the tests contained in a given repository in
        - Unit Tests
        - Integration Tests
        - System Tests
*/

import cael.uff.classification.FunctionInfo;
import cael.uff.classification.framework.FrameworkClassifier;
import cael.uff.classification.heuristics.FilepathClassifier;
import cael.uff.classification.heuristics.FunctionClassifier;
import cael.uff.classification.Result;
import cael.uff.classification.TestPhases;
import cael.uff.classification.heuristics.HeuristicsClassifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class Main {
    private final static String testFolderName = "test";

    // TODO: Proper argument parsing
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(
                    "Usage: java -jar HeuristicsClassificator.jar <path-to-repository> <keyword-json> <libraries-json>"
            );
            System.exit(1);
        }

        Path repoPath = Paths.get(args[0]);
        Path keywordPath = Paths.get(args[1]);
        Path librariesPath = Paths.get(args[2]);

        List<Path> testFolders;


        ProjectInfo.INSTANCE.setProjectPath(repoPath.toString());

        try{
            testFolders = Finders.testFoldersInRepo(repoPath, testFolderName);
            ProjectInfo.INSTANCE.setTestDirs(testFolders);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        System.out.println("Started heuristics classsifier for : " + repoPath.getFileName());
        HeuristicsClassifier heuristicsClassifier = new HeuristicsClassifier(keywordPath);
        testFolders.forEach(heuristicsClassifier::classify);

        System.out.println("Started framework classsifier for : " + repoPath.getFileName());
        FrameworkClassifier frameworkClassifier = new FrameworkClassifier(librariesPath);
        frameworkClassifier.classify();

        ObjectMapper mapper = new ObjectMapper();

        try {
            Path resultPath = Path.of(System.getProperty("user.dir")).resolve("results");
            Path resultFilePath = resultPath.resolve(repoPath.getFileName().toString() + "-heuristics-results.json");
            File resultFile = new File(resultFilePath.toString());
            mapper.writeValue(resultFile, heuristicsClassifier.getResults());
            System.out.println("Heuristics results written to " + resultFile.getAbsolutePath());

            resultFilePath = resultPath.resolve(repoPath.getFileName().toString() + "-framework-results.json");
            resultFile = new File(resultFilePath.toString());
            mapper.writeValue(resultFile, frameworkClassifier.getResults());
            System.out.println("Framework results written to " + resultFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}