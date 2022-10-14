package com.github.lernejo.korekto.grader.uml_grapher.mermaid.model;

import io.soabase.recordbuilder.core.RecordBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@RecordBuilder.Options(useImmutableCollections = true)
@RecordBuilder
public record ClassDef(String name, String annotation, Set<ClassField> fields, Set<ClassMethod> methods) {
    public boolean hasMethod(ClassMethod cm) {
        Optional<ClassMethod> potentialMatch = methods.stream()
            .filter(m -> m.name().equals(cm.name()))
            .filter(m -> m.visibility().equals(cm.visibility()))
            .filter(m -> m.parameters().equals(m.parameters()))
            .filter(m -> m.type().equals(cm.type()))
            .findFirst();
        if (potentialMatch.isEmpty()) {
            return false;
        }

        ClassMethod match = potentialMatch.get();
        // Classifier abstract on interface is optional
        return Objects.equals(match.classifier(), cm.classifier())
            || cm.classifier() == Classifier.ABSTRACT && "interface".equals(annotation) && match.classifier() == null;
    }
}
