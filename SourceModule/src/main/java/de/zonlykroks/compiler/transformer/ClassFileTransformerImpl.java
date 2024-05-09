package de.zonlykroks.compiler.transformer;

import de.zonlykroks.compiler.Bootstrap;
import de.zonlykroks.compiler.annotations.*;
import de.zonlykroks.compiler.scanner.util.MixinAnnotatedClass;
import de.zonlykroks.compiler.transformer.processor.*;
import org.glavo.classfile.*;
import org.glavo.classfile.constantpool.ClassEntry;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static de.zonlykroks.compiler.delegator.DelegatingClassLoader.getClassBytes;

public class ClassFileTransformerImpl {

    public static byte[] transform(String targetClassName, ClassModel targetModel) {
        String[] split = targetClassName.split("\\.");
        targetClassName = split[split.length - 1];

        final String temp = targetClassName;

        List<MixinAnnotatedClass> mixinAnnotatedClasses = Bootstrap.mixinAnnotatedClasses.stream().filter(mixinAnnotatedClass1 -> mixinAnnotatedClass1.mixin.classDescriptor().equalsIgnoreCase(temp)).toList();

        for(MixinAnnotatedClass clazz : mixinAnnotatedClasses) {
            URL url = Bootstrap.mixinClassLoader.findResource(clazz.mixinClazz.getName().replace(".", "/") + ".class");

            targetModel = transformInternal(clazz.mixinClazz, targetModel, ClassFile.of().parse(getClassBytes(url)));
        }

        return ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> new ClassTransform() {
            @Override
            public void accept(ClassBuilder classBuilder, ClassElement classElement) {
                classBuilder.accept(classElement);
            }
        }));
    }

    private static ClassModel transformInternal(Class<?> transformerClassModel, ClassModel targetModel, ClassModel transformerModel) {
        targetModel = transformMergeInterfaces(targetModel, transformerModel);
        targetModel = transformMergeMethods(targetModel, transformerModel);

        for(Method method : transformerClassModel.getMethods()) {
            MethodModel model = transformerModel.methods().stream().filter(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(method.getName())).findFirst().orElse(null);
            for(Annotation annotation : method.getAnnotations()) {
                if(annotation instanceof InjectAnnotation injectAnnotation) {
                    targetModel = InjectAnnotationProcessor.processInjectAnnotation(injectAnnotation, targetModel, transformerModel, model);
                }else if(annotation instanceof Overwrite overwrite) {
                    targetModel = OverwriteAnnotationProcessor.processOverwriteAnnotation(overwrite, targetModel, transformerModel, model);
                }else if(annotation instanceof ModifyConstant constant) {
                    targetModel = ModifyConstantAnnotationProcessor.processModifyConstantAnnotation(constant, targetModel, transformerModel, model);
                }else if(annotation instanceof ModifyReturnValue returnValue) {
                    targetModel = ModifyReturnValueAnnotationProcessor.processModifyReturnValueAnnotation(returnValue, targetModel, transformerModel, model);
                }
            }
        }

        return targetModel;
    }

    private static ClassModel transformMergeMethods(ClassModel targetModel, ClassModel transformerModel) {
        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> new ClassTransform() {
            @Override
            public void accept(ClassBuilder classBuilder, ClassElement classElement) {
                classBuilder.accept(classElement);
            }

            @Override
            public void atEnd(ClassBuilder builder) {
                for(MethodModel mergeModel : transformerModel.methods()) {
                    if(mergeModel.methodName().stringValue().replace(" ", "").equalsIgnoreCase("<init>")) continue;

                    builder.withMethodBody(mergeModel.methodName(), mergeModel.methodType(), mergeModel.flags().flagsMask(), codeBuilder -> {
                        if(mergeModel.code().isEmpty()) {
                            codeBuilder.nop();
                            return;
                        }

                        for(CodeElement element : mergeModel.code().get()) {
                            codeBuilder.with(element);
                        }
                    });
                }
            }
        }));

        return ClassFile.of().parse(modified);
    }

    private static ClassModel transformMergeInterfaces(ClassModel targetModel, ClassModel transformerModel) {
        byte[] modified = ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> new ClassTransform() {
            @Override
            public void accept(ClassBuilder classBuilder, ClassElement classElement) {
                classBuilder.accept(classElement);
            }

            @Override
            public void atEnd(ClassBuilder builder) {
                List<ClassEntry> mergedInterfaces = new ArrayList<>();
                mergedInterfaces.addAll(targetModel.interfaces());
                mergedInterfaces.addAll(transformerModel.interfaces());


                builder.withInterfaces(mergedInterfaces);
            }
        }));

        return ClassFile.of().parse(modified);
    }
}
