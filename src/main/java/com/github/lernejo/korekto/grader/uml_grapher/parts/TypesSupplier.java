package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relation;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relationship;
import com.github.lernejo.korekto.toolkit.misc.RandomSupplier;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ByteArrayClassLoader;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.FixedValue;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.Tool;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.stream.IntStream;

import static java.util.Collections.emptySet;

@FunctionalInterface
public interface TypesSupplier {

    static TypesSupplier simpleInterface() {
        return (c, cl) -> new Types(
            new ByteBuddy()
                .makeInterface()
                .name(c.newTypeId())
                .defineMethod(c.newId(), String.class, Modifier.PUBLIC)
                .withParameters(int.class)
                .withoutCode()
                .make()
                .load(cl)
                .getLoaded()
        );

    }

    static TypesSupplier simpleClass() {
        return (c, cl) -> new Types(
            new ByteBuddy()
                .subclass(Object.class)
                .name(c.newTypeId())
                .defineMethod(c.newId(), int.class, Modifier.PUBLIC)
                .withParameters(String.class)
                .intercept(FixedValue.value(43))
                .make()
                .load(cl)
                .getLoaded()
        );
    }

    static TypesSupplier randomJavaxType() {
        var javaxTypes = List.of(JavaCompiler.class, JavaFileObject.class, Tool.class);
        return (c, cl) -> new Types(javaxTypes.get(LaunchingContext.RANDOM.nextInt(javaxTypes.size())));
    }

    static TypesSupplier bottomUpHierarchy() {
        return (c, cl) -> {
            ByteArrayClassLoader byteArrayClassLoader = new ByteArrayClassLoader(cl, false, Map.of());
            RandomSupplier rs = LaunchingContext.RANDOM;
            Class<?> parent = new ByteBuddy()
                .makeInterface()
                .name("GrandMother_" + c.newTypeId())
                .make()
                .load(byteArrayClassLoader, ClassLoadingStrategy.Default.INJECTION)
                .getLoaded();
            List<Class<?>> subTypes = new ArrayList<>();
            Set<Relation> relations = new HashSet<>();
            for (int i = 0; i < 2 + rs.nextInt(3); i++) {
                String name = "Mother_" + c.newTypeId();
                subTypes.add(new ByteBuddy()
                    .makeInterface(parent)
                    .name(name)
                    .make()
                    .load(byteArrayClassLoader, ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded());
                relations.add(new Relation(name, Relationship.INHERITS_FROM, parent.getSimpleName()));
            }
            List<Class<?>> subSubTypes = new ArrayList<>();
            // we want the first subSubTypes to match at least one existing subTypes to avoid orphans when walking types from bottom to top
            PrimitiveIterator.OfInt seq = IntStream.concat(
                IntStream.range(0, subTypes.size()),
                IntStream.generate(() -> rs.nextInt(subTypes.size()))
            ).iterator();
            for (int i = 0; i < subTypes.size() + rs.nextInt(3); i++) {
                String name = "Daughter_" + c.newTypeId();
                Class<?> localParent = subTypes.get(seq.nextInt());
                subSubTypes.add(new ByteBuddy()
                    .makeInterface(localParent)
                    .name(name)
                    .make()
                    .load(byteArrayClassLoader, ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded());
                relations.add(new Relation(name, Relationship.INHERITS_FROM, localParent.getSimpleName()));
            }
            for (int i = 0; i < 2 + rs.nextInt(3); i++) {
                var startIndex = rs.nextInt(subTypes.size());
                var interfacesToImplement = subTypes.subList(startIndex, Math.max(subTypes.size(), startIndex + rs.nextInt(subTypes.size())));
                String name = "Son_" + c.newTypeId();
                subSubTypes.add(new ByteBuddy()
                    .subclass(Object.class)
                    .implement(interfacesToImplement)
                    .name(name)
                    .make()
                    .load(byteArrayClassLoader, ClassLoadingStrategy.Default.INJECTION)
                    .getLoaded());
                for (Class<?> localParent : interfacesToImplement) {
                    relations.add(new Relation(name, Relationship.REALIZES, localParent.getSimpleName()));
                }
            }
            List<Class<?>> allTypes = new ArrayList<>();
            allTypes.add(parent);
            allTypes.addAll(subTypes);
            allTypes.addAll(subSubTypes);
            return new Types(allTypes, subSubTypes, relations);
        };
    }

    Types supply(LaunchingContext context, ClassLoader tmpClassLoader);

    record Types(Class<?>[] allTypes, Class<?>[] selectedTypes, Set<Relation> relations) {
        public Types(Class<?>[] types) {
            this(types, types, emptySet());
        }

        public Types(Class<?> type) {
            this(new Class<?>[]{type});
        }

        public Types(Collection<Class<?>> allTypes, Collection<Class<?>> selectedTypes, Set<Relation> relations) {
            this(allTypes.toArray(new Class<?>[0]), selectedTypes.toArray(new Class<?>[0]), relations);
        }
    }
}
