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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

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

        ClassLoader tmpClassLoader = context.newTmpMavenChildClassLoader();
        try {
            TypesSupplier.Types types = typesSupplier.supply(context, tmpClassLoader);
            String graph = invoke(tmpClassLoader, types);
            AstParser parser = new AstParser(new Lexer(new CharStream(graph)));
            ClassDiagram diagram = new ModelTranslator().translate(parser.parseClassDiagram().get());
            List<String> errors = classDiagramVerifier.verify(types, diagram);
            return result(errors, (1.0 - max(0, errors.size() * errorRatioPenalty)) * maxGrade);
        } catch (InvocationError e) {
            return result(List.of(e.getMessage()), 0.0);
        } catch (MissingTokenException e) {
            return result(List.of("Graph syntax error: " + e.getMessage()), 0.0);
        } catch (RuntimeException e) {
            return result(List.of("Unhandled error, report to maintainer: " + e.getMessage()), 0.0);
        }
    }

    public String invoke(ClassLoader classLoader, TypesSupplier.Types typesToGraph) throws InvocationError {
        Class<?> umlGraphClass = ReflectUtils.loadClass(classLoader, GRAPH_CLASS_NAME);
        var umlGraphTypeClass = ReflectUtils.loadEnum(classLoader, GRAPH_TYPE_CLASS_NAME);
        Constructor<?> constructor = ReflectUtils.getConstructor(umlGraphClass, Class[].class);
        Method renderMethod = ReflectUtils.getMethod(umlGraphClass, "as", String.class, umlGraphTypeClass);

        Object umlGraphObject = ReflectUtils.instantiate(constructor, (Object) typesToGraph.selectedTypes());
        Enum<?> mermaidValue = ReflectUtils.getEnumValue(umlGraphTypeClass, "Mermaid");

        return ReflectUtils.invokeMethod(renderMethod, umlGraphObject, mermaidValue);
    }
}
