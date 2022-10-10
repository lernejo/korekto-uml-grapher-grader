package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

public sealed interface Item permits ClassBlock, ClassLine, ClassLineField, ClassLineMethod, Relation {

    ItemType _type();

    enum ItemType {
        CLASS_BLOCK,
        CLASS_LINE,
        CLASS_LINE_FIELD,
        CLASS_LINE_METHOD,
        RELATION,
        ;
    }
}
