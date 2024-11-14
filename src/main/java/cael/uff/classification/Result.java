package cael.uff.classification;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Result {
    // TODO: Turn these into dictionaries
    public List<Path> unitFolders = new ArrayList<>();
    public List<Path> integrationFolders = new ArrayList<>();
    public List<Path> systemFolders = new ArrayList<>();
    public List<Path> unclassifiedFolders = new ArrayList<>();

    public List<Path> unitFiles = new ArrayList<>();
    public List<Path> integrationFiles = new ArrayList<>();
    public List<Path> systemFiles = new ArrayList<>();
    public List<Path> unclassifiedFiles = new ArrayList<>();

    public List<FunctionInfo> unitFunctions = new ArrayList<>();
    public List<FunctionInfo> integrationFunctions = new ArrayList<>();
    public List<FunctionInfo> systemFunctions = new ArrayList<>();
    public List<FunctionInfo> unclassifiedFunctions = new ArrayList<>();
}
