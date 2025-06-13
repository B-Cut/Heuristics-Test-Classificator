package cael.uff;

import spoon.MavenLauncher;
import spoon.reflect.CtModel;
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
    private CtModel model = null;
    public void setProjectPath(String projectPath){
        this.projectPath = projectPath;
    }
    public String getProjectPath(){
        return projectPath;
    }
    public Collection<?> packages;
    public CtModel getModel(){
        if(model == null){ createModel(); }

        return model;
    }

    private List<Path> getTestFolders(){
        return testDirs;
    }
    public Collection<?> packages;
    public CtModel getModel(){
        if(model == null){ createModel(); }

        return model;
    }

    public List<Path> getTestDirs(){
        return testDirs;
    }

    public void setTestDirs(ArrayList<Path> testDirs){
        this.testDirs = testDirs;
    }

    private void createModel(){
        MavenLauncher launcher = new MavenLauncher(projectPath, MavenLauncher.SOURCE_TYPE.TEST_SOURCE);
        CompilationUnitFilter filter = new CompilationUnitFilter() {
            @Override
            public boolean exclude(String s) {
                if(!s.endsWith(".java")){
                    System.out.println(s);
                }
                return !s.endsWith(".java");
            }
        };
        launcher.getFactory().getEnvironment().setNoClasspath(true);
        launcher.getEnvironment().setComplianceLevel(21);
        launcher.getModelBuilder().addCompilationUnitFilter( filter );


        try {
            testDirs = Finders.testFoldersInRepo(Path.of(projectPath), "test");
        } catch (Exception e){
            e.printStackTrace();
        }

        model = launcher.buildModel();
        packages = model.getAllPackages();
    }

    public ProjectInfo getInstance() {
        return INSTANCE;
    }

}
