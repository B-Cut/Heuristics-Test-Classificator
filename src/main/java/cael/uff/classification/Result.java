package cael.uff.classification;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Result {
    List<Path> unitFolders = new ArrayList<>();
    List<Path> integrationFolders = new ArrayList<>();
    List<Path> systemFolders = new ArrayList<>();
    List<Path> unclassifiedFolders = new ArrayList<>();

    List<Path> unitFiles = new ArrayList<>();
    List<Path> integrationFiles = new ArrayList<>();
    List<Path> systemFiles = new ArrayList<>();
    List<Path> unclassifiedFiles = new ArrayList<>();

    List<FunctionInfo> unitFunctions = new ArrayList<>();
    List<FunctionInfo> integrationFunctions = new ArrayList<>();
    List<FunctionInfo> systemFunctions = new ArrayList<>();
    List<FunctionInfo> unclassifiedFunctions = new ArrayList<>();
}
