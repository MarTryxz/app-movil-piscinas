package com.example.myapplication;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

public class WaveView extends View {

    private Paint wavePaint;
    private Path wavePath;
    private int waveAmplitude = 50;
    private int waveLength = 400;
    private int waveOffset = 0;
    private ValueAnimator animator;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wavePaint = new Paint();
        wavePaint.setColor(Color.parseColor("#00658E")); // Color de la ola (ajusta si quieres)
        wavePaint.setStyle(Paint.Style.FILL);
        wavePaint.setAntiAlias(true);

        wavePath = new Path();

        animator = ValueAnimator.ofInt(0, waveLength);
        animator.setDuration(1500);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            waveOffset = (int) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        wavePath.reset();
        int halfHeight = getHeight() / 2;
        wavePath.moveTo(-waveLength + waveOffset, halfHeight);

        for (int i = -waveLength; i < getWidth() + waveLength; i += waveLength) {
            wavePath.rQuadTo(waveLength / 4, -waveAmplitude, waveLength / 2, 0);
            wavePath.rQuadTo(waveLength / 4, waveAmplitude, waveLength / 2, 0);
        }

        wavePath.lineTo(getWidth(), getHeight());
        wavePath.lineTo(0, getHeight());
        wavePath.close();

        canvas.drawPath(wavePath, wavePaint);
    }
}