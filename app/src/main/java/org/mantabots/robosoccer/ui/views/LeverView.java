/* -------------------------------------------------------
   Copyright (c) [2025] Nadege LEMPERIERE
   All rights reserved
   -------------------------------------------------------
   EV3 lever view
   ------------------------------------------------------- */
package org.mantabots.robosoccer.ui.views;

/* Java includes */
import java.util.concurrent.atomic.AtomicBoolean;

/* Android includes */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/* Local includes */
import org.mantabots.robosoccer.R;

@SuppressWarnings("unused")
public class LeverView extends View implements Runnable {

    /* --------- listener --------- */
    public interface OnMoveListener {
        /** @param strength  −100 … +100  (up = +) */
        void onMove(int strength);
    }

    /* ------------------------ Constants -------------------------- */

    /** Default color for button */
    private static final int DEFAULT_COLOR_RAIL = Color.GRAY;
    private static final int DEFAULT_COLOR_KNOB = Color.BLACK;


    private OnMoveListener moveListener;
    private int loopInterval = 50;                 // ms
    private Thread loopThread;

    /* --------- paints --------- */
    private final Paint railPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knobPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    /* --------- geometry --------- */
    private int centerX, railTop, railBottom;
    private int knobRadius;
    private int knobY;

    /* --------- behaviour flags --------- */
    private boolean autoRecenter = true;
    private final AtomicBoolean enabled = new AtomicBoolean(true);

    /* === ctor === */
    public LeverView(Context ctx) { this(ctx, null); }
    public LeverView(Context ctx, AttributeSet attrs) { this(ctx, attrs, 0); }
    public LeverView(Context ctx, AttributeSet attrs, int defStyleAttr) {
        super(ctx, attrs, defStyleAttr);

        TypedArray styledAttributes = ctx.obtainStyledAttributes(attrs, R.styleable.LeverView);

        int railColor;
        int knobColor;

        try {
            autoRecenter = styledAttributes.getBoolean(R.styleable.LeverView_LV_autoRecenter, true);
            railColor = styledAttributes.getColor(R.styleable.LeverView_LV_railColor, DEFAULT_COLOR_RAIL);
            knobColor = styledAttributes.getColor(R.styleable.LeverView_LV_knobColor, DEFAULT_COLOR_KNOB);
        } finally {
            styledAttributes.recycle();
        }

        railPaint.setColor(railColor);
        railPaint.setStrokeWidth(6f);
        knobPaint.setColor(knobColor);

    }

    /* ===== public helpers ===== */

    /** Same signature style as JoystickView. */
    public void setOnMoveListener(OnMoveListener l) {
        setOnMoveListener(l, loopInterval);
    }

    public void setOnMoveListener(OnMoveListener l, int intervalMs) {
        moveListener = l;
        loopInterval = intervalMs;
    }

    public void setEnabled(boolean e) { enabled.set(e); }
    public boolean isEnabled()        { return enabled.get(); }

    public void setAutoRecenter(boolean r) { autoRecenter = r; }

    /* ===== geometry ===== */
    @Override protected void onSizeChanged(int w, int h, int ow, int oh) {
        centerX    = w / 2;
        railTop    = getPaddingTop()    + 4;
        railBottom = h - getPaddingBottom() - 4;
        knobRadius = Math.min(w, h) / 8;
        knobY      = (railTop + railBottom) / 2;      // start centred
    }

    /* ===== drawing ===== */
    @Override protected void onDraw(Canvas c) {
        /* rail */
        c.drawLine(centerX, railTop, centerX, railBottom, railPaint);
        /* knob */
        c.drawCircle(centerX, knobY, knobRadius, knobPaint);
    }

    /* ===== touch handling ===== */
    @Override public boolean onTouchEvent(MotionEvent e) {
        if (!enabled.get()) return true;

        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                startLoop();
                updateKnob((int) e.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                updateKnob((int) e.getY());
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                stopLoop();
                if (autoRecenter) updateKnob((railTop + railBottom) / 2);
                else if (moveListener != null) moveListener.onMove(calcStrength());
                break;
        }
        return true;
    }

    private void updateKnob(int y) {
        knobY = Math.max(railTop + knobRadius,
                Math.min(railBottom - knobRadius, y));
        invalidate();
        if (moveListener != null) moveListener.onMove(calcStrength());
    }

    /** Convert knobY to −100 … +100 (up = positive). */
    private int calcStrength() {
        float half = (railBottom - railTop - 2f * knobRadius) / 2f;
        return Math.round((railTop + knobRadius + half - knobY) * 100f / half);
    }

    /* ===== periodic callback ===== */
    private void startLoop() {
        stopLoop();
        loopThread = new Thread(this);
        loopThread.start();
    }

    private void stopLoop() {
        if (loopThread != null) loopThread.interrupt();
        loopThread = null;
    }

    @Override public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            if (moveListener != null) post(() -> moveListener.onMove(calcStrength()));
            try { Thread.sleep(loopInterval); }
            catch (InterruptedException ie) { break; }
        }
    }
}