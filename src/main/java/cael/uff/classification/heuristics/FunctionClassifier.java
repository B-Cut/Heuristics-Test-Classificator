package cael.uff.classification.heuristics;

import cael.uff.Utils;
import cael.uff.classification.FunctionInfo;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.chain.CtQueryable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class FunctionClassifier extends Classifier {

    public HashMap<String, List<FunctionInfo>> classifiedFunctions = new HashMap<>();

    public FunctionClassifier(Path keywordFile) {
        super(keywordFile);
        for ( KeywordsInfo i : this.keywords) {
            classifiedFunctions.put(i.phase(), new ArrayList<FunctionInfo>());
        }
        classifiedFunctions.put(undefinedKeyword, new ArrayList<>());
    }

    private CtQueryable getClassesFromFile(Path path){
        SpoonAPI spoon = new Launcher();
        // Makes sure we have a proper file
        spoon.addInputResource(path.toAbsolutePath().toString());
        CtModel model = spoon.getModel();
        return model.filterChildren((el) -> el instanceof CtClass<?>);
    }

    public void massClassifyFunctionOnClassifiedFiles(HashMap<String, List<Path>> classifiedFiles){
        for ( String phase : classifiedFiles.keySet()){
            // We don't care about the unclassified files
            if (phase.equals(this.undefinedKeyword)){
                continue;
            }

            for( Path path : classifiedFiles.get(phase)){
                massClassifyFunctionOnFile(path, phase);
            }
        }
    }

    public void massClassifyFunctionOnFile(Path file, String phase){
        SpoonAPI spoon = new Launcher();
        spoon.addInputResource(file.toAbsolutePath().toString());
        CtModel model = spoon.buildModel();

        model.filterChildren((el) -> el instanceof CtClass<?>).forEach((CtClass<?> ctClass) -> {
            ctClass.filterChildren((el) -> el instanceof CtMethod).forEach((CtMethod<?>  ctMethod) -> {
                classifiedFunctions.get(phase).add( new FunctionInfo(ctMethod.getSimpleName(), file.normalize()));
            });
        });
    }

    public void massClassifyFunctionOnClassifiedDirectories(HashMap<String, List<Path>> classifiedDirectories){
        String fileExtension = ".java";
        // Walk through every subtree and classify any files it finds to folder classification
        for ( String phase : classifiedDirectories.keySet()){
            if (phase.equals(this.undefinedKeyword)){
                continue;
            }

            for (Path dir : classifiedDirectories.get(phase)){
                SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attr){
                        String name = file.getFileName().toString();
                        if (name.endsWith(fileExtension)) {
                            massClassifyFunctionOnFile(file, phase);
                        }
                        return  FileVisitResult.CONTINUE;
                    }
                    // We don't want to walk down undefined folders
                    @Override
                    public  FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                        if( dir.getParent() == dir ){
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                        return FileVisitResult.CONTINUE;
                    }
                };
                try{
                    Files.walkFileTree(dir, visitor);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void classifyFunctionsInFile(Path file){
        SpoonAPI spoon = new Launcher();
        spoon.addInputResource(file.toString());
        CtModel model = spoon.buildModel();
        // first we try to check them by name, if we can't, check by comments
        model.filterChildren((el) -> el instanceof CtClass<?>).forEach((CtClass<?> ctClass) -> {
            ctClass.filterChildren((el) -> el instanceof CtMethod<?>).forEach((CtMethod<?> method) -> {
                for ( KeywordsInfo i : this.keywords){
                    if ( Utils.containsCaseInsensitive(method.getSimpleName(), i.keywords().toArray(new String[]{}))){
                        classifiedFunctions.get(i.phase()).add( new FunctionInfo(method.getSimpleName(), file.normalize()));
                        return;
                    }
                }

                classifyFunctionByComment(method, file);
               });
        });
    }
    public void classifyFunctionByComment(CtMethod<?> method, Path file) {
        List<CtComment> comments = method.getComments();
        for (CtComment comment : comments) {
            for ( KeywordsInfo i : this.keywords){
                if ( i.phase().equals(undefinedKeyword) ){
                    continue;
                }

                if (Utils.containsCaseInsensitive(comment.getContent(), i.keywords().toArray(new String[]{}))){
                    classifiedFunctions.get(i.phase()).add( new FunctionInfo(method.getSimpleName(), file.normalize()));
                }
            }
        }

        classifiedFunctions.get(undefinedKeyword).add(new FunctionInfo(method.getSimpleName(), file.normalize()));
    }
}
