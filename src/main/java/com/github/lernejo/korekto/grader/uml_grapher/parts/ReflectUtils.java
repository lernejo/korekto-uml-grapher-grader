package com.github.lernejo.korekto.grader.uml_grapher.parts;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ReflectUtils {

    @SuppressWarnings("unchecked")
    public static <T extends Enum> Class<T> loadEnum(ClassLoader classLoader, String name) {
        try {
            return (Class<T>) classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new InvocationError("Missing enum named `" + name + "`");
        }
    }

    public static Class<?> loadClass(ClassLoader classLoader, String name) {
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            throw new InvocationError("Missing class named `" + name + "`");
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
        try {
            return clazz.getConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            String parameters = Arrays.stream(parameterTypes).map(Object::toString).collect(Collectors.joining(", "));
            throw new InvocationError("Missing constructor `" + clazz.getName() + "#<init>(" + parameters + ")`");
        }
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?> returnType, Class<?>... parameterTypes) {
        Method method;
        try {
            method = clazz.getMethod(name, parameterTypes);
        } catch (NoSuchMethodException e) {
            String parameters = Arrays.stream(parameterTypes).map(Object::toString).collect(Collectors.joining(", "));
            throw new InvocationError("Missing method `" + clazz.getName() + "#" + name + "(" + parameters + ")`");
        }
        if (method.getReturnType() != returnType) {
            String parameters = Arrays.stream(parameterTypes).map(Object::toString).collect(Collectors.joining(", "));
            throw new InvocationError("Method `" + clazz.getName() + "#" + name + "(" + parameters + ")` should return a `" + returnType.getSimpleName() + "`");
        }
        return method;
    }

    public static <T extends Enum<T>> T getEnumValue(Class<T> enumClass, String valueName) {
        try {
            return Enum.valueOf(enumClass, valueName);
        } catch (IllegalArgumentException e) {
            throw new InvocationError("Missing value `" + valueName + "` in enum `" + enumClass.getName() + "`");
        }
    }

    public static Object instantiate(Constructor<?> constructor, Object... parameters) {
        try {
            return constructor.newInstance(parameters);
        } catch (ReflectiveOperationException e) {
            throw new InvocationError("Failed to invoke constructor `" + constructor + "`: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Method method, Object obj, Object... args) {
        try {
            return (T) method.invoke(obj, args);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new InvocationError("Failed to invoke method `" + method + "`: " + e.getMessage());
        }
    }
}
