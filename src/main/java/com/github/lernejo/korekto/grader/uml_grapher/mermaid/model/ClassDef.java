package com.github.lernejo.korekto.grader.uml_grapher.mermaid.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Set;

@RecordBuilder.Options(useImmutableCollections = true)
@RecordBuilder
public record ClassDef(String name, String annotation, Set<Field> fields, Set<Method> methods) {
}
