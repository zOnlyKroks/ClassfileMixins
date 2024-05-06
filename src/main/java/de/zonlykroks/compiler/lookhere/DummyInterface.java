package de.zonlykroks.compiler.lookhere;

public interface DummyInterface {

    default void dontDoShit() {
        System.out.println("I'm not doing anything");
    }

}
