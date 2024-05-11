package de.zonlykroks.test;

import java.io.PrintStream;
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Main main = new Main();

        main.injectIntoMe();
        System.out.println(main.testOutput());
    }

    public void injectIntoMe() {
        final PrintStream printStream = System.out;
        printStream.println("A");
        printStream.println("B");
        printStream.println("C");
        printStream.println(modifyMyReturnValue());
    }

    public int testOutput() {
        if(System.currentTimeMillis() == 0) {
            return 1;
        }

        return 2;
    }

    public int modifyMyReturnValue() {
        return 10;
    }
}
