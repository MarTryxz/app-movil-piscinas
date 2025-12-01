package com.example.myapplication;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.card.MaterialCardView;

public class CircularGauge extends MaterialCardView {

    private TextView tvGaugeLabel;
    private ProgressBar gaugeProgressBar;
    private TextView tvGaugeValue;
    private String gaugeUnit = "";

    public CircularGauge(@NonNull Context context) {
        this(context, null);
    }

    public CircularGauge(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircularGauge(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        // Inflate the layout
        LayoutInflater.from(context).inflate(R.layout.circular_gauge, this, true);

        // Bind views
        tvGaugeLabel = findViewById(R.id.tvGaugeLabel);
        gaugeProgressBar = findViewById(R.id.gaugeProgressBar);
        tvGaugeValue = findViewById(R.id.tvGaugeValue);

        // Set CardView properties to match previous XML
        setRadius(getResources().getDimension(R.dimen.card_corner_radius));
        setCardElevation(getResources().getDimension(R.dimen.card_elevation));
        setCardBackgroundColor(getResources().getColor(R.color.surface, context.getTheme()));

        // Read attributes
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircularGauge);
            try {
                String title = a.getString(R.styleable.CircularGauge_gaugeTitle);
                if (title != null) {
                    tvGaugeLabel.setText(title);
                }

                String unit = a.getString(R.styleable.CircularGauge_gaugeUnit);
                if (unit != null) {
                    gaugeUnit = unit;
                }
            } finally {
                a.recycle();
            }
        }
    }

    public void setValue(double value) {
        String formattedValue;
        // Simple formatting logic, can be enhanced
        if (value % 1 == 0) {
            formattedValue = String.format("%.0f", value);
        } else {
            formattedValue = String.format("%.1f", value);
        }

        if (!gaugeUnit.isEmpty()) {
            tvGaugeValue.setText(formattedValue + gaugeUnit);
        } else {
            tvGaugeValue.setText(formattedValue);
        }
    }

    public void setValue(String text) {
        tvGaugeValue.setText(text);
    }

    public void setProgress(int progress) {
        gaugeProgressBar.setProgress(progress);
    }

    public void setMax(int max) {
        gaugeProgressBar.setMax(max);
    }
}
