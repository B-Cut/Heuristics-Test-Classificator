package cael.uff.classification;

import cael.uff.TestUtils;
import cael.uff.classification.heuristics.FilepathClassifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilepathClassifierTest {

    @TempDir
    Path tempDir;


    @Test
    void subdirectoriesWithKeywordSingle() {
        String[] testDirs = new String[]{
                "path_a/test",
                "path_b/tests",
                "path_c/TesT",
                "SomeTesting/test",
        };

        for(String testDir : testDirs){
            TestUtils.createDirectories(tempDir, testDir);
        }

        TestUtils.createFiles(tempDir, "test.txt");

        FilepathClassifier filepathClassifier = new FilepathClassifier();

        List<Path> result = filepathClassifier.subdirectoriesWithKeyword(tempDir, "test");

        assertEquals(5, result.size() );
    }

    @Test
    void subdirectoriesWithKeywordArray() {
        String[] testDirs = new String[]{
                "path_a/inTegrAtion",
                "path_b/unit_tests",
                "path_c/systems",
                "E2E/unit",
        };
        for(String testDir : testDirs){
            TestUtils.createDirectories(tempDir, testDir);
        }

        TestUtils.createFiles(tempDir, "test.txt");

        FilepathClassifier filepathClassifier = new FilepathClassifier();

        List<Path> result = filepathClassifier.subdirectoriesWithKeyword(tempDir, new String[]{
                "integration",
                "unit",
                "system",
                "e2e"
        });

        assertEquals(5, result.size() );
    }

    @Test
    void classifyFolders() {
        String[] testDirs = new String[]{
                "path_a/inTegrAtion",
                "path_b/unit_tests",
                "path_c/systems",
                "E2E/unit",
                "something"
        };

        TestUtils.createDirectories(tempDir, "test");

        Result result = new Result();

        Path root = tempDir.resolve("test");
        for(String testDir : testDirs){
            TestUtils.createDirectories(root, testDir);
        }

        FilepathClassifier filepathClassifier = new FilepathClassifier();
        filepathClassifier.classifyFolders(root, result);
        // The E2E/unit folder should not be included as unit folder
        assertEquals(1, result.unitFolders.size());
        assertEquals(1, result.integrationFolders.size());
        assertEquals(2, result.systemFolders.size());
        // path_a, path_b, path_c and the root "test" folder should be marked as unclassified
        assertEquals(5, result.unclassifiedFolders.size());
    }

    @Test
    void classifyFiles(){
        String[] testDirs = new String[]{
                "path_a/path_b"
        };

        String[] testFiles = new String[]{
                "system.txt",
                "integration.java",
                "ServerUnitTest.java",
                "some_test.java",
                "ServerE2e.java",
                "SystemTest.java",
                "path_a/unit.java"
        };

        int numUnitClasses = 1;
        int numIntegrationClasses = 1;
        int numSystemClasses = 2;
        int numUnclassifiedClasses = 1;


        TestUtils.createDirectories(tempDir, "test");
        Result result = new Result();

        Path root = tempDir.resolve("test");
        for(String testDir : testDirs){
            TestUtils.createDirectories(root, testDir);
        }

        for (String testFile : testFiles){
            TestUtils.createFiles(root, testFile);
        }
        FilepathClassifier filepathClassifier = new FilepathClassifier();
        filepathClassifier.classifyFiles(root, result);
        assertEquals(numUnitClasses, result.unitFiles.size());
        assertEquals(numIntegrationClasses, result.integrationFiles.size());
        assertEquals(numSystemClasses, result.systemFiles.size());
        assertEquals(numUnclassifiedClasses, result.unclassifiedFiles.size());
    }
}