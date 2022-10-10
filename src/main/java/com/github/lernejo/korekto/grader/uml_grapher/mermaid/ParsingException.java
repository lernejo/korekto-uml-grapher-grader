package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

public class ParsingException extends RuntimeException {
    public ParsingException(Position position, String message) {
        super(message + " at " + position);
    }
}
