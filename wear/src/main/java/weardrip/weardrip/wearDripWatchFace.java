package weardrip.weardrip;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.format.Time;
import android.util.Log;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.lang.String;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class wearDripWatchFace {

    private static final String TIME_FORMAT_WITHOUT_SECONDS = "%02d:%02d";
    private static final String TIME_FORMAT_WITH_SECONDS = TIME_FORMAT_WITHOUT_SECONDS + ":%02d";

    private static final int DATE_AND_TIME_DEFAULT_COLOUR = Color.WHITE;
    private static final int BACKGROUND_DEFAULT_COLOUR = Color.BLACK;

    String Text;
    Boolean wfChange = true;

    private final Paint timePaint;
    private final Paint backgroundPaint;
    private final Paint chartbackgroundPaint;
    private final Time time;
    private final Paint TextPaint;
    private final Paint chartPaint;

    private boolean shouldShowSeconds = true;
    private int backgroundColour = BACKGROUND_DEFAULT_COLOUR;
    private int dateAndTimeColour = DATE_AND_TIME_DEFAULT_COLOUR;




    public static wearDripWatchFace newInstance(Context context) {
        Paint timePaint = new Paint();
        timePaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        timePaint.setTextSize(context.getResources().getDimension(R.dimen.time_size));
        timePaint.setAntiAlias(true);

        Paint TextPaint = new Paint();
        TextPaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        TextPaint.setTextSize(context.getResources().getDimension(R.dimen.text_size));
        TextPaint.setAntiAlias(true);

        Paint chartPaint = new Paint();
        chartPaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        chartPaint.setTextSize(context.getResources().getDimension(R.dimen.text_size));
        chartPaint.setAntiAlias(true);

        Paint backgroundPaint = new Paint();
        backgroundPaint.setColor(BACKGROUND_DEFAULT_COLOUR);

        Paint chartbackgroundPaint = new Paint();
        chartbackgroundPaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);


        return new wearDripWatchFace(
                timePaint,
                backgroundPaint,
                TextPaint,
                chartPaint,
                chartbackgroundPaint,
                new Time());
    }



    wearDripWatchFace(Paint timePaint,
                      Paint backgroundPaint,
                      Paint TextPaint,
                      Paint chartPaint,
                      Paint chartbackgroundPaint,
                      Time time) {
        this.timePaint = timePaint;
        this.TextPaint = TextPaint;
        this.chartPaint = chartPaint;
        this.backgroundPaint = backgroundPaint;
        this.chartbackgroundPaint = chartbackgroundPaint;
        this.time = time;

    }


    public void showBG() {
        BgReading mBgReading;
        mBgReading = BgReading.last();
        if(mBgReading != null) {
            double calculated_value = mBgReading.calculated_value;
            DecimalFormat df = new DecimalFormat("#.#");
            Text = String.valueOf(df.format(calculated_value));
        } else {
            Text = "Null";
        }
    }

    public void wfChangeCase0(){wfChange = true;}
    public void wfChangeCase1(){wfChange = false;}

    public void draw(Canvas canvas, Rect bounds) {
        time.setToNow();
        showBG();
        if (wfChange == true){

            canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);
            canvas.drawRect(20, 20, 200, 175, chartbackgroundPaint);

            float TextXOffset = computeXOffset(Text, TextPaint, bounds);
            float TextYOffset = computeTextYOffset(Text, TextPaint, bounds);
            canvas.drawText(Text, TextXOffset, TextYOffset, TextPaint);

            String timeText = String.format(shouldShowSeconds ? TIME_FORMAT_WITH_SECONDS : TIME_FORMAT_WITHOUT_SECONDS, time.hour, time.minute, time.second);
            float timeXOffset = computeXOffset(timeText, timePaint, bounds);
            float timeYOffset = computeTimeYOffset(timeText, timePaint);
            canvas.drawText(timeText, timeXOffset, timeYOffset + TextYOffset, timePaint);


        }

        else if (wfChange == false){
            canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);
            String timeText = String.format(shouldShowSeconds ? TIME_FORMAT_WITH_SECONDS : TIME_FORMAT_WITHOUT_SECONDS, time.hour, time.minute, time.second);
            float timeXOffset = computeXOffset(timeText, timePaint, bounds);
            float timeYOffset = computeTimeYOffset(timeText, timePaint);
            canvas.drawText(timeText, timeXOffset, timeYOffset, timePaint);

            float TextXOffset = computeXOffset(Text, TextPaint, bounds);
            float TextYOffset = computeTextYOffset(Text, TextPaint, bounds);
            canvas.drawText(Text, TextXOffset, TextYOffset + timeYOffset, TextPaint);
        }
    }

    private float computeXOffset(String text, Paint paint, Rect watchBounds) {
        float centerX = watchBounds.exactCenterX();
        float timeLength = paint.measureText(text);
        return centerX - (timeLength / 2.0f);
    }

    private float computeTextYOffset(String Text, Paint TextPaint, Rect watchBounds) {
        float centerY = watchBounds.exactCenterY();
        Rect textBounds = new Rect();
        timePaint.getTextBounds(Text, 0, Text.length(), textBounds);
        int textHeight = textBounds.height();
        return centerY + (textHeight / 2.0f);
    }

    private float computeTimeYOffset(String timeText, Paint timePaint) {
        Rect textBounds = new Rect();
        TextPaint.getTextBounds(timeText, 0, timeText.length(), textBounds);
        return textBounds.height() + 10.0f;
    }

    public void setAntiAlias(boolean antiAlias) {
        timePaint.setAntiAlias(antiAlias);
        TextPaint.setAntiAlias(antiAlias);
    }

    public void updateDateAndTimeColourTo(int colour) {
        dateAndTimeColour = colour;
        timePaint.setColor(colour);
    }

    public void updateTimeZoneWith(String timeZone) {
        time.clear(timeZone);
        time.setToNow();
    }


    public void setShowSeconds(boolean showSeconds) {
        shouldShowSeconds = showSeconds;
    }

    public void updateBackgroundColourTo(int colour) {
        backgroundColour = colour;
        backgroundPaint.setColor(colour);
    }

    public void restoreBackgroundColour() {
        backgroundPaint.setColor(backgroundColour);
    }

    public void updateBackgroundColourToDefault() {
        backgroundPaint.setColor(BACKGROUND_DEFAULT_COLOUR);
    }

    public void updateDateAndTimeColourToDefault() {
        timePaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        TextPaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
    }

    public void restoreDateAndTimeColour() {
        timePaint.setColor(dateAndTimeColour);
        TextPaint.setColor(dateAndTimeColour);
    }
}