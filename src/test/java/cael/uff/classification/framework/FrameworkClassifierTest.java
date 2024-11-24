package cael.uff.classification.framework;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FrameworkClassifierTest {


    @BeforeAll
    static void setUp() {

    }

    // TODO: Update this test
    @Test
    void classify() {
        FrameworkClassifier classifier = new FrameworkClassifier(
                Path.of("src/test/resources/testLibs.json")
        );

        classifier.classify();

        int totalFunctionFound = 4;
        int oneFunctionFound = 1;
        int twoFunctionFound = 1;

        assertEquals(oneFunctionFound, classifier.getResults().get("one").size());
        assertEquals(twoFunctionFound, classifier.getResults().get("two").size());
    }

    @Test
    void classifyFile() {

    }
}