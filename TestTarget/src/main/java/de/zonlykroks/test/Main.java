package de.zonlykroks.test;

import java.io.PrintStream;
import java.util.Random;

public class Main {

    public Main() {
        System.out.println("Not so merged constructor call!");
    }

    static {
        System.out.println("Static block that was already there");
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Main main = new Main();

        main.mcSwitch();

        main.injectIntoMe(420);
        main.wrapMe();
    }

    public void mcSwitch() {
        Random random = new Random();
        switch (random.nextInt()) {
            case 0:
                System.out.println("0");
                break;
            case 1:
                System.out.println("1");
                break;
            default:
                System.out.println("default");
        }

        System.out.println("Switch ende");
    }

    public void injectIntoMe(int x) {
        final PrintStream printStream = System.out;
        printStream.println("A");
        printStream.println("B");
        printStream.println("C");
        printStream.println(modifyMyReturnValue());

        print(x);
    }

    public void wrapMe() {
        System.out.println("I am wrapped!");
    }

    private void print(int x) {
        System.out.println(x);
    }

    public int modifyMyReturnValue() {
        return 10;
    }
}
