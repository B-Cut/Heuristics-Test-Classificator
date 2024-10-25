package cael.uff.classification;

import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.CtModel;

import java.nio.file.Path;


public class FunctionClassifier {
    public static void massClassifyFunctionOnFile(Path file, TestPhases classification){
        SpoonAPI spoon = new Launcher();
        // Makes sure we have a proper file
        spoon.addInputResource(file.toAbsolutePath().toString());
        CtModel model = spoon.getModel();
    }
    public static void massClassifyFunctionOnDirectory(){}
    public static void classifyFunctionByName(){}
    public static void classifyFunctionByComment(){}
}
