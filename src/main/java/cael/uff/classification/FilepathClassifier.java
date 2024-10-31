package cael.uff.classification;

import cael.uff.Finders;
import cael.uff.Utils;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FilepathClassifier {
    public final String unitKeyword = "unit";
    public final String integrationKeyword= "integration";
    public final String[] systemKeywords = { "system", "E2E", "application" };



    public List<Path> subdirectoriesWithKeyword(Path root, String keyword){
        ArrayList<Path> subdirectories = new ArrayList<>();

        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException e){
                if(directory == root){
                    return FileVisitResult.TERMINATE;
                }
                if(Utils.containsCaseInsensitive(directory.getFileName().toString(), keyword)){
                    subdirectories.add(directory);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try{
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return subdirectories;
    }

    public List<Path> subdirectoriesWithKeyword(Path root, String[] keywords){
        ArrayList<Path> subdirectories = new ArrayList<>();

        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException e){
                if(directory == root){
                    return FileVisitResult.TERMINATE;
                }
                for( String keyword : keywords){
                    if(Utils.containsCaseInsensitive(directory.getFileName().toString(), keyword) && !subdirectories.contains(directory)){
                        subdirectories.add(directory);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try{
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return subdirectories;
    }


    /** Classifies the subfolders of a given path according to the keywords associated with each test phase. The results
     * are stored in the appropriate property of the <c>Result</c> class.
     * @param root The path containing the subfolders to be classified. Is included in the checks.
     */
    public void classifyFolders(Path root, Result res){
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attr){
                String name = directory.getFileName().toString();

                if ( Utils.containsCaseInsensitive(name, unitKeyword) ){
                    res.unitFolders.add(directory);
                    return FileVisitResult.SKIP_SUBTREE;
                } else if (Utils.containsCaseInsensitive(name, integrationKeyword)){
                    res.integrationFolders.add(directory);
                    return FileVisitResult.SKIP_SUBTREE;
                } else if (Utils.containsCaseInsensitive( name, systemKeywords)){
                    res.systemFolders.add(directory);
                    return FileVisitResult.SKIP_SUBTREE;
                } else{
                    res.unclassifiedFolders.add(directory);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try{
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Classifies the files of a given path according to the keywords associated with each test phase. The
     * classification is limited to .java files. The results
     * are stored in the appropriate property of the <c>Result</c> class.
     * @param root The path containing the subfolders to be classified. Is included in the checks.
     */
    public void classifyFiles(Path root, Result res){
        String fileExtension = ".java";

        /*SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr){
                String name = file.getFileName().toString();

                if(!name.endsWith(fileExtension)){
                    return FileVisitResult.CONTINUE;
                }

                if ( Utils.containsCaseInsensitive(name, unitKeyword) ){
                    res.unitFiles.add(file);
                } else if (Utils.containsCaseInsensitive(name, integrationKeyword)){
                    res.integrationFiles.add(file);
                } else if (Utils.containsCaseInsensitive( name, systemKeywords)){
                    res.systemFiles.add(file);
                } else{
                    res.unclassifiedFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            // We don't want to go into subfolders since they will be included in the unclassifield folders in Result
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.SKIP_SUBTREE;
            }
        };
        try{
            Files.walkFileTree(root, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }*/try{
            res.unitFiles = Finders.filesContainingSubstring(root, unitKeyword)
                    .stream().filter((Path path) -> path.getFileName().toString().endsWith(fileExtension)).toList();
            res.integrationFiles = Finders.filesContainingSubstring(root, integrationKeyword)
                    .stream().filter((Path path) -> path.getFileName().toString().endsWith(fileExtension)).toList();
            res.systemFiles = Finders.filesContainingSubstring(root, systemKeywords)
                    .stream().filter((Path path) -> path.getFileName().toString().endsWith(fileExtension)).toList();

            res.unclassifiedFiles = Files.find(root, 1, (path, basicFileAttributes) -> {
                        return
                                !res.unitFiles.contains(path)
                                        && !res.integrationFiles.contains(path)
                                        && !res.systemFiles.contains(path)
                                        && !Files.isDirectory(path)
                                        && path.getFileName().toString().endsWith(fileExtension);
                    }).toList();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
