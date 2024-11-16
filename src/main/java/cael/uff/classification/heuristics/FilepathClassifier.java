package cael.uff.classification.heuristics;

import cael.uff.Finders;
import cael.uff.Utils;
import cael.uff.classification.Result;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FilepathClassifier extends HeuristicsClassifier{
    public HashMap<String, List<Path>> directories = new HashMap<>();
    public HashMap<String, List<Path>> files = new HashMap<>();

    FilepathClassifier(Path keywordFile) {
        super(keywordFile);

        for( KeywordsInfo info : keywords ){
            directories.put(info.phase(), new ArrayList<>());
            files.put(info.phase(), new ArrayList<>());
        }
        directories.put(undefinedKeyword, new ArrayList<>());
        files.put(undefinedKeyword, new ArrayList<>());
    }

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

                for ( KeywordsInfo info : keywords){
                    if ( Utils.containsCaseInsensitive(name, info.keywords().toArray(new String[]{}))){
                        directories.get(info.phase()).add(directory);
                        return FileVisitResult.SKIP_SUBTREE;
                    }
                }

                directories.get(undefinedKeyword).add(directory);
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
        try{

            for( KeywordsInfo info : keywords ){
                for ( String kw : info.keywords() ){
                    files.get(info.phase()).addAll(
                            Finders.filesContainingSubstring(root, kw)
                                    .stream().filter((Path path) -> path.getFileName().toString().endsWith(fileExtension)).toList()
                    );
                }
            }

            files.get(undefinedKeyword).addAll(Files.find(root, 1, (path, basicFileAttributes) -> {
                for (List<Path> foundFiles : files.values()) {
                    if (foundFiles.contains(path)) {
                        return false;
                    }
                }
                return true;
            }).toList());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }
}
