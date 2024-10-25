package cael.uff;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class FindersTest {
    @TempDir
    Path tempDir;

    @Test
    void testFoldersInRepo() {
        TestUtils.createDirectories(tempDir, "path_a/test");
        TestUtils.createDirectories(tempDir, "path_b/tests");

        List<Path> result = new ArrayList<>();

        try {
            result = Finders.testFoldersInRepo(tempDir, "test");
        } catch (IOException e) { System.err.println(e.getMessage()); }

        assertEquals(2, result.size());
    }

    @Test
    void subfoldersWithStringCapitalization(){
        TestUtils.createDirectories(tempDir, "path_a/test");
        TestUtils.createDirectories(tempDir, "path_b/TesTs");

        List<Path> result = new ArrayList<>();
        try {
            result = Finders.subfoldersWithString(tempDir, "test");
        } catch (IOException e) { System.err.println(e.getMessage()); }

        assertEquals(2, result.size());
    }

    @Test
    void subfoldersWithStringMultipleKeywords(){
        TestUtils.createDirectories(tempDir, "path_c/something");
        TestUtils.createDirectories(tempDir, "path_c/another");

        List<Path> result = new ArrayList<>();
        try {
            result = Finders.subfoldersWithString(tempDir, new String[]{"something", "another"});
        } catch (IOException e) { System.err.println(e.getMessage()); }
        assertEquals(2, result.size());
    }
}