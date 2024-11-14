package cael.uff;
/*
    This program goal is to classify the tests contained in a given repository in
        - Unit Tests
        - Integration Tests
        - System Tests
*/

import cael.uff.classification.heuristics.FilepathClassifier;
import cael.uff.classification.heuristics.FunctionClassifier;
import cael.uff.classification.Result;
import cael.uff.classification.TestPhases;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    private final static String testFolderName = "test";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java -jar heuristics-classificator.jar <path-to-repository>");
        }

        Path repoPath = Paths.get(args[0]);

        List<Path> testFolders;

        try{
            testFolders = Finders.testFoldersInRepo(repoPath, testFolderName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Result res = new Result();

        // There are folders labeled as "test" in repository
        if(!testFolders.isEmpty()){
            for(Path testFolder : testFolders){
                FilepathClassifier classifier = new FilepathClassifier();
                classifier.classifyFiles(testFolder, res);
            }

            // First we classify the tests based on the identified file paths

            // Not proper but works
            for(Path unitFolder : res.unitFolders){
                FunctionClassifier.massClassifyFunctionOnDirectory(unitFolder, TestPhases.UNIT, res);
            }
            for(Path integrationFolder : res.integrationFolders){
                FunctionClassifier.massClassifyFunctionOnDirectory(integrationFolder, TestPhases.INTEGRATION, res);
            }
            for(Path systemFolders : res.unitFolders){
                FunctionClassifier.massClassifyFunctionOnDirectory(systemFolders, TestPhases.SYSTEM, res);
            }

            for(Path unitFiles : res.unitFiles){
                FunctionClassifier.massClassifyFunctionOnFile(unitFiles, TestPhases.UNIT, res);
            }
            for(Path integrationFiles : res.integrationFiles){
                FunctionClassifier.massClassifyFunctionOnFile(integrationFiles, TestPhases.INTEGRATION, res);
            }
            for(Path systemFiles : res.systemFiles){
                FunctionClassifier.massClassifyFunctionOnFile(systemFiles, TestPhases.SYSTEM, res);
            }

            // Then, we have to work with the unclassified files
            for(Path unclassifiedFiles : res.unclassifiedFiles){
                FunctionClassifier.classifyFunctionsInFile(unclassifiedFiles, res);
            }
        }
    }

    // TODO: Serialize result
}