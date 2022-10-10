package com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast;

import java.util.Arrays;

public enum Direction {
    TP, LR;

    public static Direction safeValueOf(String value) {
        return Arrays.stream(Direction.values())
            .filter(v -> v.name().equals(value))
            .findAny()
            .orElse(null);
    }
}
