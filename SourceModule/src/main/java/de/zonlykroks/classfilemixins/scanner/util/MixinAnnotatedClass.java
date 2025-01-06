package de.zonlykroks.classfilemixins.scanner.util;

import de.zonlykroks.classfilemixins.annotations.Mixin;

public class MixinAnnotatedClass {

    public final Class<?> mixinClazz;
    public final Mixin mixin;

    public MixinAnnotatedClass(Class<?> mixinClazz) {
        this.mixinClazz = mixinClazz;
        this.mixin = mixinClazz.getAnnotation(Mixin.class);
    }

    public String getTargetClassDescriptor() {
        return mixin.classDescriptor().replace(".", "/");
    }

    @Override
    public String toString() {
        return "MixinAnnotatedClass{" +
                "mixinClazz=" + mixinClazz +
                ", mixin=" + mixin +
                '}';
    }
}
