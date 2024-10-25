package cael.uff.classification;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FilepathClassifier {
    public final String unitKeyword = "unit";
    public final String integrationKeyword= "integration";
    public final String[] systemKeywords = { "system", "E2E", "application" };

    private boolean containsCaseInsensitive(String base, String query){
        return base.toLowerCase().contains(query.toLowerCase());
    }

    private boolean containsCaseInsensitive(String base, String[] comparators){
        for (String comparator : comparators){
            if(base.toLowerCase().contains(comparator.toLowerCase())){
                return true;
            }
        }
        return false;
    }

    public List<Path> subdirectoriesWithKeyword(Path root, String keyword){
        ArrayList<Path> subdirectories = new ArrayList<>();

        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult postVisitDirectory(Path directory, IOException e){
                if(directory == root){
                    return FileVisitResult.TERMINATE;
                }
                if(containsCaseInsensitive(directory.getFileName().toString(), keyword)){
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
                    if(containsCaseInsensitive(directory.getFileName().toString(), keyword) && !subdirectories.contains(directory)){
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

                if ( containsCaseInsensitive(name, unitKeyword) ){
                    res.unitFolders.add(directory);
                    return FileVisitResult.SKIP_SUBTREE;
                } else if (containsCaseInsensitive(name, integrationKeyword)){
                    res.integrationFolders.add(directory);
                    return FileVisitResult.SKIP_SUBTREE;
                } else if (containsCaseInsensitive( name, systemKeywords)){
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

        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr){
                String name = file.getFileName().toString();

                if(!name.endsWith(fileExtension)){
                    return FileVisitResult.CONTINUE;
                }

                if ( containsCaseInsensitive(name, unitKeyword) ){
                    res.unitFiles.add(file);
                } else if (containsCaseInsensitive(name, integrationKeyword)){
                    res.integrationFiles.add(file);
                } else if (containsCaseInsensitive( name, systemKeywords)){
                    res.systemFiles.add(file);
                } else{
                    res.unclassifiedFiles.add(file);
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
}
