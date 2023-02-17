package com.xcharge.charger.p006ui.p009c2.activity.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import com.xcharge.charger.protocol.ocpp.bean.types.Phase;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* renamed from: com.xcharge.charger.ui.c2.activity.widget.KyLetterfilter */
public class KyLetterfilter extends View {
    private boolean allowSetCurrent = true;
    private int normalBgColor = -1;
    private OnLetterfilterListener onLetterfilterListener;
    private Paint paint = new Paint();
    private int selectedBgColor = -13388315;
    private int selfHeight;
    private int selfWidth;
    private List<InnerWrapper> wrappers;

    /* renamed from: com.xcharge.charger.ui.c2.activity.widget.KyLetterfilter$OnLetterfilterListener */
    public interface OnLetterfilterListener {
        void end();

        void letterChanged(String str);

        void start();
    }

    public KyLetterfilter(Context context) {
        super(context);
    }

    public KyLetterfilter(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public KyLetterfilter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /* access modifiers changed from: protected */
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.selfWidth = 30;
        this.selfHeight = 415;
        setMeasuredDimension(this.selfWidth, this.selfHeight);
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(this.normalBgColor);
        createWrappers();
        drawLetter(canvas);
    }

    private void createWrappers() {
        if (this.wrappers == null) {
            String[] letterArr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", UnitOfMeasure.f120K, "L", "M", Phase.f118N, "O", "P", "Q", "R", "S", "T", "U", UnitOfMeasure.f121V, "W", "X", "Y", "Z"};
            this.wrappers = new ArrayList();
            int wrapperHeight = this.selfHeight / letterArr.length;
            int top = 0;
            for (String innerWrapper : letterArr) {
                this.wrappers.add(new InnerWrapper(0, top, this.selfWidth, top + wrapperHeight, innerWrapper, -16514044));
                top += wrapperHeight;
            }
        }
    }

    private void drawLetter(Canvas canvas) {
        this.paint.setTextSize(16.0f);
        this.paint.setAntiAlias(true);
        Paint.FontMetricsInt fontMetrics = this.paint.getFontMetricsInt();
        int txtHeight = fontMetrics.bottom - fontMetrics.ascent;
        for (InnerWrapper wrapper : this.wrappers) {
            RectF rectF = new RectF((float) wrapper.left, (float) wrapper.top, (float) wrapper.right, (float) wrapper.bottom);
            if (wrapper.selected) {
                this.paint.setColor(this.selectedBgColor);
            } else {
                this.paint.setColor(this.normalBgColor);
            }
            this.paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rectF, 5.0f, 5.0f, this.paint);
            this.paint.setColor(wrapper.textColor);
            this.paint.setStyle(Paint.Style.STROKE);
            canvas.drawText(wrapper.letter, (float) (wrapper.left + (((wrapper.right - wrapper.left) / 2) - (((int) this.paint.measureText(wrapper.letter)) / 2))), (float) ((wrapper.top + (((wrapper.bottom - wrapper.top) - txtHeight) / 2)) - fontMetrics.ascent), this.paint);
        }
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r5) {
        /*
            r4 = this;
            r3 = 1
            int r1 = r5.getAction()     // Catch:{ Exception -> 0x001a }
            switch(r1) {
                case 0: goto L_0x0009;
                case 1: goto L_0x002d;
                case 2: goto L_0x001f;
                default: goto L_0x0008;
            }     // Catch:{ Exception -> 0x001a }
        L_0x0008:
            return r3
        L_0x0009:
            r1 = 0
            r4.allowSetCurrent = r1     // Catch:{ Exception -> 0x001a }
            float r1 = r5.getX()     // Catch:{ Exception -> 0x001a }
            int r1 = (int) r1     // Catch:{ Exception -> 0x001a }
            float r2 = r5.getY()     // Catch:{ Exception -> 0x001a }
            int r2 = (int) r2     // Catch:{ Exception -> 0x001a }
            r4.downTouch(r1, r2)     // Catch:{ Exception -> 0x001a }
            goto L_0x0008
        L_0x001a:
            r0 = move-exception
            r0.printStackTrace()
            goto L_0x0008
        L_0x001f:
            float r1 = r5.getX()     // Catch:{ Exception -> 0x001a }
            int r1 = (int) r1     // Catch:{ Exception -> 0x001a }
            float r2 = r5.getY()     // Catch:{ Exception -> 0x001a }
            int r2 = (int) r2     // Catch:{ Exception -> 0x001a }
            r4.moveTouch(r1, r2)     // Catch:{ Exception -> 0x001a }
            goto L_0x0008
        L_0x002d:
            r4.upTouch()     // Catch:{ Exception -> 0x001a }
            r1 = 1
            r4.allowSetCurrent = r1     // Catch:{ Exception -> 0x001a }
            goto L_0x0008
        */
        throw new UnsupportedOperationException("Method not decompiled: com.xcharge.charger.p006ui.p009c2.activity.widget.KyLetterfilter.onTouchEvent(android.view.MotionEvent):boolean");
    }

    private void downTouch(int x, int y) {
        try {
            if (this.onLetterfilterListener != null) {
                this.onLetterfilterListener.start();
            }
            InnerWrapper oldWrapper = getSelectedWrapper();
            InnerWrapper touchedWrapper = getTouchedWrapper(x, y);
            if (oldWrapper != touchedWrapper) {
                if (this.onLetterfilterListener != null) {
                    this.onLetterfilterListener.letterChanged(touchedWrapper.letter);
                }
                resetSelectedFlag();
                touchedWrapper.selected = true;
                invalidate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void moveTouch(int x, int y) {
        InnerWrapper oldWrapper = getSelectedWrapper();
        InnerWrapper touchedWrapper = getTouchedWrapper(x, y);
        if (oldWrapper != touchedWrapper) {
            if (this.onLetterfilterListener != null) {
                this.onLetterfilterListener.letterChanged(touchedWrapper.letter);
            }
            resetSelectedFlag();
            touchedWrapper.selected = true;
            invalidate();
        }
    }

    private void upTouch() {
        if (this.onLetterfilterListener != null) {
            this.onLetterfilterListener.end();
        }
    }

    private InnerWrapper getTouchedWrapper(int x, int y) {
        for (InnerWrapper wrapper : this.wrappers) {
            if (x >= wrapper.left && x <= wrapper.right && y >= wrapper.top && y <= wrapper.bottom) {
                System.out.println("LetterfilterWidget downClick:单击了" + wrapper.letter);
                return wrapper;
            }
        }
        return null;
    }

    private void resetSelectedFlag() {
        for (InnerWrapper wrapper : this.wrappers) {
            wrapper.selected = false;
        }
    }

    private InnerWrapper getSelectedWrapper() {
        for (InnerWrapper wrapper : this.wrappers) {
            if (wrapper.selected) {
                return wrapper;
            }
        }
        return null;
    }

    public void setCurrent(String letter) {
        if (this.allowSetCurrent && this.wrappers != null) {
            InnerWrapper newWrapper = null;
            Iterator<InnerWrapper> it = this.wrappers.iterator();
            while (true) {
                if (it.hasNext()) {
                    InnerWrapper wrapper = it.next();
                    if (wrapper.letter.equals(letter)) {
                        newWrapper = wrapper;
                        break;
                    }
                } else {
                    break;
                }
            }
            if (newWrapper != null) {
                InnerWrapper oldWrapper = getSelectedWrapper();
                if (oldWrapper == null || !oldWrapper.letter.equals(letter)) {
                    resetSelectedFlag();
                    newWrapper.selected = true;
                    invalidate();
                }
            }
        }
    }

    /* renamed from: com.xcharge.charger.ui.c2.activity.widget.KyLetterfilter$InnerWrapper */
    private class InnerWrapper {
        int bottom;
        int left;
        String letter;
        int right;
        boolean selected = false;
        int textColor;
        int top;

        public InnerWrapper(int left2, int top2, int right2, int bottom2, String letter2, int textColor2) {
            this.left = left2;
            this.top = top2;
            this.right = right2;
            this.bottom = bottom2;
            this.letter = letter2;
            this.textColor = textColor2;
        }
    }

    public void setNormalBgColor(int normalBgColor2) {
        this.normalBgColor = normalBgColor2;
    }

    public void setSelectedBgColor(int selectedBgColor2) {
        this.selectedBgColor = selectedBgColor2;
    }

    public void setOnLetterfilterListener(OnLetterfilterListener onLetterfilterListener2) {
        this.onLetterfilterListener = onLetterfilterListener2;
    }
}
