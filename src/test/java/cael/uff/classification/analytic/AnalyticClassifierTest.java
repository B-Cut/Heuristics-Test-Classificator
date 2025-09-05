package cael.uff.classification.analytic;

import cael.uff.Utils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnalyticClassifierTest {

    @Test
    void shouldReturnSmallerPath() {
        String big = "java.test.package";
        String small = "java.test";

        String result = Utils.commonPackagePath("java", small, big);

        assertEquals(small, result);
    }

    @Test
    void shouldReturnEmpty(){
        String base = "java.test.package";
        String sameProject = "java.something.else";
        String differentProject = "project.not.this";

        assertEquals("", Utils.commonPackagePath("java", base, differentProject));
        assertEquals("", Utils.commonPackagePath("java", base, sameProject));
    }
}