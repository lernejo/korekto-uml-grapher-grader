package com.github.lernejo.korekto.grader.uml_grapher;

import com.github.lernejo.korekto.grader.uml_grapher.parts.IdentifierGenerator;
import com.github.lernejo.korekto.grader.uml_grapher.parts.MavenClassloader;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;
import com.github.lernejo.korekto.toolkit.misc.RandomSupplier;
import com.github.lernejo.korekto.toolkit.misc.ThrowingFunction;
import com.github.lernejo.korekto.toolkit.partgrader.MavenContext;
import org.apache.maven.cli.MavenExposer;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LaunchingContext extends GradingContext implements MavenContext {
    // Mutable for tests
    static RandomSupplier RANDOM = RandomSupplier.createRandom();

    private final IdentifierGenerator identifierGenerator = new IdentifierGenerator(RANDOM);
    private final MavenExposer mavenExposer = new MavenExposer();
    private boolean compilationFailed;
    private boolean testFailed;
    private List<URL> mavenClassPath;
    private ClassLoader mavenMainClassloader;

    public LaunchingContext(GradingConfiguration configuration) {
        super(configuration);
    }

    @Override
    public boolean hasCompilationFailed() {
        return compilationFailed;
    }

    @Override
    public boolean hasTestFailed() {
        return testFailed;
    }

    @Override
    public void markAsCompilationFailed() {
        compilationFailed = true;
    }

    @Override
    public void markAsTestFailed() {
        testFailed = true;
    }

    public ClassLoader getMavenMainClassloader() {
        initClassPathAndClassLoader();
        return mavenMainClassloader;
    }

    public ClassLoader newTmpMavenChildClassLoader(Path... additionalPaths) {
        URL[] urls = Arrays.stream(additionalPaths)
            .map(Path::toUri)
            .map(ThrowingFunction.sneaky(uri -> uri.toURL()))
            .toArray(URL[]::new);
        return new URLClassLoader(urls, getMavenMainClassloader());
    }

    public List<URL> getMavenClassPath() {
        initClassPathAndClassLoader();
        return List.copyOf(mavenClassPath);
    }

    public String newTypeId() {
        return identifierGenerator.generateId(true);
    }

    public String newId() {
        return identifierGenerator.generateId(false);
    }

    private void initClassPathAndClassLoader() {
        if (mavenClassPath == null) {
            mavenClassPath = MavenClassloader.getMavenClassPath(mavenExposer, getExercise());
            mavenMainClassloader = MavenClassloader.buildIsolatedClassLoader(mavenClassPath);
        }
    }

    public RandomSupplier randomSupplier() {
        return RANDOM;
    }
}
