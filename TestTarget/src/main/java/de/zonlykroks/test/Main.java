package de.zonlykroks.test;

import java.io.PrintStream;
public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Main main = new Main();

        main.injectIntoMe();
    }

    public void injectIntoMe() {
        final PrintStream printStream = System.out;
        printStream.println("I am the target method");
    }
}
