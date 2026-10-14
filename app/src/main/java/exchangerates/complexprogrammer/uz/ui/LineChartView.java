package exchangerates.complexprogrammer.uz.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import java.util.ArrayList;
import java.util.List;

import exchangerates.complexprogrammer.uz.R;
import exchangerates.complexprogrammer.uz.models.HistoryPoint;
import exchangerates.complexprogrammer.uz.services.Formatters;

/** Minimal line chart: min/max labels, first/last dates, and a touch tooltip. */
public class LineChartView extends View {
    private final List<HistoryPoint> points = new ArrayList<>();
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tooltipTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();
    private final RectF plot = new RectF();
    private final RectF tooltipRect = new RectF();
    private int selected = -1;
    private int lineColor;

    public LineChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        lineColor = themeColor(com.google.android.material.R.attr.colorPrimary);
        int textColor = ContextCompat.getColor(context, R.color.text_secondary);

        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2.5f));
        linePaint.setStrokeJoin(Paint.Join.ROUND);
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setColor(lineColor);

        gridPaint.setColor(ContextCompat.getColor(context, R.color.divider));
        gridPaint.setStrokeWidth(dp(1));

        labelPaint.setColor(textColor);
        labelPaint.setTextSize(sp(11));

        markerPaint.setColor(lineColor);

        tooltipPaint.setColor(ContextCompat.getColor(context, R.color.text_primary));
        tooltipTextPaint.setColor(ContextCompat.getColor(context, R.color.surface));
        tooltipTextPaint.setTextSize(sp(12));
    }

    public void setPoints(List<HistoryPoint> data) {
        points.clear();
        if (data != null) points.addAll(data);
        selected = -1;
        updateDescription();
        invalidate();
    }

    private void updateDescription() {
        if (points.size() < 2) {
            setContentDescription(null);
            return;
        }
        HistoryPoint first = points.get(0);
        HistoryPoint last = points.get(points.size() - 1);
        Context c = getContext();
        setContentDescription(Formatters.fullDate(c, first.time) + ": " + Formatters.money(c, first.rate) + " → "
                + Formatters.fullDate(c, last.time) + ": " + Formatters.money(c, last.rate));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (points.size() < 2) return;

        float labelHeight = labelPaint.getTextSize();
        plot.set(getPaddingLeft(), getPaddingTop() + labelHeight + dp(6),
                getWidth() - getPaddingRight(), getHeight() - getPaddingBottom() - labelHeight * 2 - dp(12));

        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (HistoryPoint p : points) {
            min = Math.min(min, p.rate);
            max = Math.max(max, p.rate);
        }
        double range = max - min;
        if (range == 0) range = Math.max(1, max * 0.01);

        // Grid: top (max) and bottom (min).
        canvas.drawLine(plot.left, plot.top, plot.right, plot.top, gridPaint);
        canvas.drawLine(plot.left, plot.bottom, plot.right, plot.bottom, gridPaint);
        Context c = getContext();
        canvas.drawText(Formatters.money(c, max), plot.left, plot.top - dp(4), labelPaint);
        canvas.drawText(Formatters.money(c, min), plot.left, plot.bottom + labelHeight + dp(2), labelPaint);
        String lastDate = Formatters.shortDate(c, points.get(points.size() - 1).time);
        String firstDate = Formatters.shortDate(c, points.get(0).time);
        float bottomLabelY = getHeight() - getPaddingBottom() - dp(1);
        canvas.drawText(firstDate, plot.left, bottomLabelY, labelPaint);
        canvas.drawText(lastDate, plot.right - labelPaint.measureText(lastDate), bottomLabelY, labelPaint);

        linePath.reset();
        fillPath.reset();
        for (int i = 0; i < points.size(); i++) {
            float x = xFor(i);
            float y = yFor(points.get(i).rate, min, range);
            if (i == 0) {
                linePath.moveTo(x, y);
                fillPath.moveTo(x, plot.bottom);
            }
            if (i > 0) linePath.lineTo(x, y);
            fillPath.lineTo(x, y);
        }
        fillPath.lineTo(xFor(points.size() - 1), plot.bottom);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(0, plot.top, 0, plot.bottom,
                ColorUtils.setAlphaComponent(lineColor, 70), Color.TRANSPARENT, Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);
        canvas.drawPath(linePath, linePaint);

        if (selected >= 0 && selected < points.size()) {
            HistoryPoint p = points.get(selected);
            float x = xFor(selected);
            float y = yFor(p.rate, min, range);
            canvas.drawLine(x, plot.top, x, plot.bottom, gridPaint);
            canvas.drawCircle(x, y, dp(5), markerPaint);

            String text = Formatters.fullDate(c, p.time) + "  " + Formatters.money(c, p.rate);
            float padding = dp(8);
            float width = tooltipTextPaint.measureText(text) + padding * 2;
            float height = tooltipTextPaint.getTextSize() + padding * 2;
            float left = Math.max(plot.left, Math.min(x - width / 2, plot.right - width));
            float top = Math.max(0, y - height - dp(10));
            tooltipRect.set(left, top, left + width, top + height);
            canvas.drawRoundRect(tooltipRect, dp(6), dp(6), tooltipPaint);
            canvas.drawText(text, left + padding, top + padding + tooltipTextPaint.getTextSize() * 0.85f, tooltipTextPaint);
        }
    }

    private float xFor(int index) {
        return plot.left + plot.width() * index / (points.size() - 1);
    }

    private float yFor(double rate, double min, double range) {
        return (float) (plot.bottom - (rate - min) / range * plot.height());
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (points.size() < 2) return false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                float fraction = (event.getX() - plot.left) / Math.max(1, plot.width());
                selected = Math.round(Math.max(0, Math.min(1, fraction)) * (points.size() - 1));
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(event);
    }

    private int themeColor(int attr) {
        TypedArray array = getContext().obtainStyledAttributes(new int[]{attr});
        int color = array.getColor(0, Color.BLUE);
        array.recycle();
        return color;
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
