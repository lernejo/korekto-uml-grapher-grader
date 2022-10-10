package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Set;

@RecordBuilder
public record ClassDiagram(Direction direction, Set<Item> items) {

}
