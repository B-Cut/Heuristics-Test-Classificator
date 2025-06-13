package cael.uff.classification.framework;

import cael.uff.ProjectInfo;
import cael.uff.classification.FunctionInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import spoon.Launcher;
import spoon.MavenLauncher;
import spoon.SpoonAPI;
import spoon.compiler.Environment;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.visitor.CtScanner;
import spoon.support.compiler.SpoonPom;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
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
    private URLClassLoader classLoader;

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

        ArrayList<URL> testDependencies = new ArrayList<URL>();

        this.results = new HashMap<>();
        for(PhaseInfo p : this.phases){
            this.results.put(p.phase(), new ArrayList<>());
        }
        this.results.put(undefinedPhase, new ArrayList<>());

        URL localMavenRepo;
        try {
            localMavenRepo = new URL("file://" + System.getenv().get("HOME") + "/.m2/repository/" );
        } catch (Exception e){
            System.err.println("Malformed file URL: " + undefinedPhase);
            return;
        }
        URL[] temp = {localMavenRepo};
        this.classLoader = new URLClassLoader(temp);
    }

    public void classify(){
        SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                classifyFile(file);
                return FileVisitResult.CONTINUE;
            }
        };



        for (Path testFolder : ProjectInfo.INSTANCE.getTestDirs()){
            try{
                Files.walkFileTree(testFolder, visitor);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void classifyFile(Path file){
        SpoonAPI spoon = new Launcher();
        spoon.addInputResource(file.toAbsolutePath().toString());
        spoon.getEnvironment().setInputClassLoader(classLoader);
        CtModel model = spoon.buildModel();

        CtScanner scanner = new CtScanner(){
            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation){
                if (invocation.getExecutable().getDeclaringType() == null){
                    return;
                }

                String phase = undefinedPhase;
                int priority = 0;

                for( PhaseInfo temp_phase : phases){
                    for ( FrameworkInfo lib : temp_phase.libraries() ){
                        CtExecutableReference source = invocation.getExecutable();

                        if (source == null) continue;

                        for (String kw : lib.frameworkKeywords()){
                            if(source.getDeclaringType().toString().contains(kw)){
                                if ( temp_phase.priority() > priority){
                                    priority = temp_phase.priority();
                                    phase = temp_phase.phase();
                                }
                            }
                        }
                    }
                }

                CtElement parent =  invocation.getParent();


                while (! (parent instanceof CtMethod)){
                    if (parent == null) return;
                    parent = parent.getParent();
                }

                CtMethod invokingMethod = (CtMethod) parent;
                results.get(phase).add(new FunctionInfo(invokingMethod.getSimpleName(), invokingMethod.getDeclaringType().getPosition().getFile().toPath(), ""));
            };
        };

        model.filterChildren(el -> el instanceof CtClass<?>).forEach(scanner::scan);
    }

    private void classifyModule(SpoonPom pom){
        if (!pom.getModules().isEmpty()){
            pom.getModules().forEach(this::classifyModule);
        }

        String modulePath = Path.of(pom.getPath()).normalize().getParent().toString();

        MavenLauncher launcher = new MavenLauncher(modulePath, MavenLauncher.SOURCE_TYPE.ALL_SOURCE, true);

        launcher.getFactory().getEnvironment().setAutoImports(true);
        launcher.getFactory().getEnvironment().setIgnoreDuplicateDeclarations(true);
        launcher.getFactory().getEnvironment().setIgnoreSyntaxErrors(true);
        launcher.getFactory().getEnvironment().setShouldCompile(false);
        launcher.getFactory().getEnvironment().checksAreSkipped();

        CtModel model;
        try {
            model = launcher.buildModel();
        } catch (Exception e){
            System.err.println("Failed to build Maven model.\nError: " + e.getMessage());
            return;
        }

        model.filterChildren((CtElement el) -> el instanceof CtClass<?>).forEach(
                ctType -> {

                    CtClass<?> ctClass = (CtClass<?>) ctType;

                    AtomicReference<Boolean> isInTestRepo = new AtomicReference<>(false);

                    ProjectInfo.INSTANCE.getTestDirs().forEach(path -> {
                        if (path.normalize().toAbsolutePath().endsWith(Path.of(ctClass.getPosition().toString()))){
                            isInTestRepo.set(Boolean.valueOf(true));
                        }
                    });

                    if (!isInTestRepo.get()){
                        return;
                    }

                    ctClass.filterChildren((el) -> el instanceof CtMethod<?>).forEach((CtMethod<?> ctMethod) -> {
                        AtomicReference<String> phase = new AtomicReference<>(undefinedPhase);
                        AtomicInteger priority = new AtomicInteger();
                        ctMethod.filterChildren((el) -> el instanceof CtInvocation).forEach((CtInvocation<?> ctInvocation) -> {
                            for( PhaseInfo temp_phase : phases){
                                for ( FrameworkInfo lib : temp_phase.libraries() ){
                                    try {
                                        CtExecutableReference source = ctInvocation.getExecutable();

                                        if (source == null) continue;

                                        for (String kw : lib.frameworkKeywords()){
                                            if(source.getDeclaringType().toString().contains(kw)){
                                                if ( temp_phase.priority() > priority.get()){
                                                    priority.set(temp_phase.priority());
                                                    phase.set(temp_phase.phase());
                                                }
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
                        results.get(phase.get()).add(new FunctionInfo(ctMethod.getSimpleName(), ctClass.getPosition().getFile().toPath(), ""));
                    });
                }
        );
    }


    public Map<String, List<FunctionInfo>> getResults() {
        return results;
    }
}
