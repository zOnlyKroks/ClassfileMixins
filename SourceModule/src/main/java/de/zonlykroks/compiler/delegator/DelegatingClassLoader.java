package de.zonlykroks.compiler.delegator;

import de.zonlykroks.compiler.Bootstrap;
import de.zonlykroks.compiler.transformer.ClassFileTransformerImpl;
import org.glavo.classfile.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;

public class DelegatingClassLoader extends ClassLoader{

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if(checkIfNoTarget(name)) {
            return super.loadClass(name, resolve);
        }

        if(checkIfMixinTargetClass(name)) {
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

        ClassModel model = ClassFile.of().parse(getClassBytes(url));

        byte[] modified = ClassFileTransformerImpl.transform(name,model);

        preDefineDumpClass(name, modified);

        return defineClass(name, modified, 0, modified.length);
    }

    private boolean checkIfNoTarget(String name) {
        return name.startsWith("java") || name.startsWith("sun") || name.startsWith("jdk") || name.startsWith("com.sun") || name.startsWith("org");
    }

    private boolean checkIfMixinTargetClass(String name) {
        return Bootstrap.mixinAnnotatedClasses.stream().anyMatch(mixinAnnotatedClass -> {
            String[] split = name.split("\\.");

            return mixinAnnotatedClass.getTargetClassDescriptor().equals(split[split.length - 1]);
        });
    }

    private void preDefineDumpClass(String name, byte[] bytes){
       try {
           String[] split = name.split("\\.");

           File dumpFolder = new File("C:/Users/finnr/IdeaProjects/ClassfileMixins/dump/");

           if(!dumpFolder.exists()) {
               dumpFolder.mkdirs();
           }

           File file = new File("C:/Users/finnr/IdeaProjects/ClassfileMixins/dump/" + split[split.length - 1] + ".dump..class");

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
