package cael.uff.classification;

import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.visitor.chain.CtFunction;
import spoon.reflect.visitor.chain.CtQueryable;
import spoon.reflect.visitor.filter.TypeFilter;

import java.nio.file.Path;


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
    public static void massClassifyFunctionOnDirectory(){}
    public static void classifyFunctionByName(){}
    public static void classifyFunctionByComment(){}
}
