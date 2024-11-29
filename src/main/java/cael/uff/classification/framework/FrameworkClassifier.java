package cael.uff.classification.framework;

import cael.uff.Finders;
import cael.uff.ProjectInfo;
import cael.uff.Utils;
import cael.uff.classification.FunctionInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.support.compiler.SpoonPom;

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

    public void classify(){
        // We will only test maven repos.
        // Since we can have multiple pom.xml files, we need to find them all before classifying
        String projectPath = ProjectInfo.INSTANCE.getProjectPath();
        //ArrayList<Path> modules = Finders.foldersContainingFile(projectPath, "pom.xml");

        MavenLauncher spoon;
        try{
            spoon = new MavenLauncher(projectPath, MavenLauncher.SOURCE_TYPE.ALL_SOURCE, true   );
        } catch (Exception e){
            System.err.println("Failed to launch Maven project. Skipping framework analysis");
            System.err.println(e.getMessage());
            return;
        }


        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setIgnoreDuplicateDeclarations(true);
        spoon.getEnvironment().setIgnoreSyntaxErrors(true);
        spoon.getEnvironment().setShouldCompile(true);

        CtModel model;
        try{
            model = spoon.buildModel();
        } catch (Exception e){
            System.err.println("Failed to build model. Skipping framework analysis");
            System.err.println(e.getMessage());
            return;
        }


        model.filterChildren((el) -> el instanceof CtClass<?>).forEach((CtClass<?> ctClass) -> {

            // If the class is not in one of the test directories, return from function
            // Inneficient, but works
            if (
                    ProjectInfo.INSTANCE.getTestDirs().stream().noneMatch(
                            (Path testDir) -> {
                                Path classPath = ctClass.getPosition().getFile().toPath().toAbsolutePath().normalize();
                                Path absoluteTestDir = testDir.toAbsolutePath().normalize();
                                return classPath.startsWith(absoluteTestDir);
                            }
                    )
            ){
                return;
            }

            ctClass.filterChildren((el) -> el instanceof CtMethod<?>).forEach((CtMethod<?> ctMethod) -> {
                AtomicReference<String> phase = new AtomicReference<>(this.undefinedPhase);
                AtomicInteger priority = new AtomicInteger();
                ctMethod.filterChildren((el) -> el instanceof CtInvocation).forEach((CtInvocation<?> ctInvocation) -> {
                    for( PhaseInfo temp_phase : phases){
                        for ( String lib : temp_phase.libraries() ){
                            try {
                                if(ctInvocation.getExecutable().getDeclaringType().toString().contains(lib)){
                                    if ( temp_phase.priority() > priority.get()){
                                        priority.set(temp_phase.priority());
                                        phase.set(temp_phase.phase());
                                    }
                                }
                            } catch (NullPointerException e) {
                                System.err.println(
                                        String.format(
                                                "Warning: No declaring type identified for %s:%s",
                                                ctClass.getSimpleName(),
                                                ctInvocation.toString()
                                        )
                                );
                                return;
                            }

                        }
                    }
                });
                results.get(phase.get()).add(new FunctionInfo(ctMethod.getSimpleName(), ctClass.getPosition().getFile().toPath()));
            });
        });
    }

    public Map<String, List<FunctionInfo>> getResults() {
        return results;
    }
}
