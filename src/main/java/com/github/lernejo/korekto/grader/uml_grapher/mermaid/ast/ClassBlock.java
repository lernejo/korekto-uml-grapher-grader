package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.List;
import java.util.Set;

@RecordBuilder.Options(useImmutableCollections = true, addSingleItemCollectionBuilders = true)
@RecordBuilder
public record ClassBlock(String name, String annotation, List<ClassBlockItem> items) implements Item {
    @Override
    public ItemType _type() {
        return ItemType.CLASS_BLOCK;
    }
}
