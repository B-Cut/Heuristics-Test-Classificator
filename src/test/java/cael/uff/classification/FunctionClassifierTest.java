package cael.uff.classification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

class FunctionClassifierTest {
    @Test
    void massClassifyFunctionOnFile() {
        Result result = new Result();
        FunctionClassifier.massClassifyFunctionOnFile(
                Paths.get( // Using full path temporally
                        "/home/nyx/Faculdade/ClassificadorJava/HeuristicsClassificator/src/test/resources/FunctionClassifierBase.java"
                )
                , TestPhases.UNDEFINED
                , result
        );
        assertEquals(2, result.unclassifiedFunctions.size());
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