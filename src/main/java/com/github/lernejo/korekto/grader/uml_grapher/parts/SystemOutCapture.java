package com.github.lernejo.korekto.grader.uml_grapher.parts;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public record SystemOutCapture(PrintStream originalPrintStream,
                               ByteArrayOutputStream capturer) implements AutoCloseable {

    public static SystemOutCapture start() {
        PrintStream originalPrintStream = System.out;
        ByteArrayOutputStream capturer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturer));
        return new SystemOutCapture(originalPrintStream, capturer);
    }

    public String getRecording() {
        return new String(capturer.toByteArray());
    }

    @Override
    public void close() {
        System.setOut(new PrintStream(originalPrintStream));
    }
}
