package cael.uff.classification;

import cael.uff.classification.heuristics.FunctionClassifier;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

// TODO: Redo tests
class FunctionClassifierTest {
    @Test
    void massClassifyFunctionOnFile() {
        Result result = new Result();
        FunctionClassifier functionClassifier = new FunctionClassifier(Path.of(""));
        functionClassifier.massClassifyFunctionOnFile(
                Paths.get( // Using full path temporally
                        "/home/nyx/Faculdade/ClassificadorJava/HeuristicsClassificator/src/test/resources/FunctionClassifierBase.java"
                )
                , "undefined"
        );
        int numberOfUnclassifiedFunctions = 2;

        assertEquals(numberOfUnclassifiedFunctions, result.unclassifiedFunctions.size());
        assertEquals(0, result.unitFunctions.size());
        assertEquals(0, result.integrationFunctions.size());
        assertEquals(0, result.systemFunctions.size());
    }

    @Test
    void massClassifyFunctionOnDirectory() {
    }

    @Test
    void classifyFunctionByName() {
    }

    @Test
    void classifyFunctionByComment() {
    }
}