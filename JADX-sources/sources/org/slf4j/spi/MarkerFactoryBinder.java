package org.slf4j.spi;

import org.slf4j.IMarkerFactory;

/* loaded from: classes.dex */
public interface MarkerFactoryBinder {
    IMarkerFactory getMarkerFactory();

    String getMarkerFactoryClassStr();
}
