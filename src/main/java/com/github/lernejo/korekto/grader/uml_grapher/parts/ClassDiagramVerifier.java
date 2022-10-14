package com.github.lernejo.korekto.grader.uml_grapher.parts;

import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDef;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDiagram;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassField;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassMethod;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Classifier;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.MethodParameter;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Relation;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.Visibility;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
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

    static ClassDiagramVerifier members() {
        return (types, diagram) -> {
            List<String> errors = new ArrayList<>();

            for (Class<?> type : types.allTypes()) {
                for (Field field : type.getDeclaredFields()) {
                    if (field.isSynthetic()) {
                        continue;
                    }
                    ClassDef classDef = diagram.getClass(type.getSimpleName());
                    ClassField expectedField = createField(field);
                    if (!classDef.fields().contains(expectedField)) {
                        errors.add("On class " + type.getSimpleName() + ", missing field: " + expectedField);
                    }
                }
                for (Method method : type.getDeclaredMethods()) {
                    if (method.isSynthetic()) {
                        continue;
                    }
                    ClassDef classDef = diagram.getClass(type.getSimpleName());
                    ClassMethod expectedMethod = createMethod(method);
                    if (!classDef.hasMethod(expectedMethod)) {
                        errors.add("On class " + type.getSimpleName() + ", missing method: " + expectedMethod);
                    }
                }
            }
            return errors;
        };
    }

    static ClassMethod createMethod(Method method) {
        return new ClassMethod(
            computeVisibility(method),
            method.getReturnType().getSimpleName(),
            method.getName(),
            computeClassifier(method),
            Arrays.stream(method.getParameters())
                .map(ClassDiagramVerifier::createParameter)
                .toList()
        );
    }

    static MethodParameter createParameter(Parameter parameter) {
        return new MethodParameter(parameter.getType().getSimpleName(), parameter.getName());
    }

    static ClassField createField(Field field) {
        return new ClassField(computeVisibility(field), field.getType().getSimpleName(), field.getName(), computeClassifier(field));
    }

    static Visibility computeVisibility(Member member) {
        if (Modifier.isPublic(member.getModifiers())) {
            return Visibility.PUBLIC;
        } else if (Modifier.isPrivate(member.getModifiers())) {
            return Visibility.PRIVATE;
        } else if (Modifier.isProtected(member.getModifiers())) {
            return Visibility.PROTECTED;
        } else {
            return Visibility.PACKAGE_PROTECTED;
        }
    }

    static Classifier computeClassifier(Member member) {
        if (Modifier.isAbstract(member.getModifiers())) {
            return Classifier.ABSTRACT;
        } else if (Modifier.isStatic(member.getModifiers())) {
            return Classifier.STATIC;
        } else {
            return null;
        }
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
