package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public class Lexer {

    public static final Predicate<Character> IS_EOL = c -> c == '\n';
    public static final Predicate<Character> IS_SPACE = c -> " \t".indexOf(c) >= 0;
    public static final Predicate<Character> IS_GREATER = c -> c == '>';
    public static final Predicate<Character> IS_STAR = c -> c == '*';
    public static final Predicate<Character> IS_DOLLAR = c -> c == '$';
    public static final Predicate<Character> IS_OPEN_PARENTHESIS = c -> c == '(';
    public static final Predicate<Character> IS_CLOSE_PARENTHESIS = c -> c == ')';
    public static final Predicate<Character> IS_NOT_A_SPACE = IS_SPACE.negate();

    private final LinkedList<Token> bufferedTokens = new LinkedList<>();
    private final CharStream charStream;

    public Lexer(CharStream charStream) {
        this.charStream = charStream;
    }

    public Token next() {
        Position position = charStream.getPosition();
        if (!bufferedTokens.isEmpty()) {
            return bufferedTokens.poll();
        }
        String spaces = charStream.nextUntil(IS_NOT_A_SPACE);


        final Token nextToken;
        if (charStream.isEndReached()) {
            nextToken = new Token(TokenType.EOF, position);
        } else if (spaces.length() > 0) {
            nextToken = new Token(TokenType.SPACE, spaces, position);
        } else if (charStream.peek() == FixedTokenDefinition.EOL.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.EOL, position);

        } else if (charStream.anyStringAhead(FixedTokenDefinition.L_INHERITANCE.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.L_INHERITANCE.getLiteralString());
            nextToken = new Token(TokenType.L_INHERITANCE, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.R_INHERITANCE.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.R_INHERITANCE.getLiteralString());
            nextToken = new Token(TokenType.R_INHERITANCE, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.L_COMPOSITION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.L_COMPOSITION.getLiteralString());
            nextToken = new Token(TokenType.L_COMPOSITION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.R_COMPOSITION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.R_COMPOSITION.getLiteralString());
            nextToken = new Token(TokenType.R_COMPOSITION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.L_AGGREGATION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.L_AGGREGATION.getLiteralString());
            nextToken = new Token(TokenType.L_AGGREGATION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.R_AGGREGATION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.R_AGGREGATION.getLiteralString());
            nextToken = new Token(TokenType.R_AGGREGATION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.L_ASSOCIATION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.L_ASSOCIATION.getLiteralString());
            nextToken = new Token(TokenType.L_ASSOCIATION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.R_ASSOCIATION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.R_ASSOCIATION.getLiteralString());
            nextToken = new Token(TokenType.R_ASSOCIATION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.L_DEPENDENCY.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.L_DEPENDENCY.getLiteralString());
            nextToken = new Token(TokenType.L_DEPENDENCY, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.R_DEPENDENCY.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.R_DEPENDENCY.getLiteralString());
            nextToken = new Token(TokenType.R_DEPENDENCY, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.L_REALIZATION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.L_REALIZATION.getLiteralString());
            nextToken = new Token(TokenType.L_REALIZATION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.R_REALIZATION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.R_REALIZATION.getLiteralString());
            nextToken = new Token(TokenType.R_REALIZATION, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.SOLID_LINK.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.SOLID_LINK.getLiteralString());
            nextToken = new Token(TokenType.SOLID_LINK, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.DASHED_LINK.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.DASHED_LINK.getLiteralString());
            nextToken = new Token(TokenType.DASHED_LINK, literal, position);

        } else if (charStream.peek() == FixedTokenDefinition.COLON.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.COLON, position);
        } else if (charStream.peek() == FixedTokenDefinition.COMMA.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.COMMA, position);
        } else if (charStream.peek() == FixedTokenDefinition.PLUS.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.PLUS, position);
        } else if (charStream.peek() == FixedTokenDefinition.MINUS.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.MINUS, position);
        } else if (charStream.peek() == FixedTokenDefinition.SHARP.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.SHARP, position);
        } else if (charStream.peek() == FixedTokenDefinition.TILDE.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.TILDE, position);
        } else if (charStream.peek() == FixedTokenDefinition.STAR.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.STAR, position);
        } else if (charStream.peek() == FixedTokenDefinition.DOLLAR.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.DOLLAR, position);
        } else if (charStream.peek() == FixedTokenDefinition.OPEN_CURLY_BRACKET.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.OPEN_CURLY_BRACKET, position);
        } else if (charStream.peek() == FixedTokenDefinition.CLOSE_CURLY_BRACKET.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.CLOSE_CURLY_BRACKET, position);
        } else if (charStream.peek() == FixedTokenDefinition.OPEN_PARENTHESIS.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.OPEN_PARENTHESIS, position);
        } else if (charStream.peek() == FixedTokenDefinition.CLOSE_PARENTHESIS.getSingleCharacter()) {
            charStream.next();
            nextToken = new Token(FixedTokenDefinition.CLOSE_PARENTHESIS, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.COMMENT_DELIMITER.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.COMMENT_DELIMITER.getLiteralString());
            nextToken = new Token(TokenType.COMMENT_DELIMITER, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.ANNOTATION_START.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.ANNOTATION_START.getLiteralString());
            nextToken = new Token(TokenType.ANNOTATION_START, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.ANNOTATION_END.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.ANNOTATION_END.getLiteralString());
            nextToken = new Token(TokenType.ANNOTATION_END, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.CLASS_DIAGRAM.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.CLASS_DIAGRAM.getLiteralString());
            nextToken = new Token(TokenType.CLASS_DIAGRAM, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.CLASS.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.CLASS.getLiteralString());
            nextToken = new Token(TokenType.CLASS, literal, position);
        } else if (charStream.anyStringAhead(FixedTokenDefinition.DIRECTION.getLiteralString())) {
            String literal = charStream.nextMatchingString(FixedTokenDefinition.DIRECTION.getLiteralString());
            nextToken = new Token(TokenType.DIRECTION, literal, position);
        } else {
            String literal = charStream.nextUntil(IS_EOL.or(IS_SPACE).or(IS_GREATER).or(IS_STAR).or(IS_DOLLAR).or(IS_OPEN_PARENTHESIS).or(IS_CLOSE_PARENTHESIS));
            nextToken = new Token(TokenType.TEXT, literal, position);
        }

        return nextToken;
    }

    public Token peek() {
        if (!bufferedTokens.isEmpty()) {
            return bufferedTokens.peek();
        } else {
            Token next = next();
            bufferedTokens.offer(next);
            return next;
        }
    }

    public Token peekNextUntil(Predicate<Token> condition) {
        List<Token> alreadyBufferedTokensAccumulator = new ArrayList<>();
        while (bufferedTokens.peek() != null && !condition.test(bufferedTokens.peek())) {
            alreadyBufferedTokensAccumulator.add(bufferedTokens.poll());
        }
        Token token;
        if (bufferedTokens.peek() != null) {
            token = bufferedTokens.peek();
            Collections.reverse(alreadyBufferedTokensAccumulator);
            alreadyBufferedTokensAccumulator.forEach(bufferedTokens::offerFirst);
            return token;
        }
        while (!condition.test(token = next()) && !token.isOfType(TokenType.EOF)) {
            alreadyBufferedTokensAccumulator.add(token);
        }
        alreadyBufferedTokensAccumulator.add(token);
        alreadyBufferedTokensAccumulator.forEach(bufferedTokens::offer);
        return token;
    }

    public Token peekNextNonBlankToken() {
        return peekNextUntil(t -> !t.isOfAnyType(TokenType.SPACE, TokenType.EOL));
    }

    public void skipBlanksAndEOL() {
        skipTokensOfType(TokenType.SPACE, TokenType.EOL);
    }

    public void skipBlanks() {
        skipTokensOfType(TokenType.SPACE);
    }

    public void skipTokensOfType(TokenType... tokenTypes) {
        Token token;
        while ((token = next()).isOfAnyType(tokenTypes)) {
            // forget these tokens
        }
        bufferedTokens.offer(token);
    }

    public TokenSequence consumeUntil(TokenType... tokenTypes) {
        List<Token> tokenList = new ArrayList<>();
        while (!peek().isOfAnyType(tokenTypes)) {
            tokenList.add(next());
        }
        return new TokenSequence(charStream.getPosition(), tokenList);
    }

    public TokenSequence consumeUntilNextLine() {
        TokenSequence tokenSequence = consumeUntil(TokenType.EOL, TokenType.EOF);
        next(); // Consume EOL if any
        return tokenSequence;
    }
}
