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

    public List<Path> getTestDirs(){
        return testDirs;
    }

    public void setTestDirs(List<Path> testDirs){
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

        launcher.getModelBuilder().addCompilationUnitFilter( filter );

        /*List<Path> testFolders = new ArrayList<Path>();
        try {
            testFolders = Finders.testFoldersInRepo(Path.of(projectPath), "test");
        } catch (Exception e){
            e.printStackTrace();
        }

        for (Path path : testFolders){
            launcher.addInputResource(path.toString());
        }*/

        model = launcher.buildModel();
        packages = model.getAllPackages();
    }

    public ProjectInfo getInstance() {
        return INSTANCE;
    }

}
