package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

public enum Visibility {
    PUBLIC,
    PRIVATE,
    PROTECTED,
    PACKAGE_PROTECTED,
    ;

    public static Visibility fromMermaid(String mermaidChar) {
        return switch (mermaidChar) {
            case "+" -> PUBLIC;
            case "-" -> PRIVATE;
            case "#" -> PROTECTED;
            case "~" -> PACKAGE_PROTECTED;
            default -> throw new IllegalArgumentException(mermaidChar + " no not match any visibility");
        };
    }
}
