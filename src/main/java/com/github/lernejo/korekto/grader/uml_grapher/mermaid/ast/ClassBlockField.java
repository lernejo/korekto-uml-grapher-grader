package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

import io.soabase.recordbuilder.core.RecordBuilder;

@RecordBuilder
public record ClassBlockField(Visibility visibility, String type, String name, Classifier classifier) implements ClassBlockItem {
}
