package cael.uff.classification.heuristics;

import cael.uff.classification.FunctionInfo;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class HeuristicsClassifier {
    FilepathClassifier filepathClassifier;
    FunctionClassifier functionClassifier;

    public HeuristicsClassifier(Path keywordFile){
        filepathClassifier = new FilepathClassifier(keywordFile);
        functionClassifier = new FunctionClassifier(keywordFile);
    }

    public void classify(Path root){
        // Classify directories and files based on name
        filepathClassifier.classify(root);

        // Classify all functions found
        functionClassifier.massClassifyFunctionOnClassifiedDirectories(filepathClassifier.directories);
        functionClassifier.massClassifyFunctionOnClassifiedFiles(filepathClassifier.files);
        // In non classified files, classify internal functions
        filepathClassifier.files.get(filepathClassifier.undefinedKeyword).forEach((Path file) -> {
            functionClassifier.classifyFunctionsInFile(file);
        });
    }

    public HashMap<String, List<FunctionInfo>> getResults(){
        return  functionClassifier.classifiedFunctions;
    }
}
