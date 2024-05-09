package de.zonlykroks.test;

import java.util.ArrayList;
import java.util.function.Supplier;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Main main = new Main();

        main.injectIntoMe();
        main.overwriteMe();
        main.modifyConstantMe();
        System.out.println("Return value should be 1, Modified Return Value is: " + main.modifyReturnValueMe());
        main.redirectMe();
        main.lambdaTest();
        main.returnTest();
    }

    public void injectIntoMe() {
        System.out.println("I am the target method!");
    }

    public void overwriteMe() {
        System.out.println("Nothing bad ever happends to the kenedies!");
    }

    public void modifyConstantMe() {
        System.out.println("Original after this should be 2, Modified Constant is:");
        System.out.println(2);
    }

    public int modifyReturnValueMe() {
        return 1;
    }

    public void redirectMe() {
        System.out.println("I am the first println");
        System.out.println("I am the second println");
    }

    public void lambdaTest() {
        println( () -> "I am a lambda!");
    }

    private void println(Supplier<String> o) {
        System.out.print(o.get());
    }

    private int returnTest() {
        if(System.currentTimeMillis() == -1) {
            return 2;
        }

        return 1;
    }
}
