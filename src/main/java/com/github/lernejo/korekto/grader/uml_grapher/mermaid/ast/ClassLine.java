package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

public record ClassLine(String name) implements Item {
    @Override
    public ItemType _type() {
        return ItemType.CLASS_LINE;
    }
}
