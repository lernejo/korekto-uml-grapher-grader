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
import com.github.lernejo.korekto.toolkit.misc.ThrowingFunction;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
            AstParser parser = new AstParser(new Lexer(new CharStream(graph)));
            ClassDiagram diagram = new ModelTranslator().translate(parser.parseClassDiagram().get());
            List<String> errors = classDiagramVerifier.verify(types, diagram);
            if (!errors.isEmpty()) {
                errors.add("For graph: \n\n```\n" + graph + "```");
            }
            return result(errors, (1.0 - max(0, errors.size() * errorRatioPenalty)) * maxGrade);
        } catch (InvocationError e) {
            return result(List.of(e.getMessage()), 0.0);
        } catch (MissingTokenException e) {
            return result(List.of("Graph syntax error: " + e.getMessage()), 0.0);
        } catch (RuntimeException e) {
            return result(List.of("Unhandled error, report to maintainer: " + e.getMessage()), 0.0);
        }
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
