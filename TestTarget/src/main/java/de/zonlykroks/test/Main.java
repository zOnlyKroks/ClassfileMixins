package de.zonlykroks.test;

import java.io.PrintStream;
public class Main {

    private final String SHADOW = "I AM A SHADOWED PRIVATE FIELD!";
    private final TestEnum testEnum;

    public Main() {
        testEnum = TestEnum.TEST_ENUM;
        System.out.println("Not so merged constructor call!");
    }

    static {
        System.out.println("Static block that was already there");
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Main main = new Main();

        main.injectIntoMe(420);
        main.wrapMe();
    }

    public void injectIntoMe(int x) {
        final PrintStream printStream = System.out;
        printStream.println("A");
        printStream.println("B");
        printStream.println("C");
        printStream.println(modifyMyReturnValue());

        print(x);
        terminateJVM();
    }

    public void terminateJVM() {
        System.out.println("Surely nothing will happen after this method call ;D");
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
