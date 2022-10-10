package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;

@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
@RecordBuilder
public record ClassBlockMethod(Visibility visibility, String type, String name, Classifier classifier, List<Parameter> parameters) implements ClassBlockItem {
}
