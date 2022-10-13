package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.Tool;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.List;

@FunctionalInterface
public interface TypesSupplier {

    static TypesSupplier simpleInterface() {
        return (c, cl) -> new Class<?>[]{
            new ByteBuddy()
                .makeInterface()
                .name(c.newTypeId())
                .defineMethod(c.newId(), String.class, Modifier.PUBLIC)
                .withParameters(int.class)
                .withoutCode()
                .make()
                .load(cl)
                .getLoaded()
        };

    }

    static TypesSupplier simpleClass() {
        return (c, cl) -> new Class<?>[]{
            new ByteBuddy()
                .subclass(Object.class)
                .name(c.newTypeId())
                .defineMethod(c.newId(), int.class, Modifier.PUBLIC)
                .withParameters(String.class)
                .intercept(FixedValue.value(43))
                .make()
                .load(cl)
                .getLoaded()
        };
    }

    static TypesSupplier randomJavaxType() {
        var javaxTypes = List.of(JavaCompiler.class, JavaFileObject.class, Tool.class);
        return (c, cl) -> new Class<?>[]{
            javaxTypes.get(LaunchingContext.RANDOM.nextInt(javaxTypes.size()))
        };
    }

    Class<?>[] supply(LaunchingContext context, ClassLoader tmpClassLoader);
}
