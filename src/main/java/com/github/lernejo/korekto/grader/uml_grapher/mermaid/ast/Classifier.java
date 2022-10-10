package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

public enum Classifier {
    ABSTRACT,
    STATIC,
    ;

    public static Classifier fromMermaid(String mermaidChar) {
        return switch (mermaidChar) {
            case "*" -> ABSTRACT;
            case "$" -> STATIC;
            default -> throw new IllegalArgumentException(mermaidChar + " no not match any classifier");
        };
    }
}
