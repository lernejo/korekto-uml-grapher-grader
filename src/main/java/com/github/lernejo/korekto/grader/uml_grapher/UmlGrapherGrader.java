package com.github.lernejo.korekto.grader.uml_grapher;

import com.github.lernejo.korekto.grader.uml_grapher.parts.ClassDiagramRenderingByCliVerification;
import com.github.lernejo.korekto.grader.uml_grapher.parts.ClassDiagramRenderingVerification;
import com.github.lernejo.korekto.grader.uml_grapher.parts.PmdPartGrader;
import com.github.lernejo.korekto.toolkit.Grader;
import com.github.lernejo.korekto.toolkit.GradingConfiguration;
import com.github.lernejo.korekto.toolkit.PartGrader;
import com.github.lernejo.korekto.toolkit.partgrader.GitHistoryPartGrader;
import com.github.lernejo.korekto.toolkit.partgrader.GitHubActionsPartGrader;
import com.github.lernejo.korekto.toolkit.partgrader.JacocoCoveragePartGrader;
import com.github.lernejo.korekto.toolkit.partgrader.MavenCompileAndTestPartGrader;
import com.github.lernejo.korekto.toolkit.thirdparty.pmd.Rule;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

import static com.github.lernejo.korekto.grader.uml_grapher.parts.ClassDiagramVerifier.*;
import static com.github.lernejo.korekto.grader.uml_grapher.parts.TypesSupplier.*;

public class UmlGrapherGrader implements Grader<LaunchingContext> {

    @NotNull
    @Override
    public String name() {
        return "UML grapher project";
    }

    @NotNull
    @Override
    public String slugToRepoUrl(@NotNull String login) {
        return "https://github.com/" + login + "/uml_grapher";
    }

    @NotNull
    @Override
    public LaunchingContext gradingContext(@NotNull GradingConfiguration configuration) {
        return new LaunchingContext(configuration);
    }

    @NotNull
    public Collection<PartGrader<LaunchingContext>> graders() {
        return List.of(
            new GitHistoryPartGrader<>("Git (proper descriptive messages)", -4.0D),
            new PmdPartGrader("Coding style", -4.0D,
                Rule.buildExcessiveClassLengthRule(100),
                Rule.buildExcessiveMethodLengthRule(18),
                Rule.buildFieldMandatoryModifierRule(2, "private", "final", "!static")
            ),
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
                topToBottomHierarchy(), classNameAndAnnotation().and(relations())),
            new ClassDiagramRenderingVerification("Part 9 - Display & walk members: singleton", 1.5D, 1.5D,
                singleton(), classNameAndAnnotation().and(relations()).and(members())),
            new ClassDiagramRenderingVerification("Part 10 - Display & walk members: proxy", 1.5D, 1.5D,
                proxy(), classNameAndAnnotation().and(relations()).and(members()))
        );
    }

    @Override
    public boolean needsWorkspaceReset() {
        return true;
    }
}
