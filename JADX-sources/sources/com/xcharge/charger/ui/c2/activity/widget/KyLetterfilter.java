package com.xcharge.charger.ui.c2.activity.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.xcharge.charger.protocol.ocpp.bean.types.Phase;
import com.xcharge.charger.protocol.ocpp.bean.types.UnitOfMeasure;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* loaded from: classes.dex */
public class KyLetterfilter extends View {
    private boolean allowSetCurrent;
    private int normalBgColor;
    private OnLetterfilterListener onLetterfilterListener;
    private Paint paint;
    private int selectedBgColor;
    private int selfHeight;
    private int selfWidth;
    private List<InnerWrapper> wrappers;

    /* loaded from: classes.dex */
    public interface OnLetterfilterListener {
        void end();

        void letterChanged(String str);

        void start();
    }

    public KyLetterfilter(Context context) {
        super(context);
        this.paint = new Paint();
        this.normalBgColor = -1;
        this.selectedBgColor = -13388315;
        this.allowSetCurrent = true;
    }

    public KyLetterfilter(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.paint = new Paint();
        this.normalBgColor = -1;
        this.selectedBgColor = -13388315;
        this.allowSetCurrent = true;
    }

    public KyLetterfilter(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.paint = new Paint();
        this.normalBgColor = -1;
        this.selectedBgColor = -13388315;
        this.allowSetCurrent = true;
    }

    @Override // android.view.View
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.selfWidth = 30;
        this.selfHeight = 415;
        setMeasuredDimension(this.selfWidth, this.selfHeight);
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(this.normalBgColor);
        createWrappers();
        drawLetter(canvas);
    }

    private void createWrappers() {
        if (this.wrappers == null) {
            String[] letterArr = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", UnitOfMeasure.K, "L", "M", Phase.N, "O", "P", "Q", "R", "S", "T", "U", UnitOfMeasure.V, "W", "X", "Y", "Z"};
            this.wrappers = new ArrayList();
            int wrapperHeight = this.selfHeight / letterArr.length;
            int top = 0;
            for (String str : letterArr) {
                int bottom = top + wrapperHeight;
                InnerWrapper wrapper = new InnerWrapper(0, top, this.selfWidth, bottom, str, -16514044);
                this.wrappers.add(wrapper);
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
            RectF rectF = new RectF(wrapper.left, wrapper.top, wrapper.right, wrapper.bottom);
            if (wrapper.selected) {
                this.paint.setColor(this.selectedBgColor);
            } else {
                this.paint.setColor(this.normalBgColor);
            }
            this.paint.setStyle(Paint.Style.FILL);
            canvas.drawRoundRect(rectF, 5.0f, 5.0f, this.paint);
            this.paint.setColor(wrapper.textColor);
            this.paint.setStyle(Paint.Style.STROKE);
            int txtWidth = (int) this.paint.measureText(wrapper.letter);
            canvas.drawText(wrapper.letter, wrapper.left + (((wrapper.right - wrapper.left) / 2) - (txtWidth / 2)), (wrapper.top + (((wrapper.bottom - wrapper.top) - txtHeight) / 2)) - fontMetrics.ascent, this.paint);
        }
    }

    @Override // android.view.View
    public boolean onTouchEvent(MotionEvent event) {
        try {
            switch (event.getAction()) {
                case 0:
                    this.allowSetCurrent = false;
                    downTouch((int) event.getX(), (int) event.getY());
                    break;
                case 1:
                    upTouch();
                    this.allowSetCurrent = true;
                    break;
                case 2:
                    moveTouch((int) event.getX(), (int) event.getY());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
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
            Iterator<InnerWrapper> it2 = this.wrappers.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                InnerWrapper wrapper = it2.next();
                if (wrapper.letter.equals(letter)) {
                    newWrapper = wrapper;
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

    /* JADX INFO: Access modifiers changed from: private */
    /* loaded from: classes.dex */
    public class InnerWrapper {
        int bottom;
        int left;
        String letter;
        int right;
        boolean selected = false;
        int textColor;
        int top;

        public InnerWrapper(int left, int top, int right, int bottom, String letter, int textColor) {
            this.left = left;
            this.top = top;
            this.right = right;
            this.bottom = bottom;
            this.letter = letter;
            this.textColor = textColor;
        }
    }

    public void setNormalBgColor(int normalBgColor) {
        this.normalBgColor = normalBgColor;
    }

    public void setSelectedBgColor(int selectedBgColor) {
        this.selectedBgColor = selectedBgColor;
    }

    public void setOnLetterfilterListener(OnLetterfilterListener onLetterfilterListener) {
        this.onLetterfilterListener = onLetterfilterListener;
    }
}
