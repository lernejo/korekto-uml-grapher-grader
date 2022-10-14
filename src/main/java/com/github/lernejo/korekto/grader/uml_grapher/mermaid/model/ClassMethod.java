package com.github.lernejo.korekto.grader.uml_grapher.mermaid.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder.Options(useImmutableCollections = true)
@RecordBuilder
public record ClassMethod(Visibility visibility, String type, String name, Classifier classifier, List<MethodParameter> parameters) {
}
