package cael.uff;

import org.apache.maven.model.Model;
import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.support.compiler.SpoonPom;
import spoon.support.compiler.jdt.CompilationUnitFilter;

import java.io.Console;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import java.util.stream.Collectors;

public enum ProjectInfo{
    INSTANCE;
    private String projectPath;
    private List<Path> testDirs = new ArrayList<>();

    public void setProjectPath(String projectPath){
        this.projectPath = projectPath;
    }
    public String getProjectPath(){
        return projectPath;
    }
    public Collection<?> packages;


    private List<Path> getTestFolders(){
        return testDirs;
    }

    public List<Path> getTestDirs(){
        return testDirs;
    }

    public void setTestDirs(ArrayList<Path> testDirs){
        this.testDirs = testDirs;
    }

    public ProjectInfo getInstance() {
        return INSTANCE;
    }

}
