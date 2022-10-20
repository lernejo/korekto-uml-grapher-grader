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
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.github.lernejo.korekto.toolkit.misc.ThrowingFunction.sneaky;
import static java.lang.Math.max;

public record ClassDiagramRenderingVerification(String name, Double maxGrade, double errorRatioPenalty,
                                                TypesSupplier typesSupplier,
                                                ClassDiagramVerifier classDiagramVerifier) implements PartGrader<LaunchingContext> {

    private static final String GRAPH_CLASS_NAME = "fr.lernejo.umlgrapher.UmlGraph";
    private static final String GRAPH_TYPE_CLASS_NAME = "fr.lernejo.umlgrapher.GraphType";

    @Override
    public GradePart grade(LaunchingContext context) {
        if (context.hasCompilationFailed()) {
            return result(List.of("Not available when there is compilation failures"), 0.0D);
        }

        ClassLoader mavenClassLoader = context.getMavenMainClassloader();
        ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(mavenClassLoader, false, Map.of(), ByteArrayClassLoader.PersistenceHandler.MANIFEST);
        try {
            TypesSupplier.Types types = typesSupplier.supply(context, byteArrayClassLoader);
            ClassLoader classLoaderForInvocation = context.newTmpMavenChildClassLoader(types.directory());
            String graph = invoke(classLoaderForInvocation, reloadFromAnotherClassloader(types.selectedTypes(), classLoaderForInvocation));
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

    private Class<?>[] reloadFromAnotherClassloader(Class<?>[] selectedTypes, ClassLoader classLoader) {
        return Arrays.stream(selectedTypes)
            .map(Class::getName)
            .map(sneaky(n -> Class.forName(n, true, classLoader)))
            .toArray(Class<?>[]::new);
    }

    public String invoke(ClassLoader classLoader, Class<?>[] typesToGraph) throws InvocationError {
        Class<?> umlGraphClass = ReflectUtils.loadClass(classLoader, GRAPH_CLASS_NAME);
        var umlGraphTypeClass = ReflectUtils.loadEnum(classLoader, GRAPH_TYPE_CLASS_NAME);
        Constructor<?> constructor = ReflectUtils.getConstructor(umlGraphClass, Class[].class);
        Method renderMethod = ReflectUtils.getMethod(umlGraphClass, "as", String.class, umlGraphTypeClass);

        Object umlGraphObject = ReflectUtils.instantiate(constructor, (Object) typesToGraph);
        Enum<?> mermaidValue = ReflectUtils.getEnumValue(umlGraphTypeClass, "Mermaid");

        return ReflectUtils.invokeMethod(renderMethod, umlGraphObject, mermaidValue);
    }
}
