package org.slf4j.helpers;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.http.conn.ssl.TokenParser;
import org.slf4j.Marker;

/* loaded from: classes.dex */
public class BasicMarker implements Marker {
    private static final long serialVersionUID = 1803952589649545191L;
    private final String name;
    private List<Marker> referenceList;
    private static String OPEN = "[ ";
    private static String CLOSE = " ]";
    private static String SEP = ", ";

    /* JADX INFO: Access modifiers changed from: package-private */
    public BasicMarker(String name) {
        if (name == null) {
            throw new IllegalArgumentException("A marker name cannot be null");
        }
        this.name = name;
    }

    @Override // org.slf4j.Marker
    public String getName() {
        return this.name;
    }

    @Override // org.slf4j.Marker
    public synchronized void add(Marker reference) {
        if (reference == null) {
            throw new IllegalArgumentException("A null value cannot be added to a Marker as reference.");
        }
        if (!contains(reference) && !reference.contains(this)) {
            if (this.referenceList == null) {
                this.referenceList = new Vector();
            }
            this.referenceList.add(reference);
        }
    }

    @Override // org.slf4j.Marker
    public synchronized boolean hasReferences() {
        boolean z;
        if (this.referenceList != null) {
            z = this.referenceList.size() > 0;
        }
        return z;
    }

    @Override // org.slf4j.Marker
    public boolean hasChildren() {
        return hasReferences();
    }

    @Override // org.slf4j.Marker
    public synchronized Iterator<Marker> iterator() {
        Iterator<Marker> it2;
        if (this.referenceList != null) {
            it2 = this.referenceList.iterator();
        } else {
            List<Marker> emptyList = Collections.emptyList();
            it2 = emptyList.iterator();
        }
        return it2;
    }

    /* JADX WARN: Code restructure failed: missing block: B:12:0x001f, code lost:
        r5.referenceList.remove(r0);
     */
    /* JADX WARN: Code restructure failed: missing block: B:13:0x0024, code lost:
        r3 = true;
     */
    @Override // org.slf4j.Marker
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public synchronized boolean remove(org.slf4j.Marker r6) {
        /*
            r5 = this;
            r3 = 0
            monitor-enter(r5)
            java.util.List<org.slf4j.Marker> r4 = r5.referenceList     // Catch: java.lang.Throwable -> L29
            if (r4 != 0) goto L8
        L6:
            monitor-exit(r5)
            return r3
        L8:
            java.util.List<org.slf4j.Marker> r4 = r5.referenceList     // Catch: java.lang.Throwable -> L29
            int r2 = r4.size()     // Catch: java.lang.Throwable -> L29
            r0 = 0
        Lf:
            if (r0 >= r2) goto L6
            java.util.List<org.slf4j.Marker> r4 = r5.referenceList     // Catch: java.lang.Throwable -> L29
            java.lang.Object r1 = r4.get(r0)     // Catch: java.lang.Throwable -> L29
            org.slf4j.Marker r1 = (org.slf4j.Marker) r1     // Catch: java.lang.Throwable -> L29
            boolean r4 = r6.equals(r1)     // Catch: java.lang.Throwable -> L29
            if (r4 == 0) goto L26
            java.util.List<org.slf4j.Marker> r3 = r5.referenceList     // Catch: java.lang.Throwable -> L29
            r3.remove(r0)     // Catch: java.lang.Throwable -> L29
            r3 = 1
            goto L6
        L26:
            int r0 = r0 + 1
            goto Lf
        L29:
            r3 = move-exception
            monitor-exit(r5)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: org.slf4j.helpers.BasicMarker.remove(org.slf4j.Marker):boolean");
    }

    @Override // org.slf4j.Marker
    public boolean contains(Marker other) {
        if (other == null) {
            throw new IllegalArgumentException("Other cannot be null");
        }
        if (equals(other)) {
            return true;
        }
        if (hasReferences()) {
            for (Marker ref : this.referenceList) {
                if (ref.contains(other)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // org.slf4j.Marker
    public boolean contains(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Other cannot be null");
        }
        if (this.name.equals(name)) {
            return true;
        }
        if (hasReferences()) {
            for (Marker ref : this.referenceList) {
                if (ref.contains(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override // org.slf4j.Marker
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof Marker)) {
            return false;
        }
        Marker other = (Marker) obj;
        return this.name.equals(other.getName());
    }

    @Override // org.slf4j.Marker
    public int hashCode() {
        return this.name.hashCode();
    }

    public String toString() {
        if (!hasReferences()) {
            return getName();
        }
        Iterator<Marker> it2 = iterator();
        StringBuilder sb = new StringBuilder(getName());
        sb.append(TokenParser.SP).append(OPEN);
        while (it2.hasNext()) {
            Marker reference = it2.next();
            sb.append(reference.getName());
            if (it2.hasNext()) {
                sb.append(SEP);
            }
        }
        sb.append(CLOSE);
        return sb.toString();
    }
}