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
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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
            ClassLoader mavenClassLoader = context.getMavenMainClassloader();
            ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(mavenClassLoader, false, Map.of(), ByteArrayClassLoader.PersistenceHandler.MANIFEST);
            TypesSupplier.Types types = typesSupplier.supply(context, byteArrayClassLoader);
            List<URL> classPath = context.getMavenClassPath();
            String cp = buildClassPath(classPath, types.directory());

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
            if (graph.isBlank()) {
                return result(List.of("Empty graph returned"), 0.0);
            }

            try {
                AstParser parser = new AstParser(new Lexer(new CharStream(graph)));
                var classDiagramAst = parser.parseClassDiagram();
                if (classDiagramAst.isEmpty()) {
                    return result(List.of("Unparseable graph returned", graphContent(graph)), 0.0);
                }
                ClassDiagram diagram = new ModelTranslator().translate(classDiagramAst.get());
                List<String> errors = classDiagramVerifier.verify(types, diagram);
                if (!errors.isEmpty()) {
                    errors.add(graphContent(graph));
                }
                return result(errors, (1.0 - max(0, errors.size() * errorRatioPenalty)) * maxGrade);
            } catch (MissingTokenException e) {
                return result(List.of("Graph syntax error: " + e.getMessage(), graphContent(graph)), 0.0);
            }
        } catch (InvocationError e) {
            return result(List.of(e.getMessage()), 0.0);
        } catch (RuntimeException e) {
            e.printStackTrace();
            String indentedStackTrace = Arrays.stream(e.getStackTrace())
                .limit(6)
                .map(String::valueOf)
                .collect(Collectors.joining("\n        ", "\n        ", ""));
            return result(List.of("Unhandled error, report to maintainer. " + e.getClass().getSimpleName() + ": " + e.getMessage() + indentedStackTrace), 0.0);
        }
    }

    private String graphContent(String graph) {
        return "For graph: \n\n```\n" + graph + "```";
    }

    private String buildClassPath(List<URL> classPath, Path... additionalPaths) {
        return Stream.concat(
            classPath.stream().map(URL::getPath).map(this::removeWinPrefix),
            Stream.of(additionalPaths).map(String::valueOf)
        ).collect(Collectors.joining(File.pathSeparator));
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
