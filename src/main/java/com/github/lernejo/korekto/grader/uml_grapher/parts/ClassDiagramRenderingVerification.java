package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.AstParser;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.CharStream;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.Lexer;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.MissingTokenException;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ModelTranslator;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDiagram;
import com.github.lernejo.korekto.toolkit.GradePart;
import com.github.lernejo.korekto.toolkit.PartGrader;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ClassDiagramRenderingVerification(String name, Double maxGrade, double errorRatioPenalty,
                                                TypesSupplier typesSupplier,
                                                ClassDiagramVerifier classDiagramVerifier) implements PartGrader<LaunchingContext> {

    @NotNull
    @Override
    public GradePart grade(@NotNull LaunchingContext context) {
        if (context.hasCompilationFailed()) {
            return result(List.of("Not available when there is compilation failures"), 0.0D);
        }

        ClassLoader tmpClassLoader = context.newTmpMavenChildClassLoader();
        try {
            Class<?>[] types = typesSupplier.supply(context, tmpClassLoader);
            String graph = new GraphApiInvoker().invoke(tmpClassLoader, types);
            AstParser parser = new AstParser(new Lexer(new CharStream(graph)));
            ClassDiagram diagram = new ModelTranslator().translate(parser.parseClassDiagram().get());
            List<String> errors = classDiagramVerifier.verify(types, diagram);
            return result(errors, (1.0 - errors.size() * errorRatioPenalty) * maxGrade);
        } catch (GraphApiInvoker.InvocationError e) {
            return result(List.of(e.getMessage()), 0.0);
        } catch (MissingTokenException e) {
            return result(List.of("Graph syntax error: " + e.getMessage()), 0.0);
        } catch (RuntimeException e) {
            throw new GraphApiInvoker.InvocationError("Unhandled error, report to maintainer: " + e.getMessage());
        }
    }
}
