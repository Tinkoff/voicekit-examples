package VoiceKit.Utils;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

// for windows compatibility
public class Printer {
    private static PrintStream ps;

    public static PrintStream getPrinter() {
        if (ps == null) {
            ps = new PrintStream(System.out, true, StandardCharsets.UTF_8);
        }
        return ps;
    }
}
