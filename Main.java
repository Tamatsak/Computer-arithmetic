import java.math.BigInteger;

public class Main {
    public static void main(String[] args) {

        if (!args[1].equals("0") || !args[2].startsWith("0x") || (args.length != 3 && args.length != 5)) {
            System.err.println("input error");
            System.exit(1);
        } else if (args[0].equals("f")) {
            BigInteger num1 = new BigInteger(args[2].substring(2), 16);
            FloatingPoint p = new FloatingPoint(num1, true);
            if (args.length == 3) {
                System.out.println(p);
            } else {
                BigInteger num2 = new BigInteger(args[4].substring(2), 16);
                FloatingPoint t = new FloatingPoint(num2, true);
                switch (args[3]) {
                    case "-" -> System.out.println(p.sub(t));
                    case "+" -> System.out.println(t.add(p));
                    case "*" -> System.out.println(p.mul(t));
                    case "/" -> System.out.println(p.div(t));
                    default -> {
                        System.err.println("unexpected operation");
                        System.exit(1);
                    }

                }
            }
        } else if (args[0].equals("h")) {
            BigInteger num1 = new BigInteger(args[2].substring(2), 16);
            FloatingPoint p = new FloatingPoint(num1, false);
            if (args.length == 3) {
                System.out.println(p);
            } else {
                BigInteger num2 = new BigInteger(args[4].substring(2), 16);
                FloatingPoint t = new FloatingPoint(num2, false);
                switch (args[3]) {
                    case "-" -> System.out.println(p.sub(t));
                    case "+" -> System.out.println(p.add(t));
                    case "*" -> System.out.println(p.mul(t));
                    case "/" -> System.out.println(p.div(t));
                    default -> {
                        System.err.println("unexpected operation");
                        System.exit(1);
                    }
                }
            }
        } else {
            System.err.println("input error");
            System.exit(1);
        }
    }
}
