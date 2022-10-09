package com.github.lernejo.korekto.grader.uml_grapher;

import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.GradingContext;
import com.github.lernejo.korekto.toolkit.misc.RandomSupplier;
import com.github.lernejo.korekto.toolkit.partgrader.MavenContext;

import java.util.Random;

public class LaunchingContext extends GradingContext implements MavenContext {
    private static final Random r = new Random();
    // Mutable for tests
    public static RandomSupplier RANDOM = r::nextInt;

    private boolean compilationFailed;
    private boolean testFailed;

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
}
