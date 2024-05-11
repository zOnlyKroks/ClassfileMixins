package de.zonlykroks.test;

import java.io.PrintStream;
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Main main = new Main();

        main.injectIntoMe(420);
    }

    public void injectIntoMe(int x) {
        final PrintStream printStream = System.out;
        printStream.println("A");
        printStream.println("B");
        printStream.println("C");
        printStream.println(modifyMyReturnValue());

        print(x);
    }

    private void print(int x) {
        System.out.println(x);
    }

    public int modifyMyReturnValue() {
        return 10;
    }
}
