package com.example.myapplication.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.myapplication.R;

import java.util.ArrayList;
import java.util.List;

public class BodyChartView extends View {

    private Paint weightPaint;
    private Paint weightFillPaint;
    private Paint bmiPaint;
    private Paint bmiFillPaint;
    private Paint gridPaint;
    private Paint textPaint;
    private Paint dotPaint;

    private Path weightPath;
    private Path weightFillPath;
    private Path bmiPath;
    private Path bmiFillPath;

    private List<Float> weightData;
    private List<Float> bmiData;
    private List<String> dateLabels;

    private float padding = 60f;
    private float bottomPadding = 40f;
    private float rightPadding = 20f;

    private int weightColor;
    private int bmiColor;

    public BodyChartView(Context context) {
        super(context);
        init();
    }

    public BodyChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BodyChartView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        weightColor = ContextCompat.getColor(getContext(), R.color.primary);
        bmiColor = ContextCompat.getColor(getContext(), R.color.accent);

        weightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        weightPaint.setStyle(Paint.Style.STROKE);
        weightPaint.setStrokeWidth(4f);
        weightPaint.setColor(weightColor);
        weightPaint.setStrokeCap(Paint.Cap.ROUND);
        weightPaint.setStrokeJoin(Paint.Join.ROUND);

        weightFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        weightFillPaint.setStyle(Paint.Style.FILL);

        bmiPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bmiPaint.setStyle(Paint.Style.STROKE);
        bmiPaint.setStrokeWidth(4f);
        bmiPaint.setColor(bmiColor);
        bmiPaint.setStrokeCap(Paint.Cap.ROUND);
        bmiPaint.setStrokeJoin(Paint.Join.ROUND);

        bmiFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bmiFillPaint.setStyle(Paint.Style.FILL);

        gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(1f);
        gridPaint.setColor(Color.parseColor("#333333"));
        gridPaint.setPathEffect(new DashPathEffect(new float[]{5, 5}, 0));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#666666"));
        textPaint.setTextSize(24f);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.WHITE);

        weightPath = new Path();
        weightFillPath = new Path();
        bmiPath = new Path();
        bmiFillPath = new Path();

        weightData = new ArrayList<>();
        bmiData = new ArrayList<>();
        dateLabels = new ArrayList<>();
    }

    public void setData(List<Float> weights, List<Float> bmis, List<String> labels) {
        this.weightData.clear();
        this.bmiData.clear();
        this.dateLabels.clear();

        if (weights != null) this.weightData.addAll(weights);
        if (bmis != null) this.bmiData.addAll(bmis);
        if (labels != null) this.dateLabels.addAll(labels);

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (weightData.isEmpty() && bmiData.isEmpty()) {
            return;
        }

        float width = getWidth();
        float height = getHeight();
        float chartWidth = width - padding - rightPadding;
        float chartHeight = height - padding - bottomPadding;

        // Draw grid lines
        drawGrid(canvas, chartWidth, chartHeight);

        // Draw labels
        drawLabels(canvas, chartWidth, chartHeight);

        // Draw weight line and fill
        if (!weightData.isEmpty()) {
            drawDataLine(canvas, weightData, weightPath, weightFillPath,
                    weightPaint, weightFillPaint, weightColor, chartWidth, chartHeight, false);
        }

        // Draw BMI line and fill
        if (!bmiData.isEmpty()) {
            drawDataLine(canvas, bmiData, bmiPath, bmiFillPath,
                    bmiPaint, bmiFillPaint, bmiColor, chartWidth, chartHeight, true);
        }
    }

    private void drawGrid(Canvas canvas, float chartWidth, float chartHeight) {
        // Horizontal grid lines
        for (int i = 0; i <= 4; i++) {
            float y = padding + (chartHeight / 4) * i;
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint);
        }

        // Vertical grid lines
        int verticalLines = Math.max(weightData.size(), bmiData.size());
        if (verticalLines > 1) {
            for (int i = 0; i < verticalLines; i++) {
                float x = padding + (chartWidth / (verticalLines - 1)) * i;
                canvas.drawLine(x, padding, x, padding + chartHeight, gridPaint);
            }
        }
    }

    private void drawLabels(Canvas canvas, float chartWidth, float chartHeight) {
        // Draw Y-axis labels for weight
        if (!weightData.isEmpty()) {
            float minWeight = getMinValue(weightData);
            float maxWeight = getMaxValue(weightData);
            canvas.drawText(String.format("%.0f", maxWeight), 10, padding + 10, textPaint);
            canvas.drawText(String.format("%.0f", minWeight), 10, padding + chartHeight, textPaint);
        }

        // Draw X-axis date labels
        if (!dateLabels.isEmpty() && dateLabels.size() > 1) {
            int labelCount = Math.min(dateLabels.size(), 5);
            for (int i = 0; i < labelCount; i++) {
                int index = (dateLabels.size() - 1) * i / (labelCount - 1);
                float x = padding + (chartWidth / (dateLabels.size() - 1)) * index;
                float y = padding + chartHeight + 30;
                String label = dateLabels.get(index);
                if (label.length() > 5) {
                    label = label.substring(5);
                }
                canvas.drawText(label, x - 15, y, textPaint);
            }
        }
    }

    private void drawDataLine(Canvas canvas, List<Float> data, Path linePath, Path fillPath,
                             Paint linePaint, Paint fillPaint, int color,
                             float chartWidth, float chartHeight, boolean isBmi) {
        if (data.size() < 2) {
            // Draw single point
            if (data.size() == 1) {
                float x = padding + chartWidth / 2;
                float value = data.get(0);
                float minVal = isBmi ? 10 : getMinValue(weightData);
                float maxVal = isBmi ? 35 : getMaxValue(weightData);
                float y = padding + chartHeight - ((value - minVal) / (maxVal - minVal)) * chartHeight;

                dotPaint.setColor(color);
                canvas.drawCircle(x, y, 8f, dotPaint);
            }
            return;
        }

        float minVal = isBmi ? 10 : getMinValue(data);
        float maxVal = isBmi ? 35 : getMaxValue(data);

        // Normalize range
        if (maxVal == minVal) maxVal = minVal + 1;

        linePath.reset();
        fillPath.reset();

        float stepX = chartWidth / (data.size() - 1);
        float startX = padding;
        float startY = padding + chartHeight - ((data.get(0) - minVal) / (maxVal - minVal)) * chartHeight;

        linePath.moveTo(startX, startY);
        fillPath.moveTo(startX, padding + chartHeight);
        fillPath.lineTo(startX, startY);

        for (int i = 1; i < data.size(); i++) {
            float x = startX + stepX * i;
            float y = padding + chartHeight - ((data.get(i) - minVal) / (maxVal - minVal)) * chartHeight;
            linePath.lineTo(x, y);
            fillPath.lineTo(x, y);
        }

        float lastX = startX + stepX * (data.size() - 1);
        fillPath.lineTo(lastX, padding + chartHeight);
        fillPath.close();

        // Create gradient for fill
        LinearGradient gradient = new LinearGradient(0, padding, 0, padding + chartHeight,
                color & 0x40FFFFFF, color & 0x10FFFFFF, Shader.TileMode.CLAMP);
        fillPaint.setShader(gradient);

        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        // Draw dots at data points
        dotPaint.setColor(color);
        for (int i = 0; i < data.size(); i++) {
            float x = startX + stepX * i;
            float y = padding + chartHeight - ((data.get(i) - minVal) / (maxVal - minVal)) * chartHeight;
            canvas.drawCircle(x, y, 6f, dotPaint);
            canvas.drawCircle(x, y, 6f, linePaint);
        }
    }

    private float getMinValue(List<Float> data) {
        if (data.isEmpty()) return 0;
        float min = data.get(0);
        for (float val : data) {
            if (val < min) min = val;
        }
        return (float) (Math.floor(min / 5) * 5); // Round down to nearest 5
    }

    private float getMaxValue(List<Float> data) {
        if (data.isEmpty()) return 100;
        float max = data.get(0);
        for (float val : data) {
            if (val > max) max = val;
        }
        return (float) (Math.ceil(max / 5) * 5); // Round up to nearest 5
    }
}
