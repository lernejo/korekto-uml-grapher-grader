package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

import java.util.Arrays;

public record Token(TokenType type, String literal, Position position) {

    public Token(TokenType type, Position position) {
        this(type, "", position);
    }

    public Token(FixedTokenDefinition fixedTokenDefinition, Position position) {
        this(fixedTokenDefinition.getType(), fixedTokenDefinition.getLiteralString(), position);
    }

    public boolean isOfType(TokenType tokenType) {
        return isOfAnyType(tokenType);
    }

    public boolean isOfAnyType(TokenType... tokenTypes) {
        return Arrays.stream(tokenTypes)
            .map(tt -> tt == type())
            .mapToInt(sameType -> sameType ? 1 : 0)
            .sum() > 0;
    }
}
