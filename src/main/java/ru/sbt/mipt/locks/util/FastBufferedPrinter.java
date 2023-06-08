package ru.sbt.mipt.locks.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FastBufferedPrinter {
    private final BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

    public void print(String s) {
        try {
            out.write(s + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void flush() {
        try {
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
