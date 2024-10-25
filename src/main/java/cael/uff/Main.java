package cael.uff;
/*
    This program goal is to classify the tests contained in a given repository in
        - Unit Tests
        - Integration Tests
        - System Tests
*/

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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

        // There are folders labeled as "test" in repository
        if(!testFolders.isEmpty()){
            List<Path> unitFolders = new ArrayList<>();
            List<Path> integrationFolders = new ArrayList<>();
            List<Path> systemFolders = new ArrayList<>();

            for(Path folder : testFolders){
                try {
                    unitFolders.addAll(Finders.subfoldersWithString(folder, "unit"));
                    integrationFolders.addAll(Finders.subfoldersWithString(folder, "integration"));
                    systemFolders.addAll(Finders.subfoldersWithString(folder, new String[]{"application", "system"}));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }


    }
}