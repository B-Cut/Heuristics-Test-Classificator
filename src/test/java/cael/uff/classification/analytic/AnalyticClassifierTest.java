package cael.uff.classification.analytic;

import cael.uff.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticClassifierTest {

    @Test
    void shouldReturnSmallerPath() {
        String big = "java.test.package";
        String small = "java.test";

        String result = Utils.commonPackagePath(small, big);

        assertEquals(small, result);
    }
}