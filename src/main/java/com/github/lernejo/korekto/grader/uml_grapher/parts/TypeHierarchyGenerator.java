package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.LaunchingContext;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relation;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relationship;
import com.github.lernejo.korekto.toolkit.misc.RandomSupplier;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import static com.github.lernejo.korekto.grader.uml_grapher.parts.ByteBuddys.*;

public class TypeHierarchyGenerator {

    public static TypeHierarchy generate(ClassLoader cl, LaunchingContext c) {
        Path directory = ByteBuddys.createTempClassDirectory(c.randomSupplier());
        Class<?> parent = saveToFileAndLoad(
            BYTE_BUDDY
                .makeInterface()
                .name(GEN_PACKAGE + "GrandMother_" + c.newTypeId())
                .make(),
            directory,
            cl
        );
        List<Class<?>> subTypes = new ArrayList<>();
        Set<Relation> relations = new HashSet<>();
        for (int i = 0; i < 2 + c.randomSupplier().nextInt(3); i++) {
            String name = "Mother_" + c.newTypeId();
            subTypes.add(
                saveToFileAndLoad(
                    BYTE_BUDDY
                        .makeInterface(parent)
                        .name(GEN_PACKAGE + name)
                        .make(),
                    directory,
                    cl)
            );
            relations.add(new Relation(name, Relationship.INHERITS_FROM, parent.getSimpleName()));
        }
        List<Class<?>> subSubTypes = new ArrayList<>();
        // we want the first subSubTypes to match at least one existing subTypes to avoid orphans when walking types from bottom to top
        PrimitiveIterator.OfInt seq = IntStream.concat(
            IntStream.range(0, subTypes.size()),
            IntStream.generate(() -> c.randomSupplier().nextInt(subTypes.size()))
        ).iterator();
        for (int i = 0; i < subTypes.size() + c.randomSupplier().nextInt(3); i++) {
            String name = "Daughter_" + c.newTypeId();
            Class<?> localParent = subTypes.get(seq.nextInt());
            subSubTypes.add(
                saveToFileAndLoad(
                    BYTE_BUDDY
                        .makeInterface(localParent)
                        .name(GEN_PACKAGE + name)
                        .make(),
                    directory,
                    cl)
            );
            relations.add(new Relation(name, Relationship.INHERITS_FROM, localParent.getSimpleName()));
        }
        for (int i = 0; i < 2 + c.randomSupplier().nextInt(3); i++) {
            var startIndex = c.randomSupplier().nextInt(subTypes.size());
            int endIndex = subTypes.size();
            var interfacesToImplement = subTypes.subList(startIndex, endIndex);
            String name = "Son_" + c.newTypeId();
            subSubTypes.add(
                saveToFileAndLoad(
                    BYTE_BUDDY
                        .subclass(Object.class)
                        .implement(interfacesToImplement)
                        .name(GEN_PACKAGE + name)
                        .make(),
                    directory,
                    cl)
            );
            for (Class<?> localParent : interfacesToImplement) {
                relations.add(new Relation(name, Relationship.REALIZES, localParent.getSimpleName()));
            }
        }
        return new TypeHierarchy(parent, subTypes, subSubTypes, relations, directory);
    }

    record TypeHierarchy(Class<?> parent, List<Class<?>> subTypes, List<Class<?>> subSubTypes,
                         Set<Relation> relations, Path directory) {
        public List<Class<?>> allTypes() {
            List<Class<?>> allTypes = new ArrayList<>();
            allTypes.add(parent);
            allTypes.addAll(subTypes);
            allTypes.addAll(subSubTypes);
            return allTypes;
        }
    }
}
