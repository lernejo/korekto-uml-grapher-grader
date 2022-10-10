package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDef;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDiagram;

import java.util.ArrayList;
import java.util.List;

@FunctionalInterface
public interface ClassDiagramVerifier {

    static ClassDiagramVerifier classNameAndAnnotation() {
        return (types, diagram) -> {
            List<String> errors = new ArrayList<>();
            for (Class<?> type : types) {
                ClassDef classDef = diagram.getClass(type.getSimpleName());
                if (classDef == null) {
                    errors.add("Missing class: " + type.getSimpleName());
                } else if(type.isInterface() && !"interface".equals(classDef.annotation())) {
                    errors.add("Class " + type + " should be annotated with `interface`");
                }
            }
            return errors;
        };
    }

    List<String> verify(Class<?>[] types, ClassDiagram diagram);
}
