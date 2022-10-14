package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.ClassBlock;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.ClassBlockField;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.ClassBlockItem;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.ClassBlockMethod;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.ClassLine;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.DirectionalRelationship;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.Item;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDefBuilder;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDiagram;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDiagramBuilder;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassMethod;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Classifier;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassField;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.MethodParameter;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relation;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relationship;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Visibility;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ModelTranslator {

    public ClassDiagram translate(com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.ClassDiagram cd) {
        var builder = ClassDiagramBuilder.builder();
        Map<String, ClassDefBuilder> classBuilders = new HashMap<>();
        for (Item item : cd.items()) {
            // TODO move to exhaustive pattern-matching after Java 18+
            if (item instanceof ClassLine cl) {
                classBuilders.computeIfAbsent(cl.name(), ClassDefBuilder.builder()::name);
            } else if (item instanceof ClassBlock cb) {
                classBuilders.computeIfAbsent(cb.name(), ClassDefBuilder.builder()::name)
                    .annotation(cb.annotation())
                    .fields(extractFields(cb.items()))
                    .methods(extractMethods(cb.items()));
            } else if (item instanceof com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.Relation r) {
                final Relationship relationship = translate(r.relationship());
                final String c1;
                final String c2;
                if (r.relationship().right != null && r.relationship().right) {
                    c1 = r.c1();
                    c2 = r.c2();
                } else {
                    c1 = r.c2();
                    c2 = r.c1();
                }
                builder.addRelations(new Relation(c1, relationship, c2));
            }
        }
        classBuilders.values()
            .stream()
            .map(b -> b.build())
            .forEach(builder::addClasses);
        return builder.build();
    }

    private Set<ClassMethod> extractMethods(List<ClassBlockItem> items) {
        Set<ClassMethod> methods = new HashSet<>();
        for (ClassBlockItem item : items) {
            if (item instanceof ClassBlockMethod m) {
                methods.add(new ClassMethod(translate(m.visibility()), m.type(), m.name(), translate(m.classifier()), translate(m.parameters())));
            }
        }
        return methods;
    }

    private Set<ClassField> extractFields(List<ClassBlockItem> items) {
        Set<ClassField> fields = new HashSet<>();
        for (ClassBlockItem item : items) {
            if (item instanceof ClassBlockField f) {
                fields.add(new ClassField(translate(f.visibility()), f.type(), f.name(), translate(f.classifier())));
            }
        }
        return fields;
    }

    private List<MethodParameter> translate(List<com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.Parameter> parameters) {
        return parameters.stream().map(this::translate).toList();
    }

    private MethodParameter translate(com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.Parameter parameter) {
        return new MethodParameter(parameter.type(), parameter.name());
    }

    private Relationship translate(DirectionalRelationship relationship) {
        return switch (relationship.relationship) {
            case INHERITANCE -> Relationship.INHERITS_FROM;
            case COMPOSITION -> Relationship.COMPOSES;
            case AGGREGATION -> Relationship.AGGREGATES;
            case ASSOCIATION -> Relationship.ASSOCIATES_WITH;
            case DEPENDENCY -> Relationship.DEPENDS_ON;
            case REALIZATION -> Relationship.REALIZES;
            case SOLID_LINK -> Relationship.IS_SOLID_LINKED_TO;
            case DASHED_LINK -> Relationship.IS_DASHED_LINKED;
        };
    }

    private Visibility translate(com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.Visibility visibility) {
        return Visibility.valueOf(visibility.name());
    }

    private Classifier translate(com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.Classifier classifier) {
        return classifier == null ? null : Classifier.valueOf(classifier.name());
    }
}
