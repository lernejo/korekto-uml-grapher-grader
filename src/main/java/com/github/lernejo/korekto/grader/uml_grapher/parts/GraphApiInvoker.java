package com.github.lernejo.korekto.grader.uml_grapher.parts;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GraphApiInvoker {

    private static final String GRAPH_CLASS_NAME = "fr.lernejo.umlgrapher.UmlGraph";
    private static final String GRAPH_TYPE_CLASS_NAME = "fr.lernejo.umlgrapher.GraphType";

    @SuppressWarnings("unchecked")
    private static <T extends Enum> Class<T> loadGraphTypeClass(ClassLoader classLoader) {
        try {
            return (Class<T>) classLoader.loadClass(GRAPH_TYPE_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new InvocationError("Missing class named `" + GRAPH_TYPE_CLASS_NAME + "`");
        }
    }

    private static Class<?> loadGraphClass(ClassLoader classLoader) {
        try {
            return classLoader.loadClass(GRAPH_CLASS_NAME);
        } catch (ClassNotFoundException e) {
            throw new InvocationError("Missing class named `" + GRAPH_CLASS_NAME + "`");
        }
    }

    private static Constructor<?> loadGraphClassConstructor(Class<?> umlGraphClass) {
        try {
            return umlGraphClass.getConstructor(Class[].class);
        } catch (NoSuchMethodException e) {
            throw new InvocationError("Missing constructor `" + GRAPH_CLASS_NAME + "#<init>(Class<?>[] classes)`");
        }
    }

    private static Method loadRenderMethod(Class<?> umlGraphClass, Class<?> umlGraphTypeClass) {
        Method renderMethod;
        try {
            renderMethod = umlGraphClass.getMethod("as", umlGraphTypeClass);
        } catch (NoSuchMethodException e) {
            throw new InvocationError("Missing method `" + GRAPH_CLASS_NAME + "#as(" + GRAPH_TYPE_CLASS_NAME + " type)`");
        }
        if (renderMethod.getReturnType() != String.class) {
            throw new InvocationError("Method `" + GRAPH_CLASS_NAME + "#as(" + GRAPH_TYPE_CLASS_NAME + " type)` should return a `String`");
        }
        return renderMethod;
    }

    private static Object instantiate(Constructor<?> constructor, Object parameter) {
        try {
            return constructor.newInstance(parameter);
        } catch (ReflectiveOperationException e) {
            throw new InvocationError("Failed to invoke constructor `" + GRAPH_CLASS_NAME + "#<init>(Class<?>[] classes)`: " + e.getMessage());
        }
    }

    private static <T extends Enum<T>> T loadMermaidGraphType(Class<T> umlGraphTypeClass) {
        try {
            return Enum.valueOf(umlGraphTypeClass, "Mermaid");
        } catch (IllegalArgumentException e) {
            throw new InvocationError("Missing value `Mermaid` in enum `" + GRAPH_TYPE_CLASS_NAME + "`");
        }
    }

    public String invoke(ClassLoader classLoader, Class<?>... typesToGraph) throws InvocationError {
        Class<?> umlGraphClass = loadGraphClass(classLoader);
        var umlGraphTypeClass = loadGraphTypeClass(classLoader);
        Constructor<?> constructor = loadGraphClassConstructor(umlGraphClass);
        Method renderMethod = loadRenderMethod(umlGraphClass, umlGraphTypeClass);

        Object umlGraphObject = instantiate(constructor, (Object) typesToGraph);
        Enum<?> mermaidValue = loadMermaidGraphType(umlGraphTypeClass);

        try {
            return  (String) renderMethod.invoke(umlGraphObject, mermaidValue);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new InvocationError("Failed to invoke constructor `" + GRAPH_CLASS_NAME + "#<init>(Class<?>[] classes)`: " + e.getMessage());
        } catch (RuntimeException e) {
            throw new InvocationError("Unhandled error, report to maintainer: " + e.getMessage());
        }
    }

    static class InvocationError extends RuntimeException {

        public InvocationError(String message) {
            super(message);
        }
    }
}
