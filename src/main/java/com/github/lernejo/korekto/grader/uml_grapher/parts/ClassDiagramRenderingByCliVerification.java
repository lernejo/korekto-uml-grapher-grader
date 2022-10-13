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
import com.github.lernejo.korekto.toolkit.misc.OS;
import com.github.lernejo.korekto.toolkit.misc.Processes;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.max;

public record ClassDiagramRenderingByCliVerification(String name, Double maxGrade, double errorRatioPenalty,
                                                     TypesSupplier typesSupplier,
                                                     ClassDiagramVerifier classDiagramVerifier) implements PartGrader<LaunchingContext> {

    private static final boolean IS_WIN = OS.WINDOWS.isCurrentOs();
    private static final String LAUNCHER_CLASS_NAME = "fr.lernejo.umlgrapher.Launcher";

    @Override
    public GradePart grade(LaunchingContext context) {
        if (context.hasCompilationFailed()) {
            return result(List.of("Not available when there is compilation failures"), 0.0D);
        }

        try {
            List<URL> classPath = context.getMavenClassPath();
            String cp = buildClassPath(classPath);

            TypesSupplier.Types types = typesSupplier.supply(context, context.newTmpMavenChildClassLoader());

            String programArguments = Arrays.stream(types.selectedTypes())
                .flatMap(t -> Stream.of("-c", t.getName()))
                .collect(Collectors.joining(" "));

            String javaCommand = escape(Paths.get(System.getProperty("java.home")).resolve("bin").resolve("java"));
            String command = javaCommand + " -cp \"" + cp + "\" " + LAUNCHER_CLASS_NAME + " " + programArguments;
            Processes.ProcessResult result = Processes.launch(command, null);

            if (result.getExitCode() != 0) {
                return result(List.of("Program exit with code `" + result.getExitCode() + "`: " + result.getOutput()), 0.0);
            }

            String graph = result.getOutput();

            try {
                AstParser parser = new AstParser(new Lexer(new CharStream(graph)));
                ClassDiagram diagram = new ModelTranslator().translate(parser.parseClassDiagram().get());
                List<String> errors = classDiagramVerifier.verify(types, diagram);
                return result(errors, (1.0 - max(0, errors.size() * errorRatioPenalty)) * maxGrade);
            } catch (MissingTokenException e) {
                return result(List.of("Graph syntax error: " + e.getMessage() + "\n\n```\n" + graph.trim() + "\n```"), 0.0);
            }
        } catch (InvocationError e) {
            return result(List.of(e.getMessage()), 0.0);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return result(List.of("Unhandled error, report to maintainer: " + e.getClass().getName() + " " + e.getMessage()), 0.0);
        }
    }

    private String buildClassPath(List<URL> classPath) {
        return classPath.stream().map(URL::getPath).map(this::removeWinPrefix).collect(Collectors.joining(File.pathSeparator));
    }

    private String removeWinPrefix(String s) {
        if (IS_WIN && s.startsWith("/")) {
            return s.substring(1);
        } else {
            return s;
        }
    }

    private String escape(Path path) {
        if (IS_WIN) {
            return "\"" + path + "\"";
        } else {
            return path.toString();
        }
    }
}
