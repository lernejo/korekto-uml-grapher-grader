package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

import com.github.lernejo.korekto.grader.uml_grapher.mermaid.TokenType;

public enum DirectionalRelationship {
    L_INHERITANCE(Relationship.INHERITANCE, false), R_INHERITANCE(Relationship.INHERITANCE, true),
    L_COMPOSITION(Relationship.COMPOSITION, false), R_COMPOSITION(Relationship.COMPOSITION, true),
    L_AGGREGATION(Relationship.AGGREGATION, false), R_AGGREGATION(Relationship.AGGREGATION, true),
    L_ASSOCIATION(Relationship.ASSOCIATION, false), R_ASSOCIATION(Relationship.ASSOCIATION, true),
    L_DEPENDENCY(Relationship.DEPENDENCY, false), R_DEPENDENCY(Relationship.DEPENDENCY, true),
    L_REALIZATION(Relationship.REALIZATION, false), R_REALIZATION(Relationship.REALIZATION, true),
    SOLID_LINK(Relationship.SOLID_LINK, null),
    DASHED_LINK(Relationship.DASHED_LINK, null),
    ;

    public final Relationship relationship;
    public final Boolean right;

    DirectionalRelationship(Relationship relationship, Boolean right) {
        this.relationship = relationship;
        this.right = right;
    }

    public static DirectionalRelationship from(TokenType tt) {
        return DirectionalRelationship.valueOf(tt.name());
    }
}
