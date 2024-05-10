package de.zonlykroks.compiler.transformer.processor;

import org.glavo.classfile.ClassModel;
import org.glavo.classfile.MethodModel;

import java.lang.annotation.Annotation;

public abstract class AbstractAnnotationProcessor<T extends Annotation> {

    public abstract ClassModel processAnnotation(T annotation, ClassModel targetModel, ClassModel sourceClassModel, MethodModel sourceMethodModule);

}
