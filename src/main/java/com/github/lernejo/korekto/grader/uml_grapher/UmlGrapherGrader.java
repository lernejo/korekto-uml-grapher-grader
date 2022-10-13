package com.github.lernejo.korekto.grader.uml_grapher;

import com.github.lernejo.korekto.grader.uml_grapher.parts.ClassDiagramRenderingByCliVerification;
import com.github.lernejo.korekto.grader.uml_grapher.parts.ClassDiagramRenderingVerification;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.Grader;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.misc.HumanReadableDuration;
import com.github.lernejo.korekto.toolkit.partgrader.GitHubActionsPartGrader;
import com.github.lernejo.korekto.toolkit.partgrader.JacocoCoveragePartGrader;
import com.github.lernejo.korekto.toolkit.partgrader.MavenCompileAndTestPartGrader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;

import static com.github.lernejo.korekto.grader.uml_grapher.parts.ClassDiagramVerifier.classNameAndAnnotation;
import static com.github.lernejo.korekto.grader.uml_grapher.parts.ClassDiagramVerifier.relations;
import static com.github.lernejo.korekto.grader.uml_grapher.parts.TypesSupplier.*;

public class UmlGrapherGrader implements Grader<LaunchingContext> {

    private final Logger logger = LoggerFactory.getLogger(UmlGrapherGrader.class);

    @Override
    public String slugToRepoUrl(String login) {
        return "https://github.com/" + login + "/uml_grapher";
    }

    @Override
    public void run(LaunchingContext context) {
        context.getGradeDetails().getParts().addAll(grade(context));
    }

    private Collection<? extends GradePart> grade(LaunchingContext context) {
        return graders().stream()
            .map(g -> applyPartGrader(context, g))
            .toList();
    }

    private GradePart applyPartGrader(LaunchingContext context, PartGrader<LaunchingContext> g) {
        long startTime = System.currentTimeMillis();
        try {
            return g.grade(context);
        } finally {
            logger.debug(g.name() + " in " + HumanReadableDuration.toString(System.currentTimeMillis() - startTime));
        }
    }

    private Collection<? extends PartGrader<LaunchingContext>> graders() {
        return List.of(
            new MavenCompileAndTestPartGrader<>(
                "Part 1 - Compilation & Tests",
                2.0D),
            new GitHubActionsPartGrader<>("Part 2 - CI", 2.0D),
            new JacocoCoveragePartGrader<>("Part 3 - Code Coverage", 4.0D, 0.85D),
            new ClassDiagramRenderingVerification("Part 4 - Graph of a simple Interface", 1.0D, 1.0D,
                simpleInterface(), classNameAndAnnotation()),
            new ClassDiagramRenderingVerification("Part 5 - Graph of simple Classes", 1.0D, 1.0D,
                simpleClasses(2), classNameAndAnnotation()),
            new ClassDiagramRenderingByCliVerification("Part 6 - CLI invocation", 2.0D, 1.0D,
                simpleClasses(4), classNameAndAnnotation()),
            new ClassDiagramRenderingVerification("Part 7 - Walk into the parent hierarchy", 2.0D, 1.0D,
                bottomUpHierarchy(), classNameAndAnnotation().and(relations())),
            new ClassDiagramRenderingVerification("Part 8 - Walk into the children hierarchy", 3.0D, 1.5D,
                topToBottomHierarchy(), classNameAndAnnotation().and(relations()))
        );
    }

    @Override
    public boolean needsWorkspaceReset() {
        return true;
    }

    @Override
    public LaunchingContext gradingContext(GradingConfiguration configuration) {
        return new LaunchingContext(configuration);
    }
}
