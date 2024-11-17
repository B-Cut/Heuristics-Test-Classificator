package cael.uff;
/*
    This program goal is to classify the tests contained in a given repository in
        - Unit Tests
        - Integration Tests
        - System Tests
*/

import cael.uff.classification.framework.FrameworkClassifier;
import cael.uff.classification.heuristics.FilepathClassifier;
import cael.uff.classification.heuristics.FunctionClassifier;
import cael.uff.classification.Result;
import cael.uff.classification.TestPhases;
import cael.uff.classification.heuristics.HeuristicsClassifier;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private final static String testFolderName = "test";

    // TODO: Proper argument parsing
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println(
                    "Usage: java -jar tests-classificator.jar <path-to-repository> <keyword-json> <libraries-json>"
            );
        }

        Path repoPath = Paths.get(args[0]);
        Path keywordPath = Paths.get(args[1]);
        Path librariesPath = Paths.get(args[2]);

        List<Path> testFolders;

        try{
            testFolders = Finders.testFoldersInRepo(repoPath, testFolderName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        HeuristicsClassifier heuristicsClassifier = new HeuristicsClassifier(keywordPath);

        testFolders.forEach(heuristicsClassifier::classify);

        FrameworkClassifier frameworkClassifier = new FrameworkClassifier(librariesPath);
        testFolders.forEach(frameworkClassifier::classify);
    }

    // TODO: Serialize result
}