package org.apache.mina.util.byteaccess;

import java.nio.ByteOrder;
import org.apache.mina.util.byteaccess.ByteArray;
import org.apache.mina.util.byteaccess.CompositeByteArray;

/* loaded from: classes.dex */
abstract class CompositeByteArrayRelativeBase {
    protected final CompositeByteArray cba;
    protected final ByteArray.Cursor cursor;

    protected abstract void cursorPassedFirstComponent();

    public CompositeByteArrayRelativeBase(CompositeByteArray cba) {
        this.cba = cba;
        this.cursor = cba.cursor(cba.first(), new CompositeByteArray.CursorListener() { // from class: org.apache.mina.util.byteaccess.CompositeByteArrayRelativeBase.1
            static final /* synthetic */ boolean $assertionsDisabled;

            static {
                $assertionsDisabled = !CompositeByteArrayRelativeBase.class.desiredAssertionStatus();
            }

            @Override // org.apache.mina.util.byteaccess.CompositeByteArray.CursorListener
            public void enteredFirstComponent(int componentIndex, ByteArray component) {
            }

            @Override // org.apache.mina.util.byteaccess.CompositeByteArray.CursorListener
            public void enteredLastComponent(int componentIndex, ByteArray component) {
                if (!$assertionsDisabled) {
                    throw new AssertionError();
                }
            }

            @Override // org.apache.mina.util.byteaccess.CompositeByteArray.CursorListener
            public void enteredNextComponent(int componentIndex, ByteArray component) {
                CompositeByteArrayRelativeBase.this.cursorPassedFirstComponent();
            }

            @Override // org.apache.mina.util.byteaccess.CompositeByteArray.CursorListener
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
