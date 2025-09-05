package cael.uff.classification.analytic;

import cael.uff.MavenModelFactory;
import cael.uff.ProjectInfo;
import cael.uff.Utils;
import cael.uff.classification.FunctionInfo;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;


import java.io.FileWriter;
import java.io.IOException;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.*;


/**
 * This class uses the information contained in a given test to determine in which phase it belong
 */

/*
IDEA:
    The pyramid is organized, from bottom to top, as Unit > Integration > System
    A Unit test has as it's SUT the smallest possible element, usually a method.
    As such, we will classify a Unit test as a test that encompasses only a single method from the package


 */
public class AnalyticClassifier {

    private Map<String, List<FunctionInfo>> results = new HashMap<String, List<FunctionInfo>>();
    private final String undefinedPhase = "undefined";
    private String rootPackage = "";

    private ArrayList<AnalyticResult> analyticResults = new ArrayList<>();

    public AnalyticClassifier(){
        results.put("unit", new ArrayList<>());
        results.put("integration", new ArrayList<>());
        results.put("system", new ArrayList<>());
        results.put(undefinedPhase, new ArrayList<>());
    }

    public void startClassification(){
        CtModel model = new MavenModelFactory(true).createModel(Path.of(ProjectInfo.INSTANCE.getProjectPath()));
        this.rootPackage = model.getRootPackage().getQualifiedName();
        CtScanner scanner = new CtScanner(){
            @Override
            public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
                super.visitCtAnnotation(annotation);
                // We found a test
                if(annotation.toString().endsWith("Test")){
                    classify(annotation.getParent(CtMethod.class));
                }
            }
        };


        model.filterChildren((el) -> el instanceof CtClass<?>).forEach(scanner::scan);

        System.out.println(analyticResults.size());
    }

    private String extractMethodName(CtInvocation<?> invocation){
        try{
            return extractDeclaringClass(invocation) + '.' + invocation.getExecutable().getSimpleName();
        } catch (Exception e){
            throw new RuntimeException("Could not extract method name from CtInvocation: " + invocation.toStringDebug(), e);
        }
    }

    private String extractDeclaringClass(CtInvocation<?> invocation){
        try{
            return invocation.getExecutable().getDeclaringType().getQualifiedName();
        } catch (Exception e){
            throw new RuntimeException("Could not extract class name from CtInvocation: " + invocation.toStringDebug(), e);
        }
    }

    private String extractPackage(CtInvocation<?> invocation){
        CtTypeReference<?> declaringType = invocation.getExecutable().getDeclaringType();
        while (declaringType.getDeclaringType() != null){
            declaringType = declaringType.getDeclaringType();
        }
        try{
            return declaringType.getPackage().getQualifiedName();
        } catch (Exception e){
            throw new RuntimeException("Could not extract package name from CtInvocation: " + invocation.toString(), e);
        }
    }

    private class UnitScanner extends CtScanner {
        String lastMethodCalled = "";
        String lastClassCalled = "";
        String highestOrderPackage = "";

        public boolean hadError = false;

        private UnitTypes currentType = UnitTypes.METHOD;

        private final String[] packageIgnoreList = {
                "java.",
                "org.junit"
        };

        @Override
        public <T> void visitCtInvocation(CtInvocation<T> invocation) {
            if(currentType == UnitTypes.NOT_UNIT) return;

            String methodName = "";

            try {
                methodName = extractMethodName(invocation);
            } catch (RuntimeException ex){
                System.err.println("Failed to get executable for " + invocation);
                System.err.println(ex.getMessage() + "\n");
                hadError = true;
                return;
            }

            if(Arrays.stream(packageIgnoreList)
                    .anyMatch(p -> extractMethodName(invocation).startsWith(p))
            ){
                return;
            }

            try{
                if(lastMethodCalled.isEmpty()) lastMethodCalled = extractMethodName(invocation);
                if(lastClassCalled.isEmpty()) lastClassCalled = extractDeclaringClass(invocation);
                if(highestOrderPackage.isEmpty()) highestOrderPackage = extractPackage(invocation);
            } catch (RuntimeException ex){
                System.err.println("Failed to get executable for " + invocation.toString());
                System.err.println(ex.getMessage() + "\n");
                hadError = true;
                return;
            }


            if(!lastMethodCalled.equals(extractMethodName(invocation)) && currentType == UnitTypes.METHOD)
                currentType = UnitTypes.CLASS;

            if(!lastClassCalled.equals(extractDeclaringClass(invocation)) && currentType == UnitTypes.CLASS)
                currentType = UnitTypes.PACKAGE;

            String currentPackage;
            try{
                currentPackage = extractPackage(invocation);
            } catch (RuntimeException ex){
                System.err.println("Failed to get package for " + invocation.toString());
                System.err.println(ex.getMessage() + "\n");
                hadError = true;
                return;
            }


            String commonPath = Utils.commonPackagePath(rootPackage, currentPackage, highestOrderPackage);

            if(!commonPath.equals(highestOrderPackage)){
                if (commonPath.isEmpty() || commonPath.equals(rootPackage)) currentType = UnitTypes.NOT_UNIT;
                else if(commonPath.length() < highestOrderPackage.length()) highestOrderPackage = commonPath;
            }
        }

        public UnitTypes getCurrentType() {
            return currentType;
        }
    }

    private void classify(CtMethod<?> method){
        if (method == null) return;
        UnitScanner scanner = new UnitScanner();

        scanner.scan(method);

        if(scanner.hadError && scanner.getCurrentType() == UnitTypes.METHOD){
            scanner.currentType = UnitTypes.ERROR;
        }

        analyticResults.add(
                new AnalyticResult(method.getSimpleName(),
                        method.getDeclaringType().getQualifiedName(),
                        scanner.getCurrentType(),
                        method.prettyprint(),
                        scanner.hadError));
    }

    public void dumpResults(Path path){
        try{
            FileWriter result = new FileWriter(path.toFile());
            for (AnalyticResult analyticResult : analyticResults) {
                result.append("Function Name: ").append(analyticResult.Name()).append("\n");
                result.append("Origin Class: ").append(analyticResult.Origin()).append("\n");
                result.append("Type: ").append(analyticResult.Type().toString()).append("\n");
                result.append("Encountered Error: ").append(analyticResult.hadError().toString()).append("\n");
                result.append("Body:\n").append(analyticResult.body()).append("\n");
                result.append("\n====================================================================================================\n");
            }
        } catch (IOException e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

    }

}
