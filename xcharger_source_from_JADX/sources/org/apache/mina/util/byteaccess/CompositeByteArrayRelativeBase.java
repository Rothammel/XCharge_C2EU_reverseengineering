package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.CompositeByteArray;

abstract class CompositeByteArrayRelativeBase {
    protected final CompositeByteArray cba;
    protected final ByteArray.Cursor cursor;

    /* access modifiers changed from: protected */
    public abstract void cursorPassedFirstComponent();

    public CompositeByteArrayRelativeBase(CompositeByteArray cba2) {
        this.cba = cba2;
        this.cursor = cba2.cursor(cba2.first(), new CompositeByteArray.CursorListener() {
            static final /* synthetic */ boolean $assertionsDisabled = (!CompositeByteArrayRelativeBase.class.desiredAssertionStatus());

            public void enteredFirstComponent(int componentIndex, ByteArray component) {
            }

            public void enteredLastComponent(int componentIndex, ByteArray component) {
                if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
            }

            public void enteredNextComponent(int componentIndex, ByteArray component) {
                CompositeByteArrayRelativeBase.this.cursorPassedFirstComponent();
            }

            public void enteredPreviousComponent(int componentIndex, ByteArray component) {
                if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
            }
        });
    }

    public final int getRemaining() {
        return this.cursor.getRemaining();
    }

    public final boolean hasRemaining() {
        return this.cursor.hasRemaining();
    }

    public ByteOrder order() {
        return this.cba.order();
    }

    public final void append(ByteArray ba) {
        this.cba.addLast(ba);
    }

    public final void free() {
        this.cba.free();
    }

    public final int getIndex() {
        return this.cursor.getIndex();
    }

    public final int last() {
        return this.cba.last();
    }
}
