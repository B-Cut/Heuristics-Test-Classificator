package cael.uff.ModelFactories;

import cael.uff.Finders;
import org.apache.commons.lang3.ArrayUtils;
import spoon.Launcher;
import spoon.reflect.CtModel;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class GradleModelFactory implements ModelFactory {
    Path gradleDownloadCache;

    private static final int DEFAULT_COMPLIANCE_LEVEL = 21;
    private static final boolean DEFAULT_NO_CLASSPATH_MODE = false;

    private int complianceLevel;
    private boolean noClassPathMode;

    public GradleModelFactory(int complianceLevel, boolean noClassPathMode) {
        this.complianceLevel = complianceLevel;
        this.noClassPathMode = noClassPathMode;
        gradleDownloadCache = Paths.get(System.getProperty("user.home"), ".gradle/caches/modules-2/files-2.1");
    }

    public GradleModelFactory(int complianceLevel) {
        this(complianceLevel, DEFAULT_NO_CLASSPATH_MODE);
    }

    public GradleModelFactory(boolean noClassPathMode) {
        this(DEFAULT_COMPLIANCE_LEVEL, noClassPathMode);
    }

    public GradleModelFactory() {
        this(DEFAULT_COMPLIANCE_LEVEL, DEFAULT_NO_CLASSPATH_MODE);
    }

    @Override
    public CtModel createModel(Path path) {
        Launcher launcher = new Launcher();

        launcher.addInputResource(path.toAbsolutePath().toString());

        List<Path> cachedDependencies;
        try {
            cachedDependencies = Finders.filesContainingSubstring(gradleDownloadCache, ".jar");
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }

        /*for (Path cachedDependency : cachedDependencies) {
            launcher.addInputResource(cachedDependency.toAbsolutePath().toString());
        }*/

        String[] dependenciesClasspath = cachedDependencies.stream().map((Path t) -> t.toAbsolutePath().toString()).collect(Collectors.toList()).toArray(new String[0]);


        String[] fullClasspath = ArrayUtils.addAll(launcher.getEnvironment().getSourceClasspath(), dependenciesClasspath);

        launcher.getEnvironment().setSourceClasspath(fullClasspath);
        launcher.getEnvironment().setIgnoreDuplicateDeclarations(true);
        launcher.getEnvironment().setComplianceLevel(this.complianceLevel);
        launcher.getEnvironment().setAutoImports(false);
        launcher.getEnvironment().setLevel("ALL");
        launcher.getEnvironment().setNoClasspath(true);
        return launcher.buildModel();
    }
}
