package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

public record ClassLineMethod() implements Item {
    @Override
    public ItemType _type() {
        return ItemType.CLASS_LINE_METHOD;
    }
}
