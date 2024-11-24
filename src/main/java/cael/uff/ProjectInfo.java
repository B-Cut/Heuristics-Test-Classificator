package cael.uff;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

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

    public List<Path> getTestDirs(){
        return testDirs;
    }

    public void setTestDirs(List<Path> testDirs){
        this.testDirs = testDirs;
    }
    public ProjectInfo getInstance() {
        return INSTANCE;
    }
}
