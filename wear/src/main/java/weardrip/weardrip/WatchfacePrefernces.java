package weardrip.weardrip;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;

import preference.WearPreferenceActivity;


public class WatchfacePrefernces extends WearPreferenceActivity {

    private static final int DATE_AND_TIME_DEFAULT_COLOUR = Color.WHITE;
    private static final int BACKGROUND_DEFAULT_COLOUR = Color.BLACK;
    Boolean wfChange = true;
    private int backgroundColour = BACKGROUND_DEFAULT_COLOUR;
    private int dateAndTimeColour = DATE_AND_TIME_DEFAULT_COLOUR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    public void wfChangeCase0() {
        wfChange = true;
    }

    public void wfChangeCase1() {
        wfChange = false;
    }

    public void draw(Canvas canvas, Rect bounds) {
//        time.setToNow();
        if (wfChange == true) {

            //canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);
            //canvas.drawRect(20, 20, 200, 175, chartbackgroundPaint);
        } else if (wfChange == false) {
            //canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);
            //canvas.drawText(Text, TextXOffset, TextYOffset + timeYOffset, TextPaint);
        }
    }


    public void updateDateAndTimeColourTo(int colour) {
        dateAndTimeColour = colour;
        //      timePaint.setColor(colour);
    }

    public void updateBackgroundColourTo(int colour) {
        backgroundColour = colour;
        //    backgroundPaint.setColor(colour);
    }

    //  public void restoreBackgroundColour() {
    //    backgroundPaint.setColor(backgroundColour);
    // }

    public void updateBackgroundColourToDefault() {
        //   backgroundPaint.setColor(BACKGROUND_DEFAULT_COLOUR);
    }

    public void updateDateAndTimeColourToDefault() {
        // timePaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
        //TextPaint.setColor(DATE_AND_TIME_DEFAULT_COLOUR);
    }

    public void restoreDateAndTimeColour() {
        //timePaint.setColor(dateAndTimeColour);
        //TextPaint.setColor(dateAndTimeColour);
    }


}
