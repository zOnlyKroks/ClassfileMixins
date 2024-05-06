package de.zonlykroks.compiler.lookhere;

import de.zonlykroks.compiler.collector.annotations.Mixin;
import de.zonlykroks.compiler.collector.annotations.modifiers.InterfaceInjector;

@Mixin(target = "/src/main/java/de/zonlykroks/compiler/lookhere/TransformMe.java")
@InterfaceInjector(interfaceDescriptor = "de.zonlykroks.compiler.lookhere.DummyInterface")
public class HelloMixin {
}
