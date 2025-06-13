package cael.uff.classification.analytic;

import cael.uff.ProjectInfo;
import cael.uff.Utils;
import cael.uff.classification.FunctionInfo;
import org.apache.commons.lang3.StringUtils;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.*;
import spoon.reflect.visitor.CtScanner;

import java.lang.annotation.Annotation;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private final String rootPackage = ProjectInfo.INSTANCE.getModel().getRootPackage().getQualifiedName();

    public AnalyticClassifier(){
        results.put("unit", new ArrayList<>());
        results.put("integration", new ArrayList<>());
        results.put("system", new ArrayList<>());
        results.put(undefinedPhase, new ArrayList<>());
    }

    public void startClassification(){
        CtModel model = ProjectInfo.INSTANCE.getModel();

        CtScanner scanner = new CtScanner(){
            @Override
            public <A extends Annotation> void visitCtAnnotation(CtAnnotation<A> annotation) {
                super.visitCtAnnotation(annotation);
                // We found a test
                if(annotation.toString().endsWith("Test")){
                    classifyClassIsUnit(annotation.getParent(CtMethod.class));
                }
            }
        };


        model.filterChildren((el) -> el instanceof CtClass<?>).forEach(scanner::scan);

        System.out.println(results.get("unit").size());
    }

    private String extractMethodName(CtInvocation<?> invocation){
        return invocation.getExecutable().getActualMethod().toString();
    }

    private String extractDeclaringClass(CtInvocation<?> invocation){
        return invocation.getExecutable().getDeclaringType().toString();
    }

    private String extractPackage(CtInvocation<?> invocation){
        return invocation.getExecutable().getDeclaringType().getPackage().toString();
    }

    private String commonPackagePath(String package1, String package2){
        if(package1.equals(package2)) return package1;



        String[] longestPath = package1.split("\\.");
        String[] shortestPath = package2.split("\\.");

        if (shortestPath.length > longestPath.length){
            String[] temp = longestPath;
            longestPath = shortestPath;
            shortestPath = temp;
        }

        List<String> commonPath = new ArrayList<String>();

        for(int i = 0; i < shortestPath.length; i++){
            if(shortestPath[i].equals(longestPath[i])){
                commonPath.add(shortestPath[i]);
            } else {
                break;
            }
        }

        return String.join(".", commonPath);
    }
    private void classify(CtMethod<?> method){


        CtScanner scanner = new CtScanner(){
            String lastMethodCalled = "";
            String lastClassCalled = "";
            String highestOrderPackage = "";

            private enum Type{METHOD, CLASS, PACKAGE, NOT_UNIT}
            private Type currentType = Type.METHOD;

            private String[] packageIgnoreList = {
                    "java.",
                    "org.junit"
            };

            @Override
            public <T> void visitCtInvocation(CtInvocation<T> invocation) {
                if(currentType == Type.NOT_UNIT) return;

                if(Arrays.stream(packageIgnoreList)
                        .anyMatch(p -> extractMethodName(invocation).startsWith(p))
                ){
                    return;
                }


                if(lastMethodCalled.isEmpty()) lastMethodCalled = extractMethodName(invocation);
                if(lastClassCalled.isEmpty()) lastClassCalled = extractDeclaringClass(invocation);
                if(highestOrderPackage == null) highestOrderPackage = extractPackage(invocation);

                if(!lastMethodCalled.equals(extractMethodName(invocation)) && currentType == Type.METHOD)
                    currentType = Type.CLASS;
                if(!lastClassCalled.equals(extractDeclaringClass(invocation)) && currentType == Type.CLASS)
                    currentType = Type.PACKAGE;

                if(!extractPackage(invocation).equals(highestOrderPackage)){
                    String newPath = commonPackagePath(extractPackage(invocation), highestOrderPackage);
                    if (newPath.isEmpty() || newPath.equals(rootPackage)) currentType = Type.NOT_UNIT;
                    else highestOrderPackage = newPath;
                }

            }

            public String getCurrentType() {
                return currentType.toString();
            }
        };

        scanner.scan(method);



    }

    private void classifyMethodIsUnit(CtMethod<?> method){
        final String[] lastMethodCalled = {""};
        AtomicBoolean isUnit = new AtomicBoolean(true);

        method.filterChildren(((el) -> el instanceof CtInvocation<?>)).forEach((CtInvocation<?> invocation) -> {
            // This method won't stop until all invocations are evaluated.
            // So we set this variable to skip all subsequent function calls
            if(!isUnit.get()) return;

            String methodName = invocation.getExecutable().getActualMethod().toString();

            // Ignore default java libraries
            if(methodName.startsWith("java.")){
                return;
            }

            // Ignore JUnit methods
            if(Utils.containsCaseInsensitive(methodName, "junit")) return;


            if(lastMethodCalled[0].isEmpty()){
                // If there isn't a class defined, define one
                lastMethodCalled[0] = methodName;
            }
            // if the current invocations belongs to a class different from the one being tested, the class is not the unit
            else if(!methodName.equals(lastMethodCalled[0])){
                isUnit.set(false);
            }
        });

        if(isUnit.get()){
            results.get("unit")
                    .add(new FunctionInfo(method.getSimpleName(), Path.of(method.getPath().toString()), method.prettyprint()));
        } else {
            classifyClassIsUnit(method);
        }
    }

    private void classifyClassIsUnit(CtMethod<?> method){

        final String[] lastInvocationClass = {""};
        AtomicBoolean isUnit = new AtomicBoolean(true);


        method.filterChildren(((el) -> el instanceof CtInvocation<?>)).forEach((CtInvocation<?> invocation) -> {
            // This method won't stop until all invocations are evaluated.
            // So we set this variable to skip all subsequent function calls
            if(!isUnit.get()) return;

            String className = extractDeclaringClass(invocation);

            // Ignore default java libraries
            if(className.startsWith("java.")){
                return;
            }

            // Ignore JUnit methods
            if(Utils.containsCaseInsensitive(className, "junit")) return;


            if(lastInvocationClass[0].isEmpty()){
                // If there isn't a class defined, define one
                lastInvocationClass[0] = className;
            }
            // if the current invocations belongs to a class different from the one being tested, the class is not the unit
            else if(!className.equals(lastInvocationClass[0])){
                isUnit.set(false);
            }
        });

        if(isUnit.get()){
            results.get("unit")
                    .add(new FunctionInfo(method.getSimpleName(), Path.of(method.getPath().toString()), method.prettyprint()));
        } else {
            classifyPackageIsUnit(method);
        }
    }

    private void classifyPackageIsUnit(CtMethod<?> method){}
}
