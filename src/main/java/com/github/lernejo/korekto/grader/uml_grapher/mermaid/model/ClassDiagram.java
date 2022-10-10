package com.github.lernejo.korekto.grader.uml_grapher.mermaid.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
@RecordBuilder
public record ClassDiagram(Set<ClassDef> classes, Set<Relation> relations) {
    public Set<ClassDef> classesWithAnnotation(String annotation) {
        return Set.copyOf(classes.stream().filter(c -> Objects.equals(c.annotation(), annotation)).collect(Collectors.toSet()));
    }

    public ClassDef getClass(String name) {
        return classes.stream().filter(c -> c.name().equals(name)).findFirst().orElse(null);
    }
}
