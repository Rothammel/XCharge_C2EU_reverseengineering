package org.apache.commons.lang3.builder;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

/* loaded from: classes.dex */
class MultilineRecursiveToStringStyle extends RecursiveToStringStyle {
    private static final long serialVersionUID = 1;
    private int indent = 2;
    private int spaces = 2;

    public MultilineRecursiveToStringStyle() {
        resetIndent();
    }

    private void resetIndent() {
        setArrayStart("{" + SystemUtils.LINE_SEPARATOR + ((Object) spacer(this.spaces)));
        setArraySeparator("," + SystemUtils.LINE_SEPARATOR + ((Object) spacer(this.spaces)));
        setArrayEnd(SystemUtils.LINE_SEPARATOR + ((Object) spacer(this.spaces - this.indent)) + "}");
        setContentStart("[" + SystemUtils.LINE_SEPARATOR + ((Object) spacer(this.spaces)));
        setFieldSeparator("," + SystemUtils.LINE_SEPARATOR + ((Object) spacer(this.spaces)));
        setContentEnd(SystemUtils.LINE_SEPARATOR + ((Object) spacer(this.spaces - this.indent)) + "]");
    }

    private StringBuilder spacer(int spaces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            sb.append(StringUtils.SPACE);
        }
        return sb;
    }

    @Override // org.apache.commons.lang3.builder.RecursiveToStringStyle, org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, Object value) {
        if (!ClassUtils.isPrimitiveWrapper(value.getClass()) && !String.class.equals(value.getClass()) && accept(value.getClass())) {
            this.spaces += this.indent;
            resetIndent();
            buffer.append(ReflectionToStringBuilder.toString(value, this));
            this.spaces -= this.indent;
            resetIndent();
            return;
        }
        super.appendDetail(buffer, fieldName, value);
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, Object[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void reflectionAppendArrayDetail(StringBuffer buffer, String fieldName, Object array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, long[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, int[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, short[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, byte[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, char[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, double[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, float[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }

    /* JADX INFO: Access modifiers changed from: protected */
    @Override // org.apache.commons.lang3.builder.ToStringStyle
    public void appendDetail(StringBuffer buffer, String fieldName, boolean[] array) {
        this.spaces += this.indent;
        resetIndent();
        super.appendDetail(buffer, fieldName, array);
        this.spaces -= this.indent;
        resetIndent();
    }
}