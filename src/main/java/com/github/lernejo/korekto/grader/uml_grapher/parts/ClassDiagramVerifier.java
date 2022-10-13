package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDef;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDiagram;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@FunctionalInterface
public interface ClassDiagramVerifier {

    static ClassDiagramVerifier classNameAndAnnotation() {
        return (types, diagram) -> {
            List<String> errors = new ArrayList<>();
            for (Class<?> type : types.allTypes()) {
                ClassDef classDef = diagram.getClass(type.getSimpleName());
                if (classDef == null) {
                    errors.add("Missing class: " + type.getSimpleName());
                } else if (type.isInterface() && !"interface".equals(classDef.annotation())) {
                    errors.add("Class " + type + " should be annotated with `interface`");
                }
            }
            return errors;
        };
    }

    static ClassDiagramVerifier relations() {
        return (types, diagram) -> {
            List<String> errors = new ArrayList<>();
            for (Relation relation : types.relations()) {
                Set<Relation> relatedRelations = diagram.getRelationsInvolving(relation.c1(), relation.c2());
                if (relatedRelations.isEmpty()) {
                    errors.add("Missing relation: " + relation);
                } else if (!relatedRelations.contains(relation)) {
                    errors.add("Missing relation: " + relation + ", found: " + relatedRelations);
                }
            }
            return errors;
        };
    }

    List<String> verify(TypesSupplier.Types types, ClassDiagram diagram);

    default ClassDiagramVerifier and(ClassDiagramVerifier other) {
        return (types, diagram) -> {
            List<String> errors = new ArrayList<>();
            errors.addAll(verify(types, diagram));
            errors.addAll(other.verify(types, diagram));
            return errors;
        };
    }
}
