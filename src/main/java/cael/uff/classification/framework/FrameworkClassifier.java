package cael.uff.classification.framework;

import cael.uff.classification.FunctionInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/***
 * This class contains the necessary methods to classify a given test via the frameworks specified.
 */
public class FrameworkClassifier {
    private ArrayList<PhaseInfo> phases;
    private Map<String, List<FunctionInfo>> results;
    private final String undefinedPhase = "undefined";

    private final String fileExtension = ".java";
    /***
     * Instantiates a <c>FrameworkClassifier</c> with a given libraries file. <c>libJson</c> must point to a JSON file
     * that containing a dictionary that has the classification type as a key and the value must be a list of libraries
     * corresponding to that classification.
     * @param libsJson
     */
    public FrameworkClassifier(Path libsJson){
        if (!Files.exists(libsJson)){
            System.err.println("The libs json file does not exist.");
            System.exit(1);
        }

        ObjectMapper mapper = new ObjectMapper();
        try{
            this.phases = mapper.readValue(new File(libsJson.toAbsolutePath().toString()), new TypeReference<ArrayList<PhaseInfo>>(){});
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        this.results = new HashMap<>();
        for(PhaseInfo p : this.phases){
            this.results.put(p.phase(), new ArrayList<>());
        }
        this.results.put(undefinedPhase, new ArrayList<>());
    }

    public void classify(Path root){
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attr){
                if (file.toString().endsWith(fileExtension)){
                    classifyFile(file);
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

    public void classifyFile(Path file){
        SpoonAPI spoon = new Launcher();
        spoon.addInputResource(file.toString());
        CtModel model = spoon.buildModel();

        model.filterChildren((el) -> el instanceof CtClass).forEach((CtClass<?> ctClass) -> {
            ctClass.filterChildren((el) -> el instanceof CtMethod<?>).forEach((CtMethod<?> ctMethod) -> {
                AtomicReference<String> phase = new AtomicReference<>(this.undefinedPhase);
                AtomicInteger priority = new AtomicInteger();
                ctMethod.filterChildren((el) -> el instanceof CtInvocation).forEach((CtInvocation<?> ctInvocation) -> {
                    for( PhaseInfo temp_phase : phases){
                        for ( String lib : temp_phase.libraries() ){
                            if(ctInvocation.getExecutable().getDeclaringType().toString().contains(lib)){
                                if ( temp_phase.priority() > priority.get()){
                                    priority.set(temp_phase.priority());
                                    phase.set(temp_phase.phase());
                                }
                            }
                        }
                    }
                });
                results.get(phase.get()).add(new FunctionInfo(ctMethod.getSimpleName(), file));
            });
        });
    }

    public Map<String, List<FunctionInfo>> getResults() {
        return results;
    }
}
