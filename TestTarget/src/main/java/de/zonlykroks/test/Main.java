package de.zonlykroks.test;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        Main main = new Main();

        main.injectIntoMe();
        main.overwriteMe();
        main.modifyConstantMe();
        System.out.println("Original return is 1, new is: " + main.modifyReturnValueMe());
        main.wrapMe();
    }

    public void injectIntoMe() {
        System.out.println("I am the target method!");
    }

    public void overwriteMe() {
        System.out.println("Nothing bad ever happends to the kenedies!");
    }

    public void modifyConstantMe() {
        System.out.println("Original after this should be 1, is:");
        System.out.println(2);
    }

    public int modifyReturnValueMe() {
        return 1;
    }

    public void wrapMe() {
        System.out.println("Wrap me please ;D");
    }

}
