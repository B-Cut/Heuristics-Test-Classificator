package cael.uff;

import java.nio.file.Files;
import java.nio.file.Path;

public class TestUtils 
{
    public static void createDirectories(Path root, String newDir){
        try {
            Files.createDirectories(root.resolve(newDir));
        } catch (Exception e) {
            System.err.println("Error creating temporary directory in: " + root.toAbsolutePath());
            System.err.println(e.getMessage());
        }
    }

    public static void createFiles(Path root, String newFile){
        try {
            Files.createFile(root.resolve(newFile));
        } catch (Exception e) {
            System.err.println("Error creating temporary directory in: " + root.toAbsolutePath());
            System.err.println(e.getMessage());
        }
    }
}
