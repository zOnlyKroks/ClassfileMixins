package de.zonlykroks.compiler.lookhere;

import de.zonlykroks.compiler.collector.annotations.Mixin;
import de.zonlykroks.compiler.collector.annotations.modifiers.AnnotationAdder;
import de.zonlykroks.compiler.collector.annotations.modifiers.AnnotationDropper;
import de.zonlykroks.compiler.collector.annotations.modifiers.InterfaceInjector;

@Mixin(target = "/src/main/java/de/zonlykroks/compiler/lookhere/TransformMe.java")
@InterfaceInjector(interfaceDescriptor = "de.zonlykroks.compiler.lookhere.DummyInterface")
@AnnotationDropper
@AnnotationAdder(annotationDescriptions = {"de.zonlykroks.compiler.lookhere.DummyAddAnnotation", "java.lang.Deprecated"})
public class HelloMixin {
}
