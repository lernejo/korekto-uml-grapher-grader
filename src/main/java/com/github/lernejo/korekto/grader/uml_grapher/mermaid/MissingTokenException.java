package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

import java.util.Arrays;
import java.util.stream.Collectors;

public class MissingTokenException extends ParsingException {

    public MissingTokenException(FixedTokenDefinition expected, Token found) {
        super(found.position(), "Expecting " + wording(expected) + ", but found " + found);
    }

    public MissingTokenException(Token found, DynamicTokenDefinition... expectations) {
        super(found.position(), "Expecting " + (expectations.length > 0 ? Arrays
            .stream(expectations)
            .map(MissingTokenException::wording)
            .collect(Collectors.joining(" or "))
            : "")
            + ", but found " + found);
    }

    public MissingTokenException(Token found, FixedTokenDefinition... expectations) {
        super(found.position(), "Expecting " + (expectations.length > 0 ? Arrays
            .stream(expectations)
            .map(MissingTokenException::wording)
            .collect(Collectors.joining(" or "))
            : "")
            + ", but found " + found);
    }

    private static String wording(FixedTokenDefinition token) {
        return token.getType().name() + " token (" + characterWording(token) + " '" + token.getLiteralString().replace("\n", "\\n") + "')";
    }

    private static String wording(DynamicTokenDefinition dynamicTokenDefinition) {
        return dynamicTokenDefinition.type().name() + " token (any of: " + dynamicTokenDefinition.literals() + ")";
    }

    private static String characterWording(FixedTokenDefinition fixedTokenDefinition) {
        final String wording;
        if (fixedTokenDefinition.getLiteralString().length() > 1) {
            wording = "character sequence";
        } else {
            wording = "character";
        }
        return wording;
    }
}
