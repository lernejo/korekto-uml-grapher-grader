package com.github.lernejo.korekto.grader.uml_grapher;

import com.github.lernejo.korekto.grader.uml_grapher.parts.IdentifierGenerator;
import com.github.lernejo.korekto.grader.uml_grapher.parts.MavenClassloader;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;
import com.github.lernejo.korekto.toolkit.misc.ClassLoaders;
import com.github.lernejo.korekto.toolkit.misc.RandomSupplier;
import com.github.lernejo.korekto.toolkit.partgrader.MavenContext;
import org.apache.maven.cli.MavenExposer;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Random;

public class LaunchingContext extends GradingContext implements MavenContext {
    private static final Random r = new Random();
    // Mutable for tests
    public static RandomSupplier RANDOM = r::nextInt;

    private final IdentifierGenerator identifierGenerator = new IdentifierGenerator(RANDOM);
    private boolean compilationFailed;
    private boolean testFailed;

    private ClassLoader mavenMainClassloader;

    private final MavenExposer mavenExposer = new MavenExposer();

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
        if (mavenMainClassloader == null) {
            mavenMainClassloader = MavenClassloader.build(mavenExposer, getExercise());
        }
        return mavenMainClassloader;
    }

    public ClassLoader newTmpMavenChildClassLoader() {
        return new URLClassLoader(new URL[0], getMavenMainClassloader());
    }

    public String newTypeId() {
        return identifierGenerator.generateId(true);
    }

    public String newId() {
        return identifierGenerator.generateId(false);
    }
}
