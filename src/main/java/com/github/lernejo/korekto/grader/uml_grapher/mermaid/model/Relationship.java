package com.github.lernejo.korekto.grader.uml_grapher.mermaid.model;

public enum Relationship {

    INHERITS_FROM,
    COMPOSES,
    AGGREGATES,
    ASSOCIATES_WITH,
    IS_SOLID_LINKED_TO(true),
    DEPENDS_ON,
    REALIZES,
    IS_DASHED_LINKED(true),
    ;

    public final boolean reversible;

    Relationship() {
        this(false);
    }

    Relationship(boolean reversible) {
        this.reversible = reversible;
    }
}
