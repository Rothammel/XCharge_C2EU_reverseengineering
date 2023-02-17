package com.nostra13.universalimageloader.cache.disc.naming;

/* loaded from: classes.dex */
public class HashCodeFileNameGenerator implements FileNameGenerator {
    @Override // com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator
    public String generate(String imageUri) {
        return String.valueOf(imageUri.hashCode());
    }
}