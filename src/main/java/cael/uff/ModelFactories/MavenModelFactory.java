package cael.uff.ModelFactories;

import spoon.MavenLauncher;
import spoon.reflect.CtModel;
import spoon.support.compiler.jdt.CompilationUnitFilter;

import java.nio.file.Path;

public class MavenModelFactory implements ModelFactory {
    private static final int DEFAULT_COMPLIANCE_LEVEL = 21;
    private static final boolean DEFAULT_NO_CLASSPATH_MODE = false;

    private int complianceLevel;
    private boolean noClassPathMode;

    public MavenModelFactory(int complianceLevel, boolean noClassPathMode) {
        this.complianceLevel = complianceLevel;
        this.noClassPathMode = noClassPathMode;
    }

    public MavenModelFactory(int complianceLevel) {
        this(complianceLevel, DEFAULT_NO_CLASSPATH_MODE);
    }

    public MavenModelFactory(boolean noClassPathMode) {
        this(DEFAULT_COMPLIANCE_LEVEL, noClassPathMode);
    }

    public MavenModelFactory() {
        this(DEFAULT_COMPLIANCE_LEVEL, DEFAULT_NO_CLASSPATH_MODE);
    }

    @Override
    public CtModel createModel(Path path){
        MavenLauncher launcher = new MavenLauncher(path.toString(), MavenLauncher.SOURCE_TYPE.ALL_SOURCE);

        CompilationUnitFilter filter = new CompilationUnitFilter() {
            @Override
            public boolean exclude(String s) {
                if(!s.endsWith(".java")){
                    System.out.println(s);
                }
                return !s.endsWith(".java");
            }
        };
        launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);

        launcher.getModelBuilder().addCompilationUnitFilter( filter );
        launcher.getEnvironment().setComplianceLevel(this.complianceLevel);
        launcher.getEnvironment().setAutoImports(true);
        launcher.getEnvironment().setLevel("ALL");

        /*for (SpoonPom mod : modules){
            MavenLauncher tempLauncher = new MavenLauncher(mod.getPath(), MavenLauncher.SOURCE_TYPE.TEST_SOURCE);
            tempLauncher.getFactory().getEnvironment().setNoClasspath(true);
            tempLauncher.getFactory().getEnvironment().setIgnoreDuplicateDeclarations(true);
            models.add(tempLauncher.buildModel());
        }*/

        launcher.getEnvironment().setNoClasspath(noClassPathMode);
        return launcher.buildModel();
    }
}
