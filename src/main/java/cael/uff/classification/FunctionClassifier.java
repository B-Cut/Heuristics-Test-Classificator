package cael.uff.classification;

import cael.uff.Utils;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.chain.CtFunction;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;


public class FunctionClassifier {

    private static CtQueryable getClassesFromFile(Path path){
        SpoonAPI spoon = new Launcher();
        // Makes sure we have a proper file
        spoon.addInputResource(path.toAbsolutePath().toString());
        CtModel model = spoon.getModel();
        return model.filterChildren((el) -> el instanceof CtClass<?>);
    }

    public static void massClassifyFunctionOnFile(Path file, TestPhases classification, Result result){
        SpoonAPI spoon = new Launcher();
        // Makes sure we have a proper file
        if(!Files.exists(file)){
            throw new RuntimeException("File not found: " + file.toAbsolutePath());
        }
        spoon.addInputResource(file.toString());
        CtModel model = spoon.buildModel();
        model.filterChildren((el) -> el instanceof CtClass<?>)
                .forEach((CtClass<?> ctClass) -> {
                    ctClass.filterChildren((el) -> el instanceof CtMethod<?>)
                            .forEach((CtMethod<?> method) -> {
                                switch (classification){
                                    case UNIT -> result.unitFunctions.add(new FunctionInfo(method.getSimpleName(), file));
                                    case INTEGRATION -> result.integrationFunctions.add(new FunctionInfo(method.getSimpleName(), file));
                                    case SYSTEM -> result.systemFunctions.add(new FunctionInfo(method.getSimpleName(), file));
                                    case UNDEFINED -> result.unclassifiedFunctions.add(new FunctionInfo(method.getSimpleName(), file));
                                    case null, default -> {
                                        return;
                                    }
                                }
                    });
                });
    }

    public static void massClassifyFunctionOnDirectory(Path directory, TestPhases classification, Result result){
        SpoonAPI spoon = new Launcher();
        String fileExtension = ".java";
        if(!Files.exists(directory)){
            throw new RuntimeException("Directory not found: " + directory.toAbsolutePath());
        }

        // Walk through every subtree and classify any files it finds
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr){
                String name = directory.getFileName().toString();
                if (name.endsWith(fileExtension)) {
                    massClassifyFunctionOnFile(file, classification, result);
                }

                return  FileVisitResult.CONTINUE;
            }
            // We don't want to walk down undefined folders
            @Override
            public  FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs){
                if(classification == TestPhases.UNDEFINED && dir.getParent() == directory ){
                    return FileVisitResult.SKIP_SUBTREE;
                }
                return FileVisitResult.CONTINUE;
            }
        };
        try{
            Files.walkFileTree(directory, visitor);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void classifyFunctionByName(){}
    public static void classifyFunctionByComment(){}
}
