import infrastructure.cli.Interface;

public class Program {
    static Interface cli = new Interface();

    public static void main(String[] args) {
        cli.start(args);
    }
}
