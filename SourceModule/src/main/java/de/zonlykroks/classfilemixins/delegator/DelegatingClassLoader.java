package de.zonlykroks.classfilemixins.delegator;

import de.zonlykroks.classfilemixins.Bootstrap;
import de.zonlykroks.classfilemixins.Constants;
import de.zonlykroks.classfilemixins.transformer.ClassFileTransformerImpl;
import org.glavo.classfile.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;

public class DelegatingClassLoader extends ClassLoader{

    private final ClassFileTransformerImpl classFileTransformer = new ClassFileTransformerImpl();
    private final ClassFile classFile = ClassFile.of();

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        String[] split = name.split("\\.");
        String targetClassName = split[split.length - 1];

        if(checkIfNoTarget(targetClassName)) {
            return super.loadClass(name, resolve);
        }

        if(checkIfMixinTargetClass(targetClassName)) {
            return defineMixinRelatedClass(name);
        }

        try {
            return super.loadClass(name, resolve);
        }catch (ClassNotFoundException e) {
            return defineMixinRelatedClass(name);
        }
    }

    private Class<?> defineMixinRelatedClass(String name) {
        URL url = Bootstrap.mixinClassLoader.findResource(name.replace(".", "/") + ".class");

        ClassModel model = classFile.parse(getClassBytes(url));

        byte[] modified = classFileTransformer.transform(name,model);

        preDefineDumpClass(name, modified);

        return defineClass(name, modified, 0, modified.length);
    }

    private boolean checkIfNoTarget(String name) {
        return name.startsWith("java") || name.startsWith("sun") || name.startsWith("jdk") || name.startsWith("com.sun") || name.startsWith("org") || name.startsWith(Constants.PACKAGE_NAME);
    }

    private boolean checkIfMixinTargetClass(String splitName) {
        return Bootstrap.mixinAnnotatedClasses.stream().anyMatch(mixinAnnotatedClass -> mixinAnnotatedClass.getTargetClassDescriptor().equals(splitName));
    }

    private void preDefineDumpClass(String splitName, byte[] bytes){
       try {
           File dumpFolder = new File(Constants.DUMP_PATH);

           if(!dumpFolder.exists()) {
               dumpFolder.mkdirs();
           }

           File file = new File(Constants.DUMP_PATH + splitName + ".class");

           if(!file.exists()) file.createNewFile();

           Files.write(file.toPath(), bytes);
       }catch (Exception e) {
           e.printStackTrace();
       }
    }

    public static byte[] getClassBytes(URL url){
        try (InputStream inputStream = url.openStream();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            return outputStream.toByteArray();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
