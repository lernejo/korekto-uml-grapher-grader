package com.github.lernejo.korekto.grader.uml_grapher.mermaid;


import com.github.lernejo.korekto.grader.uml_grapher.mermaid.ast.*;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.github.lernejo.korekto.grader.uml_grapher.mermaid.DynamicTokenDefinition.from;

public record AstParser(Lexer lexer) {

    public Optional<ClassDiagram> parseClassDiagram() {
        if (lexer.peek().isOfType(TokenType.CLASS_DIAGRAM)) {
            lexer.next(); // consume keyword
            lexer.skipBlanksAndEOL();
            var builder = ClassDiagramBuilder.builder();
            builder.direction(parseDirection());
            builder.items(parseItems());
            return Optional.of(builder.build());
        } else {
            return Optional.empty();
        }
    }

    private Set<Item> parseItems() {
        Set<Item> items = new HashSet<>();
        while (!lexer.peekNextNonBlankToken().isOfType(TokenType.EOF)) {
            items.add(parseItem());
        }
        return items;
    }

    private Item parseItem() {
        lexer.skipBlanksAndEOL();
        Token token = lexer.next();
        lexer.skipBlanks();
        if (token.isOfType(TokenType.CLASS)) {
            String name = lexer.next().literal();
            lexer.skipBlanks();
            if (lexer.peek().isOfType(TokenType.EOL)) {
                return new ClassLine(name);
            } else if (lexer.peek().isOfType(TokenType.OPEN_CURLY_BRACKET)) {
                return parseClassBlock(name);
            } else {
                throw new MissingTokenException(lexer.peek(), FixedTokenDefinition.EOL, FixedTokenDefinition.OPEN_CURLY_BRACKET);
            }
        } else if (lexer.peekNextNonBlankToken().isOfAnyType(TokenType.ARROW_TYPES)) {
            lexer.skipBlanks();
            Token arrow = lexer.next();
            lexer.skipBlanks();
            Token secondClassName = lexer.next();
            return new Relation(token.literal(), DirectionalRelationship.from(arrow.type()), secondClassName.literal());
        } else {
            // TODO handle classLineField, Methods
        }
        return null;
    }

    private ClassBlock parseClassBlock(String className) {
        var builder = ClassBlockBuilder.builder().name(className);
        var openCurly = lexer.next();
        if (!openCurly.isOfType(TokenType.OPEN_CURLY_BRACKET)) {
            throw new MissingTokenException(FixedTokenDefinition.OPEN_CURLY_BRACKET, openCurly);
        }
        lexer.skipBlanksAndEOL();
        while (!lexer.peekNextNonBlankToken().isOfType(TokenType.CLOSE_CURLY_BRACKET)) {
            if (lexer.peek().isOfType(TokenType.ANNOTATION_START)) {
                builder.annotation(parseAnnotation());
            } else if (lexer.peek().isOfAnyType(TokenType.VISIBILITIES)) {
                builder.addItems(parseClassBlockItem());
            } else {
                throw new MissingTokenException(lexer.peek(), from(
                    FixedTokenDefinition.ANNOTATION_START,
                    FixedTokenDefinition.PLUS,
                    FixedTokenDefinition.MINUS,
                    FixedTokenDefinition.SHARP,
                    FixedTokenDefinition.TILDE));
            }
            lexer.skipBlanksAndEOL();
        }
        return builder.build();
    }

    private ClassBlockItem parseClassBlockItem() {
        Token visibility = lexer.next();
        if (visibility.isOfAnyType(TokenType.VISIBILITIES)) {
            lexer.skipBlanks();
            Token nameOrType = lexer.next();
            lexer.skipBlanks();
            Token textOrParenthesis = lexer.peek();
            if (textOrParenthesis.isOfType(TokenType.OPEN_PARENTHESIS)) {
                return parseClassBlockMethod(visibility, nameOrType);
            } else if (textOrParenthesis.isOfType(TokenType.TEXT)) {
                return parseClassBlockField(visibility, nameOrType);
            } else {
                throw new MissingTokenException(textOrParenthesis,
                    from(FixedTokenDefinition.OPEN_PARENTHESIS),
                    new DynamicTokenDefinition(TokenType.TEXT));
            }
        } else {
            throw new MissingTokenException(visibility, from(
                FixedTokenDefinition.PLUS,
                FixedTokenDefinition.MINUS,
                FixedTokenDefinition.SHARP,
                FixedTokenDefinition.TILDE));
        }
    }

    private ClassBlockField parseClassBlockField(Token visibility, Token type) {
        var builder = ClassBlockFieldBuilder.builder();
        builder.visibility(Visibility.fromMermaid(visibility.literal()));
        builder.type(type.literal());
        Token name = lexer.next();
        if (!name.isOfType(TokenType.TEXT)) {
            throw new MissingTokenException(name, new DynamicTokenDefinition(TokenType.TEXT));
        }
        builder.name(name.literal());
        lexer.skipBlanks();
        if (lexer.peek().isOfAnyType(TokenType.CLASSIFIERS)) {
            builder.classifier(Classifier.fromMermaid(lexer.next().literal()));
        }
        return builder.build();
    }

    private ClassBlockMethod parseClassBlockMethod(Token visibility, Token name) {
        Token openParenthesis = lexer.next();
        if (!openParenthesis.isOfType(TokenType.OPEN_PARENTHESIS)) {
            throw new MissingTokenException(FixedTokenDefinition.OPEN_PARENTHESIS, openParenthesis);
        }
        var builder = ClassBlockMethodBuilder.builder()
            .visibility(Visibility.fromMermaid(visibility.literal()))
            .name(name.literal());
        lexer.skipBlanks();
        while (!lexer.peekNextNonBlankToken().isOfType(TokenType.CLOSE_PARENTHESIS)) {
            Token parameterType = lexer.next();
            lexer.skipBlanks();
            Token parameterName = lexer.next();
            lexer.skipBlanks();
            Token closeParenthesisOrComma = lexer.peek();
            if (closeParenthesisOrComma.isOfType(TokenType.COMMA)) {
                lexer.next(); // consume comma
            } else if (!closeParenthesisOrComma.isOfType(TokenType.CLOSE_PARENTHESIS)) {
                throw new MissingTokenException(FixedTokenDefinition.CLOSE_PARENTHESIS, closeParenthesisOrComma);
            }
            lexer.skipBlanks();
            builder.addParameters(new Parameter(parameterType.literal(), parameterName.literal()));
        }
        lexer.next(); // consume close_parenthesis
        lexer.skipBlanks();
        if (lexer.peek().isOfAnyType(TokenType.CLASSIFIERS)) {
            builder.classifier(Classifier.fromMermaid(lexer.next().literal()));
            lexer.skipBlanks();
        }
        builder.type(lexer.next().literal());

        return builder.build();
    }

    private String parseAnnotation() {
        Token annotationStart = lexer.next();
        if (!annotationStart.isOfType(TokenType.ANNOTATION_START)) {
            throw new MissingTokenException(FixedTokenDefinition.ANNOTATION_START, annotationStart);
        }
        lexer.skipBlanks();
        String annotation = lexer.next().literal();
        lexer.skipBlanks();
        Token annotationEnd = lexer.next();
        if (!annotationEnd.isOfType(TokenType.ANNOTATION_END)) {
            throw new MissingTokenException(FixedTokenDefinition.ANNOTATION_END, annotationEnd);
        }
        return annotation;
    }

    private Direction parseDirection() {
        if (lexer.peek().isOfType(TokenType.DIRECTION)) {
            lexer.next();
            TokenSequence tokenSequence = lexer.consumeUntilNextLine();
            String direction = tokenSequence.toLiteral().trim();
            return Direction.safeValueOf(direction);
        }
        return null;
    }
}
