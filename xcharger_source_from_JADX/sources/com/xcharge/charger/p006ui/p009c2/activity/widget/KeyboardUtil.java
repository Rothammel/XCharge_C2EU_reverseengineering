package com.xcharge.charger.p006ui.p009c2.activity.widget;

import android.app.Activity;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import com.xcharge.charger.C0221R;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/* renamed from: com.xcharge.charger.ui.c2.activity.widget.KeyboardUtil */
public class KeyboardUtil {
    public static KeyboardUtil mInstance;
    private Activity activity;
    private EditText et_click;
    /* access modifiers changed from: private */
    public EditText et_visual;
    public boolean isShow = false;
    public boolean isUpper = false;
    /* access modifiers changed from: private */

    /* renamed from: k1 */
    public Keyboard f133k1;
    /* access modifiers changed from: private */

    /* renamed from: k2 */
    public Keyboard f134k2;
    /* access modifiers changed from: private */
    public KeyboardView keyboardView;
    private KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {
        public void swipeUp() {
        }

        public void swipeRight() {
        }

        public void swipeLeft() {
        }

        public void swipeDown() {
        }

        public void onText(CharSequence text) {
        }

        public void onRelease(int primaryCode) {
        }

        public void onPress(int primaryCode) {
            checkIShowPrewiew(primaryCode);
        }

        private void checkIShowPrewiew(int primaryCode) {
            if (Arrays.asList(new Integer[]{-3, -5, -1, 46, 32, 44}).contains(Integer.valueOf(primaryCode))) {
                KeyboardUtil.this.keyboardView.setPreviewEnabled(false);
            } else {
                KeyboardUtil.this.keyboardView.setPreviewEnabled(true);
            }
        }

        public void onKey(int primaryCode, int[] keyCodes) {
            checkIShowPrewiew(primaryCode);
            Log.i("KeyBoard", "primaryCode=" + primaryCode);
            Editable editable = KeyboardUtil.this.et_visual.getText();
            int start = KeyboardUtil.this.et_visual.getSelectionStart();
            if (primaryCode == -3) {
                KeyboardUtil.this.hideKeyboard();
            } else if (primaryCode == -5) {
                if (editable != null && editable.length() > 0 && start > 0) {
                    editable.delete(start - 1, start);
                }
            } else if (primaryCode == -1) {
                KeyboardUtil.this.changeKey();
                KeyboardUtil.this.keyboardView.setKeyboard(KeyboardUtil.this.f133k1);
            } else if (primaryCode == 1000) {
                KeyboardUtil.this.keyboardView.setKeyboard(KeyboardUtil.this.f134k2);
            } else if (primaryCode == 1001) {
                KeyboardUtil.this.keyboardView.setKeyboard(KeyboardUtil.this.f133k1);
            } else if (primaryCode == 1003) {
                editable.insert(start, StringUtils.SPACE);
            } else if (primaryCode == 1002) {
                if (start > 0) {
                    KeyboardUtil.this.et_visual.setSelection(start - 1);
                }
            } else if (primaryCode != 1004) {
                editable.insert(start, Character.toString((char) primaryCode));
            } else if (start < KeyboardUtil.this.et_visual.length()) {
                KeyboardUtil.this.et_visual.setSelection(start + 1);
            }
        }
    };

    public KeyboardUtil(Activity activity2, EditText editText) {
        this.activity = activity2;
        this.et_click = editText;
        this.f133k1 = new Keyboard(activity2, C0221R.xml.letter_softkeyboard);
        this.f134k2 = new Keyboard(activity2, C0221R.xml.character_softkeyboard);
        this.keyboardView = (KeyboardView) activity2.findViewById(C0221R.C0223id.keyboard_view);
        this.keyboardView.setKeyboard(this.f133k1);
        this.keyboardView.setEnabled(true);
        this.keyboardView.setOnKeyboardActionListener(this.listener);
        this.et_visual = (EditText) activity2.findViewById(C0221R.C0223id.et_visual);
        this.et_visual.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                int inputType = KeyboardUtil.this.et_visual.getInputType() | 524288;
                KeyboardUtil.this.et_visual.setInputType(0);
                KeyboardUtil.this.et_visual.setInputType(inputType);
                KeyboardUtil.this.et_visual.setSelection(KeyboardUtil.this.et_visual.getText().toString().length());
                return false;
            }
        });
    }

    /* access modifiers changed from: private */
    public void changeKey() {
        List<Keyboard.Key> keylist = this.f133k1.getKeys();
        if (this.isUpper) {
            this.isUpper = false;
            for (Keyboard.Key key : keylist) {
                if (key.label != null && isword(key.label.toString())) {
                    key.label = key.label.toString().toLowerCase();
                    key.codes[0] = key.codes[0] + 32;
                } else if (key.label.toString().equals(this.activity.getString(C0221R.string.keyboard_lower_case))) {
                    key.label = this.activity.getString(C0221R.string.keyboard_upper_case);
                }
            }
            return;
        }
        this.isUpper = true;
        for (Keyboard.Key key2 : keylist) {
            if (key2.label != null && isword(key2.label.toString())) {
                key2.label = key2.label.toString().toUpperCase();
                key2.codes[0] = key2.codes[0] - 32;
            } else if (key2.label.toString().equals(this.activity.getString(C0221R.string.keyboard_upper_case))) {
                key2.label = this.activity.getString(C0221R.string.keyboard_lower_case);
            }
        }
    }

    public void showKeyboard() {
        int visibility = this.keyboardView.getVisibility();
        if (visibility == 8 || visibility == 4) {
            int inputType = this.et_visual.getInputType() | 524288;
            this.et_visual.setInputType(0);
            this.keyboardView.setVisibility(0);
            this.et_visual.setVisibility(0);
            this.et_visual.setInputType(inputType);
            this.isShow = true;
        }
    }

    public void hideKeyboard() {
        if (this.keyboardView.getVisibility() == 0) {
            this.keyboardView.setVisibility(8);
            this.et_visual.setVisibility(8);
            this.et_click.setText(this.et_visual.getText().toString());
            this.et_visual.setText((CharSequence) null);
            this.isShow = false;
        }
    }

    private boolean isword(String str) {
        if ("abcdefghijklmnopqrstuvwxyz".indexOf(str.toLowerCase()) > -1) {
            return true;
        }
        return false;
    }

    public static KeyboardUtil shared(Activity activity2, EditText editText) {
        if (mInstance == null) {
            mInstance = new KeyboardUtil(activity2, editText);
        }
        mInstance.et_click = editText;
        mInstance.et_visual.setFocusable(true);
        mInstance.et_visual.setFocusableInTouchMode(true);
        mInstance.et_visual.requestFocus();
        String text = editText.getText().toString();
        mInstance.et_visual.setText(text);
        mInstance.et_visual.setSelection(text.length());
        return mInstance;
    }

    public static KeyboardUtil getmInstance() {
        return mInstance;
    }

    public static void setmInstance(KeyboardUtil mInstance2) {
        mInstance = mInstance2;
    }
}
