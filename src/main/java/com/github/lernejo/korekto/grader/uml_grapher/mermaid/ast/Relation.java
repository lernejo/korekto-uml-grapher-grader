package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

public record Relation(String c1, DirectionalRelationship relationship, String c2) implements Item {
    @Override
    public ItemType _type() {
        return ItemType.RELATION;
    }
}
