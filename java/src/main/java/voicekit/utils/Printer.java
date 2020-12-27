package voicekit.utils;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

// for windows compatibility
public class Printer {
    private static PrintStream ps;

    private Printer() {
        throw new IllegalStateException("Utility class");
    }

    public static PrintStream getPrinter() {
        if (ps == null) {
            ps = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        }
        return ps;
    }
}
