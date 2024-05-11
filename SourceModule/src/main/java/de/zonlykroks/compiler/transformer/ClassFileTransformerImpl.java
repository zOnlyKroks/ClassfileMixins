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

    private final InjectAnnotationProcessor InjectAnnotationProcessor = new InjectAnnotationProcessor();
    private final OverwriteAnnotationProcessor OverwriteAnnotationProcessor = new OverwriteAnnotationProcessor();
    private final ModifyConstantAnnotationProcessor ModifyConstantAnnotationProcessor = new ModifyConstantAnnotationProcessor();
    private final ModifyReturnValueAnnotationProcessor ModifyReturnValueAnnotationProcessor = new ModifyReturnValueAnnotationProcessor();
    private final RedirectAnnotationProcessor RedirectAnnotationProcessor = new RedirectAnnotationProcessor();
    private final ModifyGetFieldReferenceAnnotationProcessor ModifyGetFieldReferenceAnnotationProcessor = new ModifyGetFieldReferenceAnnotationProcessor();
    private final ModifyLoadAnnotationProcessor ModifyLoadAnnotationProcessor = new ModifyLoadAnnotationProcessor();
    private final WrapWithConditionAnnotationProcessor WrapWithConditionAnnotationProcessor = new WrapWithConditionAnnotationProcessor();

    public byte[] transform(String targetClassName, ClassModel targetModel) {
        String[] split = targetClassName.split("\\.");
        targetClassName = split[split.length - 1];

        final String temp = targetClassName;

        List<MixinAnnotatedClass> mixinAnnotatedClasses = Bootstrap.mixinAnnotatedClasses.stream().filter(mixinAnnotatedClass1 -> mixinAnnotatedClass1.mixin.classDescriptor().equalsIgnoreCase(temp)).toList();

        for(MixinAnnotatedClass clazz : mixinAnnotatedClasses) {
            URL url = Bootstrap.mixinClassLoader.findResource(clazz.mixinClazz.getName().replace(".", "/") + ".class");

            targetModel = transformInternal(clazz.mixinClazz, targetModel, ClassFile.of().parse(getClassBytes(url)));
        }

        return ClassFile.of().transform(targetModel, ClassTransform.ofStateful( () -> ClassFileBuilder::accept));
    }

    private ClassModel transformInternal(Class<?> transformerClassModel, ClassModel targetModel, ClassModel transformerModel) {
        targetModel = transformMergeInterfaces(targetModel, transformerModel);
        targetModel = transformMergeMethods(targetModel, transformerModel);

        for(Method method : transformerClassModel.getMethods()) {
            MethodModel model = transformerModel.methods().stream().filter(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase(method.getName())).findFirst().orElse(null);

            for(Annotation annotation : method.getAnnotations()) {
                switch (annotation) {
                    case InjectAnnotation injectAnnotation -> targetModel = InjectAnnotationProcessor.processAnnotation(injectAnnotation, targetModel, transformerModel, model);
                    case Overwrite overwrite -> targetModel = OverwriteAnnotationProcessor.processAnnotation(overwrite, targetModel, transformerModel, model);
                    case ModifyConstant constant -> targetModel = ModifyConstantAnnotationProcessor.processAnnotation(constant, targetModel, transformerModel, model);
                    case ModifyReturnValue returnValue -> targetModel = ModifyReturnValueAnnotationProcessor.processAnnotation(returnValue, targetModel, transformerModel, model);
                    case Redirect redirect -> targetModel = RedirectAnnotationProcessor.processAnnotation(redirect, targetModel, transformerModel, model);
                    case ModifyGetFieldReference getStatic -> targetModel = ModifyGetFieldReferenceAnnotationProcessor.processAnnotation(getStatic, targetModel, transformerModel, model);
                    case ModifyLoadInstruction modifyLoad -> targetModel = ModifyLoadAnnotationProcessor.processAnnotation(modifyLoad, targetModel, transformerModel, model);
                    case WrapWithCondition wrapWithCondition -> targetModel = WrapWithConditionAnnotationProcessor.processAnnotation(wrapWithCondition, targetModel, transformerModel, model);
                    default -> {
                        //Nothing bad ever happens to the kenedies
                    }
                }
            }
        }

        return targetModel;
    }

    private ClassModel transformMergeMethods(ClassModel targetModel, ClassModel transformerModel) {
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
                }}
        }));

        return ClassFile.of().parse(modified);
    }

    private ClassModel transformMergeInterfaces(ClassModel targetModel, ClassModel transformerModel) {
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
