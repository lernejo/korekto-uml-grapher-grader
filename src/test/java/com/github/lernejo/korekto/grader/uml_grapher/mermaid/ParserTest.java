package com.github.lernejo.korekto.grader.uml_grapher.mermaid;

import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDef;
import com.github.lernejo.korekto.grader.uml_grapher.mermaid.model.ClassDiagram;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ParserTest {

    @Test
    void no_direction() {
        var content = """
            classDiagram
                class Bird {
                    <<interface>>
                }
                class Bug {
                    <<interface>>
                }
                class Machin {
                    +supplySomeStr(int offset) String
                }
                class Flying {
                    <<interface>>
                }
                Flying <|-- Bird : extends
                Flying <|-- Bug : extends
            """;
        AstParser parser = new AstParser(new Lexer(new CharStream(content)));

        ClassDiagram diagram = new ModelTranslator().translate(parser.parseClassDiagram().get());

        Set<ClassDef> interfaces = diagram.classesWithAnnotation("interface");

        assertThat(diagram.classes())
            .extracting(ClassDef::name)
            .containsExactlyInAnyOrder("Bird", "Bug", "Flying", "Machin");

        assertThat(interfaces)
            .extracting(ClassDef::name)
            .containsExactlyInAnyOrder("Bird", "Bug", "Flying");
    }

    @Test
    void with_class_block_items() {
        var content = """
            classDiagram
            direction LR
            class Singleton {
                -Singleton instance$
                +getInstance()$ Singleton
                +supplySomeStr(int offset) String
            }
            Singleton <-- Singleton : returns
            """;
        AstParser parser = new AstParser(new Lexer(new CharStream(content)));

        ClassDiagram diagram = new ModelTranslator().translate(parser.parseClassDiagram().get());

        assertThat(diagram).isNotNull();
    }
}
