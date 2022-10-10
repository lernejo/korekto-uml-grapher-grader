package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

import java.util.Arrays;
import java.util.Set;

public record DynamicTokenDefinition(TokenType type, Set<String> literals) {

    public DynamicTokenDefinition(TokenType type) {
        this(type, Set.of());
    }

    public static DynamicTokenDefinition dynamicToken(TokenType type, Set<String> literals) {
        return new DynamicTokenDefinition(type, literals);
    }

    public static DynamicTokenDefinition from(FixedTokenDefinition def) {
        return dynamicToken(def.getType(), Set.of(def.getLiteralString()));
    }

    public static DynamicTokenDefinition[] from(FixedTokenDefinition... defs) {
        return Arrays.stream(defs).map(DynamicTokenDefinition::from).toArray(DynamicTokenDefinition[]::new);
    }
}
