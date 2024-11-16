package cael.uff.classification.framework;

import cael.uff.classification.Result;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtConstructor;
import spoon.reflect.declaration.CtMethod;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/***
 * This class contains the necessary methods to classify a given test via the frameworks specified.
 */
public class FrameworkClassifier {
    private Map<String, List<String>> libraries;
    private Map<String, List<String>> results;
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
            this.libraries = mapper.readValue(libsJson.toAbsolutePath().toString(), new TypeReference<Map<String, List<String>>>(){});
        } catch (Exception e){
            e.printStackTrace();
            System.exit(1);
        }
        this.results = new HashMap<>();
    }

    public void classifyFile(Path file){
        SpoonAPI spoon = new Launcher();
        spoon.addInputResource(file.toString());
        CtModel model = spoon.buildModel();

        model.filterChildren((el) -> el instanceof CtClass).forEach((CtClass<?> ctClass) -> {
            ctClass.filterChildren((el) -> el instanceof CtMethod<?>).forEach((CtMethod<?> ctMethod) -> {
                ctMethod.filterChildren((el) -> el instanceof CtInvocation).forEach((CtInvocation<?> ctInvocation) -> {
                    for( String key : libraries.keySet()){
                        if (!results.containsKey(key)){
                            results.put(key, new ArrayList<>());
                        }
                        if( libraries.get(key).contains(ctInvocation.getExecutable().getDeclaringType().toString()) ){
                            results.get(key).add(file.toString() + ":" + ctMethod.getSimpleName());
                        }
                    }
                });
            });
        });
    }
}
