package de.zonlykroks.compiler.transformer;

import de.zonlykroks.compiler.Bootstrap;
import de.zonlykroks.compiler.annotations.*;
import de.zonlykroks.compiler.scanner.util.MixinAnnotatedClass;
import de.zonlykroks.compiler.transformer.processor.*;
import de.zonlykroks.compiler.transformer.util.TransformerUtils;
import org.glavo.classfile.*;
import org.glavo.classfile.constantpool.ClassEntry;
import org.glavo.classfile.instruction.InvokeInstruction;
import org.glavo.classfile.instruction.ReturnInstruction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static de.zonlykroks.compiler.delegator.DelegatingClassLoader.getClassBytes;

public class ClassFileTransformerImpl {

    private final ClassFile classFile = ClassFile.of();

    private byte[] transform(ClassModel targetModel, ClassTransform transform) {
        return classFile.transform(targetModel,transform);
    }

    private ClassModel parse(byte[] bytes) {
        return classFile.parse(bytes);
    }

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

            targetModel = transformInternal(clazz.mixinClazz, targetModel, parse(getClassBytes(url)));
        }

        return transform(targetModel, ClassTransform.ofStateful( () -> ClassFileBuilder::accept));
    }

    private ClassModel transformInternal(Class<?> transformerClassModel, ClassModel targetModel, ClassModel transformerModel) {
        targetModel = mergeConstructor(targetModel, transformerModel);
        targetModel = mergeStaticConstructor(targetModel, transformerModel);
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
                        //Nothing bad ever happens to the kennedies
                    }
                }
            }
        }

        return targetModel;
    }

    private ClassModel transformMergeMethods(ClassModel targetModel, ClassModel transformerModel) {
        byte[] modified = transform(targetModel, ClassTransform.ofStateful( () -> new ClassTransform() {
            @Override
            public void accept(ClassBuilder classBuilder, ClassElement classElement) {
                classBuilder.accept(classElement);
            }

            @Override
            public void atEnd(ClassBuilder builder) {
                for(MethodModel mergeModel : transformerModel.methods()) {
                    if(mergeModel.methodName().stringValue().replace(" ", "").equalsIgnoreCase("<init>") || mergeModel.methodName().stringValue().replace(" ", "").equalsIgnoreCase("<clinit>")) {
                        System.out.println(mergeModel.methodName().stringValue() + " is a constructor or static constructor, skipping the merge as method! Returning at later stage!");
                        continue;
                    }

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

        return parse(modified);
    }

    private ClassModel mergeConstructor(ClassModel targetConstructorModel, ClassModel transformerModel) {
        final MethodModel constructorModel = transformerModel.methods().stream().filter(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase("<init>")).findFirst().orElse(null);

        if(constructorModel == null) {
            //HOLY SHIT WE GOTTA GO BALD!!!!
            return targetConstructorModel;
        }

        return parse( transform(targetConstructorModel, TransformerUtils.getTransformingMethodBodies("<init>", (codeBuilder, codeElement) -> {
            if(codeElement instanceof InvokeInstruction invokeInstruction) {
                if(invokeInstruction.opcode() == Opcode.INVOKESPECIAL) {
                    return;
                }
            }

            handleMergeConstructor(codeBuilder, codeElement, constructorModel);
        })));
    }

    private ClassModel mergeStaticConstructor(ClassModel targetConstructorModel, ClassModel transformerModel) {
        final MethodModel staticConstructorModel = transformerModel.methods().stream().filter(methodModel -> methodModel.methodName().stringValue().equalsIgnoreCase("<clinit>")).findFirst().orElse(null);

        if(staticConstructorModel == null) {
            return targetConstructorModel;
        }

        AtomicBoolean clinitAlreadyExists = new AtomicBoolean(false);

        byte[] modified = transform(targetConstructorModel, ClassTransform.ofStateful( () -> new ClassTransform() {

            @Override
            public void accept(ClassBuilder classBuilder, ClassElement classElement) {
                if(classElement instanceof MethodModel methodElement) {
                    if(methodElement.methodName().stringValue().equalsIgnoreCase("<clinit>")) {
                        clinitAlreadyExists.set(true);
                    }
                }

                classBuilder.accept(classElement);
            }

            @Override
            public void atEnd(ClassBuilder builder) {
                if(!clinitAlreadyExists.get()) {
                    builder.withMethodBody(staticConstructorModel.methodName(), staticConstructorModel.methodType(), staticConstructorModel.flags().flagsMask(), methodBuilder -> {
                        if(staticConstructorModel.code().isEmpty()) {
                            methodBuilder.nop();
                        }else {
                            for(CodeElement element : staticConstructorModel.code().get()) {
                                methodBuilder.with(element);
                            }
                        }
                    });
                    clinitAlreadyExists.set(true);
                }
            }
        }));

        return parse(transform(parse(modified), TransformerUtils.getTransformingMethodBodies("<clinit>", (codeBuilder, codeElement) -> handleMergeConstructor(codeBuilder, codeElement, staticConstructorModel))));
    }

    private void handleMergeConstructor(CodeBuilder codeBuilder, CodeElement codeElement, MethodModel constructorModel) {
        if(codeElement instanceof ReturnInstruction returnInstruction) {
            if (returnInstruction.typeKind() == TypeKind.VoidType) {
                if(constructorModel.code().isEmpty()) {
                    codeBuilder.nop();
                }else {
                    for(CodeElement element : constructorModel.code().get()) {
                        if(element instanceof ReturnInstruction) {
                            continue;
                        }

                        codeBuilder.with(element);
                    }
                }

                codeBuilder.with(codeElement);
                return;
            }
        }

        codeBuilder.with(codeElement);
    }

    private ClassModel transformMergeInterfaces(ClassModel targetModel, ClassModel transformerModel) {
        byte[] modified = transform(targetModel, ClassTransform.ofStateful( () -> new ClassTransform() {
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

        return parse(modified);
    }
}
