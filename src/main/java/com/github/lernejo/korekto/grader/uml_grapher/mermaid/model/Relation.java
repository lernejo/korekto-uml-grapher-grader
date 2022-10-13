package com.github.lernejo.korekto.grader.uml_grapher.mermaid.model;

import java.util.Objects;

public record Relation(String c1, Relationship relationship, String c2) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Relation relation = (Relation) o;
        if (relationship != relation.relationship) return false;

        boolean originalEquality = Objects.equals(c1, relation.c1) && Objects.equals(c2, relation.c2);
        if (relationship.reversible) {
            return originalEquality
                || Objects.equals(c1, relation.c2) && Objects.equals(c2, relation.c1);
        } else {
            return originalEquality;
        }
    }

    public boolean involves(String oc1, String oc2) {
        return Objects.equals(c1, oc1) && Objects.equals(c2, oc2)
            || Objects.equals(c1, oc2) && Objects.equals(c2, oc1);
    }
}
