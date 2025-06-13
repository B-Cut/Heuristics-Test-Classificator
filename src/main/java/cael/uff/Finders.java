package cael.uff;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.exit;

public abstract class Finders {
    // Assumes test folder name is in the singular.
    public static List<Path> testFoldersInRepo(Path repo, String testFolderName) throws IOException {
        List<Path> paths = new ArrayList<>();

        // Look for test folder or it's plural
        try(
                Stream<Path> pathStream = Files
                        .find(repo, Integer.MAX_VALUE, (path, basicFileAttributes) ->
                                path.getFileName().toString().equalsIgnoreCase(testFolderName)
                                        || path.getFileName().toString().equalsIgnoreCase(testFolderName+"s"))
        ){
            paths = pathStream.toList();
        }
        return paths;
    }

    public static List<Path> filesContainingSubstring(Path folder, String substring) throws IOException {
        List<Path> paths = new ArrayList<>();

        try(
                Stream<Path> pathStream = Files
                        .find(folder, 1, (path, basicFileAttributes) ->
                                Utils.containsCaseInsensitive(path.getFileName().toString(), substring))
        ){
            paths = pathStream.toList();
        }

        return paths;
    }

    public static ArrayList<Path> filesThatEqual(Path folder, String substring) throws IOException {
        ArrayList<Path> paths = new ArrayList<>();

        try(
                Stream<Path> pathStream = Files
                        .find(folder, 1, (path, basicFileAttributes) ->
                                basicFileAttributes.isRegularFile() && path.getFileName().toString().equalsIgnoreCase(substring))
        ){
            paths = (ArrayList<Path> ) pathStream.toList();
        }

        return paths;
    }

    public static ArrayList<Path> foldersContainingFile(Path root, String filename){
        ArrayList<Path> paths = new ArrayList<>();
        try(
                Stream<Path> pathStream = Files.find(root, Integer.MAX_VALUE, (path, basicFileAttributes) ->
                        path.getFileName().equals(filename) && basicFileAttributes.isRegularFile())
        ){
            paths.addAll(pathStream.map((path -> path = path.toAbsolutePath().normalize().getParent())).toList());
        } catch (Exception e) {
            System.err.println("Failed to get files from " + root);
            e.getMessage();
            exit(1);
        }

        return paths;
    }

    public static List<Path> filesContainingSubstring(Path folder, String[] substrings) throws IOException {
        List<Path> paths = new ArrayList<>();

        try(
                Stream<Path> pathStream = Files
                        .find(folder, 1, (path, basicFileAttributes) -> {
                            boolean contains = false;
                            for(String substring : substrings){
                                if( Utils.containsCaseInsensitive(path.getFileName().toString(), substring) ){
                                    contains = true;
                                }
                            }
                            return contains;
                        });

        ){
            paths = pathStream.toList();
        }

        return paths;
    }

    public static List<Path> subfoldersWithString(Path repo, String containedString) throws IOException {
        List<Path> paths = new ArrayList<>();

        // Checks if there are any subfolders that contain given string in their name
        try(
                Stream<Path> pathStream = Files
                        .find(repo, Integer.MAX_VALUE, (path, basicFileAttributes) ->
                                path
                                        .getFileName().toString().toLowerCase()
                                        .contains(containedString.toLowerCase()))
        ){
            paths = pathStream.toList();
        }
        return paths;
    }
    public static List<Path> subfoldersWithString(Path repo, String[] containedString) throws IOException {
        List<Path> paths = new ArrayList<>();

        // Checks if there are any subfolders that contain given string in their name for all strings
        for(String s : containedString) {
            try(
                    Stream<Path> pathStream = Files
                            .find(repo, Integer.MAX_VALUE, (path, basicFileAttributes) ->
                                    path
                                            .getFileName().toString().toLowerCase()
                                            .contains(s.toLowerCase()))
            ){
                paths.addAll(pathStream.toList());
            }
        }

        return paths;
    }
}
