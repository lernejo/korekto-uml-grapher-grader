package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

import java.util.List;
import java.util.stream.Collectors;

public record TokenSequence(Position position, List<Token> tokens) {

    public String toLiteral() {
        return tokens.stream().map(Token::literal).collect(Collectors.joining());
    }
}
