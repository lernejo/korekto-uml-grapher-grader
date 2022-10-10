package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

public enum FixedTokenDefinition {
    EOL(TokenType.EOL, '\n'),
    COLON(TokenType.COLON, ':'),
    COMMA(TokenType.COMMA, ','),
    PLUS(TokenType.PLUS, '+'),
    MINUS(TokenType.MINUS, '-'),
    SHARP(TokenType.SHARP, '#'),
    TILDE(TokenType.TILDE, '~'),
    STAR(TokenType.STAR, '*'),
    DOLLAR(TokenType.DOLLAR, '$'),
    OPEN_CURLY_BRACKET(TokenType.OPEN_CURLY_BRACKET, '{'),
    CLOSE_CURLY_BRACKET(TokenType.CLOSE_CURLY_BRACKET, '}'),
    OPEN_PARENTHESIS(TokenType.OPEN_PARENTHESIS, '('),
    CLOSE_PARENTHESIS(TokenType.CLOSE_PARENTHESIS, ')'),
    COMMENT_DELIMITER(TokenType.COMMENT_DELIMITER, "%%"),
    ANNOTATION_START(TokenType.ANNOTATION_START, "<<"),
    ANNOTATION_END(TokenType.ANNOTATION_END, ">>"),
    CLASS_DIAGRAM(TokenType.CLASS_DIAGRAM, "classDiagram"),
    CLASS(TokenType.CLASS, "class"),
    DIRECTION(TokenType.DIRECTION, "direction "),
    L_INHERITANCE(TokenType.L_INHERITANCE, "<|--"),
    R_INHERITANCE(TokenType.R_INHERITANCE, "--|>"),
    L_COMPOSITION(TokenType.L_COMPOSITION, "*--"),
    R_COMPOSITION(TokenType.R_COMPOSITION, "--*"),
    L_AGGREGATION(TokenType.L_AGGREGATION, "o--"),
    R_AGGREGATION(TokenType.R_AGGREGATION, "--o"),
    L_ASSOCIATION(TokenType.L_ASSOCIATION, "<--"),
    R_ASSOCIATION(TokenType.R_ASSOCIATION, "-->"),
    L_DEPENDENCY(TokenType.L_DEPENDENCY, "<.."),
    R_DEPENDENCY(TokenType.R_DEPENDENCY, "..>"),
    L_REALIZATION(TokenType.L_REALIZATION, "<|.."),
    R_REALIZATION(TokenType.R_REALIZATION, "..|>"),
    SOLID_LINK(TokenType.SOLID_LINK, "--"),
    DASHED_LINK(TokenType.DASHED_LINK, ".."),
    ;


    private final TokenType type;
    private final char singleCharacter;
    private final String literalString;

    FixedTokenDefinition(TokenType type, char literalCharacter) {
        this.type = type;
        this.singleCharacter = literalCharacter;
        this.literalString = String.valueOf(literalCharacter);
    }

    FixedTokenDefinition(TokenType type, String literalString) {
        this.type = type;
        this.singleCharacter = Character.MIN_VALUE;
        this.literalString = literalString;
    }

    public TokenType getType() {
        return type;
    }

    public char getSingleCharacter() {
        return singleCharacter;
    }

    public String getLiteralString() {
        return literalString;
    }
}
