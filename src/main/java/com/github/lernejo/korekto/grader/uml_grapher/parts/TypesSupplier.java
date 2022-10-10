package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;

import java.lang.reflect.Modifier;

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

    Class<?>[] supply(LaunchingContext context, ClassLoader tmpClassLoader);
}
