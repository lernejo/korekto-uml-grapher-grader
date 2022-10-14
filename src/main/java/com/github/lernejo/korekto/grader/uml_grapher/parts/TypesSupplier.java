package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relation;
import net.bytebuddy.implementation.FixedValue;

import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static com.github.lernejo.korekto.grader.uml_grapher.parts.ByteBuddys.*;
import static java.util.Collections.emptySet;

@FunctionalInterface
public interface TypesSupplier {

    static TypesSupplier simpleInterface() {
        return (c, cl) -> {
            Path directory = ByteBuddys.createTempClassDirectory(c.randomSupplier());
            return new Types(
                saveToFileAndLoad(
                    BYTE_BUDDY
                        .makeInterface()
                        .name(GEN_PACKAGE + c.newTypeId())
                        .defineMethod(c.newId(), String.class, Modifier.PUBLIC)
                        .withParameters(int.class)
                        .withoutCode()
                        .make()
                    ,
                    directory, cl),
                directory);
        };

    }

    static TypesSupplier simpleClasses(int count) {
        return (c, cl) -> {
            Path directory = ByteBuddys.createTempClassDirectory(c.randomSupplier());
            Class<?>[] types = IntStream.range(0, count)
                .mapToObj(i -> saveToFileAndLoad(
                    BYTE_BUDDY
                        .subclass(Object.class)
                        .name(GEN_PACKAGE + c.newTypeId())
                        .defineMethod(c.newId(), int.class, Modifier.PUBLIC)
                        .withParameters(String.class)
                        .intercept(FixedValue.value(43))
                        .make(),
                    directory,
                    cl))
                .toArray(Class<?>[]::new);
            return new Types(types, directory);
        };
    }

    static TypesSupplier bottomUpHierarchy() {
        return (c, cl) -> {
            var typeHierarchy = TypeHierarchyGenerator
                .generate(cl, c);

            return new Types(typeHierarchy.allTypes(), typeHierarchy.subSubTypes(), typeHierarchy.relations(), typeHierarchy.directory());
        };
    }

    static TypesSupplier topToBottomHierarchy() {
        return (c, cl) -> {
            var typeHierarchy = TypeHierarchyGenerator
                .generate(cl, c);

            return new Types(typeHierarchy.allTypes(), List.of(typeHierarchy.parent()), typeHierarchy.relations(), typeHierarchy.directory());
        };
    }

    static TypesSupplier singleton() {
        return (c, cl) -> DesignPatternGenerator.generateSingleton(cl, c);
    }

    static TypesSupplier proxy() {
        return (c, cl) -> DesignPatternGenerator.generateProxy(cl, c);
    }

    Types supply(LaunchingContext context, ClassLoader tmpClassLoader);

    record Types(Class<?>[] allTypes, Class<?>[] selectedTypes, Set<Relation> relations, Path directory) {
        public Types(Class<?>[] types, Path directory) {
            this(types, types, emptySet(), directory);
        }

        public Types(Class<?> type, Path directory) {
            this(new Class<?>[]{type}, directory);
        }

        public Types(Collection<Class<?>> allTypes, Collection<Class<?>> selectedTypes, Set<Relation> relations, Path directory) {
            this(allTypes.toArray(new Class<?>[0]), selectedTypes.toArray(new Class<?>[0]), relations, directory);
        }
    }
}
