package pl.droidsonroids.gif;

import android.content.ContentResolver;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.MediaController;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Locale;

/* loaded from: classes.dex */
public class GifDrawable extends Drawable implements Animatable, MediaController.MediaPlayerControl {
    private static final Handler UI_HANDLER;
    private boolean mApplyTransformation;
    private int[] mColors;
    private final Rect mDstRect;
    private volatile int mGifInfoPtr;
    private final long mInputSourceLength;
    private final Runnable mInvalidateTask;
    private volatile boolean mIsRunning;
    private final int[] mMetaData;
    protected final Paint mPaint;
    private final Runnable mResetTask;
    private final Runnable mSaveRemainderTask;
    private final Runnable mStartTask;
    private float mSx;
    private float mSy;

    private static native void free(int i);

    private static native long getAllocationByteCount(int i);

    private static native String getComment(int i);

    private static native int getCurrentPosition(int i);

    private static native int getDuration(int i);

    private static native int getLoopCount(int i);

    private static native int openByteArray(int[] iArr, byte[] bArr) throws GifIOException;

    private static native int openDirectByteBuffer(int[] iArr, ByteBuffer byteBuffer) throws GifIOException;

    private static native int openFd(int[] iArr, FileDescriptor fileDescriptor, long j) throws GifIOException;

    private static native int openFile(int[] iArr, String str) throws GifIOException;

    private static native int openStream(int[] iArr, InputStream inputStream) throws GifIOException;

    private static native void renderFrame(int[] iArr, int i, int[] iArr2);

    /* JADX INFO: Access modifiers changed from: private */
    public static native boolean reset(int i);

    /* JADX INFO: Access modifiers changed from: private */
    public static native int restoreRemainder(int i);

    /* JADX INFO: Access modifiers changed from: private */
    public static native int saveRemainder(int i);

    /* JADX INFO: Access modifiers changed from: private */
    public static native int seekToFrame(int i, int i2, int[] iArr);

    /* JADX INFO: Access modifiers changed from: private */
    public static native int seekToTime(int i, int i2, int[] iArr);

    private static native void setSpeedFactor(int i, float f);

    static {
        System.loadLibrary("gif");
        UI_HANDLER = new Handler(Looper.getMainLooper());
    }

    private static void runOnUiThread(Runnable task) {
        if (Looper.myLooper() == UI_HANDLER.getLooper()) {
            task.run();
        } else {
            UI_HANDLER.post(task);
        }
    }

    public GifDrawable(Resources res, int id) throws Resources.NotFoundException, IOException {
        this(res.openRawResourceFd(id));
    }

    public GifDrawable(AssetManager assets, String assetName) throws IOException {
        this(assets.openFd(assetName));
    }

    public GifDrawable(String filePath) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.1
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.2
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.3
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.4
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (filePath == null) {
            throw new NullPointerException("Source is null");
        }
        this.mInputSourceLength = new File(filePath).length();
        this.mGifInfoPtr = openFile(this.mMetaData, filePath);
        this.mColors = new int[this.mMetaData[0] * this.mMetaData[1]];
    }

    public GifDrawable(File file) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.1
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.2
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.3
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.4
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (file == null) {
            throw new NullPointerException("Source is null");
        }
        this.mInputSourceLength = file.length();
        this.mGifInfoPtr = openFile(this.mMetaData, file.getPath());
        this.mColors = new int[this.mMetaData[0] * this.mMetaData[1]];
    }

    public GifDrawable(InputStream stream) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.1
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.2
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.3
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.4
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (stream == null) {
            throw new NullPointerException("Source is null");
        }
        if (!stream.markSupported()) {
            throw new IllegalArgumentException("InputStream does not support marking");
        }
        this.mGifInfoPtr = openStream(this.mMetaData, stream);
        this.mColors = new int[this.mMetaData[0] * this.mMetaData[1]];
        this.mInputSourceLength = -1L;
    }

    public GifDrawable(AssetFileDescriptor afd) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.1
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.2
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.3
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.4
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (afd == null) {
            throw new NullPointerException("Source is null");
        }
        FileDescriptor fd = afd.getFileDescriptor();
        try {
            this.mGifInfoPtr = openFd(this.mMetaData, fd, afd.getStartOffset());
            this.mColors = new int[this.mMetaData[0] * this.mMetaData[1]];
            this.mInputSourceLength = afd.getLength();
        } catch (IOException ex) {
            afd.close();
            throw ex;
        }
    }

    public GifDrawable(FileDescriptor fd) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.1
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.2
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.3
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.4
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (fd == null) {
            throw new NullPointerException("Source is null");
        }
        this.mGifInfoPtr = openFd(this.mMetaData, fd, 0L);
        this.mColors = new int[this.mMetaData[0] * this.mMetaData[1]];
        this.mInputSourceLength = -1L;
    }

    public GifDrawable(byte[] bytes) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.1
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.2
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.3
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.4
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (bytes == null) {
            throw new NullPointerException("Source is null");
        }
        this.mGifInfoPtr = openByteArray(this.mMetaData, bytes);
        this.mColors = new int[this.mMetaData[0] * this.mMetaData[1]];
        this.mInputSourceLength = bytes.length;
    }

    public GifDrawable(ByteBuffer buffer) throws IOException {
        this.mIsRunning = true;
        this.mMetaData = new int[5];
        this.mSx = 1.0f;
        this.mSy = 1.0f;
        this.mDstRect = new Rect();
        this.mPaint = new Paint(6);
        this.mResetTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.1
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.reset(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mStartTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.2
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.restoreRemainder(GifDrawable.this.mGifInfoPtr);
                GifDrawable.this.invalidateSelf();
            }
        };
        this.mSaveRemainderTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.3
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.saveRemainder(GifDrawable.this.mGifInfoPtr);
            }
        };
        this.mInvalidateTask = new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.4
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.this.invalidateSelf();
            }
        };
        if (buffer == null) {
            throw new NullPointerException("Source is null");
        }
        if (!buffer.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer is not direct");
        }
        this.mGifInfoPtr = openDirectByteBuffer(this.mMetaData, buffer);
        this.mColors = new int[this.mMetaData[0] * this.mMetaData[1]];
        this.mInputSourceLength = buffer.capacity();
    }

    public GifDrawable(ContentResolver resolver, Uri uri) throws IOException {
        this(resolver.openAssetFileDescriptor(uri, "r"));
    }

    public void recycle() {
        this.mIsRunning = false;
        int tmpPtr = this.mGifInfoPtr;
        this.mGifInfoPtr = 0;
        this.mColors = null;
        free(tmpPtr);
    }

    protected void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicHeight() {
        return this.mMetaData[1];
    }

    @Override // android.graphics.drawable.Drawable
    public int getIntrinsicWidth() {
        return this.mMetaData[0];
    }

    @Override // android.graphics.drawable.Drawable
    public void setAlpha(int alpha) {
        this.mPaint.setAlpha(alpha);
    }

    @Override // android.graphics.drawable.Drawable
    public void setColorFilter(ColorFilter cf) {
        this.mPaint.setColorFilter(cf);
    }

    @Override // android.graphics.drawable.Drawable
    public int getOpacity() {
        return -2;
    }

    @Override // android.graphics.drawable.Animatable, android.widget.MediaController.MediaPlayerControl
    public void start() {
        this.mIsRunning = true;
        runOnUiThread(this.mStartTask);
    }

    public void reset() {
        runOnUiThread(this.mResetTask);
    }

    @Override // android.graphics.drawable.Animatable
    public void stop() {
        this.mIsRunning = false;
        runOnUiThread(this.mSaveRemainderTask);
    }

    @Override // android.graphics.drawable.Animatable
    public boolean isRunning() {
        return this.mIsRunning;
    }

    public String getComment() {
        return getComment(this.mGifInfoPtr);
    }

    public int getLoopCount() {
        return getLoopCount(this.mGifInfoPtr);
    }

    public String toString() {
        return String.format(Locale.US, "Size: %dx%d, %d frames, error: %d", Integer.valueOf(this.mMetaData[0]), Integer.valueOf(this.mMetaData[1]), Integer.valueOf(this.mMetaData[2]), Integer.valueOf(this.mMetaData[3]));
    }

    public int getNumberOfFrames() {
        return this.mMetaData[2];
    }

    public GifError getError() {
        return GifError.fromCode(this.mMetaData[3]);
    }

    public static GifDrawable createFromResource(Resources res, int resourceId) {
        try {
            return new GifDrawable(res, resourceId);
        } catch (IOException e) {
            return null;
        }
    }

    public void setSpeed(float factor) {
        if (factor <= 0.0f) {
            throw new IllegalArgumentException("Speed factor is not positive");
        }
        setSpeedFactor(this.mGifInfoPtr, factor);
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void pause() {
        stop();
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getDuration() {
        return getDuration(this.mGifInfoPtr);
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getCurrentPosition() {
        return getCurrentPosition(this.mGifInfoPtr);
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public void seekTo(final int position) {
        if (position < 0) {
            throw new IllegalArgumentException("Position is not positive");
        }
        runOnUiThread(new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.5
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.seekToTime(GifDrawable.this.mGifInfoPtr, position, GifDrawable.this.mColors);
                GifDrawable.this.invalidateSelf();
            }
        });
    }

    public void seekToFrame(final int frameIndex) {
        if (frameIndex < 0) {
            throw new IllegalArgumentException("frameIndex is not positive");
        }
        runOnUiThread(new Runnable() { // from class: pl.droidsonroids.gif.GifDrawable.6
            @Override // java.lang.Runnable
            public void run() {
                GifDrawable.seekToFrame(GifDrawable.this.mGifInfoPtr, frameIndex, GifDrawable.this.mColors);
                GifDrawable.this.invalidateSelf();
            }
        });
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean isPlaying() {
        return this.mIsRunning;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getBufferPercentage() {
        return 100;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canPause() {
        return true;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canSeekBackward() {
        return false;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public boolean canSeekForward() {
        return getNumberOfFrames() > 1;
    }

    @Override // android.widget.MediaController.MediaPlayerControl
    public int getAudioSessionId() {
        return 0;
    }

    public int getFrameByteCount() {
        return this.mMetaData[0] * this.mMetaData[1] * 4;
    }

    public long getAllocationByteCount() {
        long nativeSize = getAllocationByteCount(this.mGifInfoPtr);
        int[] colors = this.mColors;
        return colors == null ? nativeSize : nativeSize + (colors.length * 4);
    }

    public long getInputSourceByteCount() {
        return this.mInputSourceLength;
    }

    public void getPixels(int[] pixels) {
        int[] colors = this.mColors;
        if (colors != null) {
            if (pixels.length < colors.length) {
                throw new ArrayIndexOutOfBoundsException("Pixels array is too small. Required length: " + colors.length);
            }
            System.arraycopy(colors, 0, pixels, 0, colors.length);
        }
    }

    public int getPixel(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        }
        if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }
        if (x >= this.mMetaData[0]) {
            throw new IllegalArgumentException("x must be < GIF width");
        }
        if (y >= this.mMetaData[1]) {
            throw new IllegalArgumentException("y must be < GIF height");
        }
        int[] colors = this.mColors;
        if (colors == null) {
            throw new IllegalArgumentException("GifDrawable is recycled");
        }
        return colors[(this.mMetaData[1] * y) + x];
    }

    @Override // android.graphics.drawable.Drawable
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        this.mApplyTransformation = true;
    }

    @Override // android.graphics.drawable.Drawable
    public void draw(Canvas canvas) {
        if (this.mApplyTransformation) {
            this.mDstRect.set(getBounds());
            this.mSx = this.mDstRect.width() / this.mMetaData[0];
            this.mSy = this.mDstRect.height() / this.mMetaData[1];
            this.mApplyTransformation = false;
        }
        if (this.mPaint.getShader() == null) {
            if (this.mIsRunning) {
                renderFrame(this.mColors, this.mGifInfoPtr, this.mMetaData);
            } else {
                this.mMetaData[4] = -1;
            }
            canvas.scale(this.mSx, this.mSy);
            int[] colors = this.mColors;
            if (colors != null) {
                canvas.drawBitmap(colors, 0, this.mMetaData[0], 0.0f, 0.0f, this.mMetaData[0], this.mMetaData[1], true, this.mPaint);
            }
            if (this.mMetaData[4] >= 0 && this.mMetaData[2] > 1) {
                UI_HANDLER.postDelayed(this.mInvalidateTask, this.mMetaData[4]);
                return;
            }
            return;
        }
        canvas.drawRect(this.mDstRect, this.mPaint);
    }

    public final Paint getPaint() {
        return this.mPaint;
    }

    @Override // android.graphics.drawable.Drawable
    public int getAlpha() {
        return this.mPaint.getAlpha();
    }

    @Override // android.graphics.drawable.Drawable
    public void setFilterBitmap(boolean filter) {
        this.mPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public void setDither(boolean dither) {
        this.mPaint.setDither(dither);
        invalidateSelf();
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumHeight() {
        return this.mMetaData[1];
    }

    @Override // android.graphics.drawable.Drawable
    public int getMinimumWidth() {
        return this.mMetaData[0];
    }
}
