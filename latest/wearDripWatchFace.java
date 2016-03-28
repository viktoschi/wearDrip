package weardrip.weardrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;



public class wearDripWatchFace extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;

        /**
         * Handler to update the time periodically in interactive mode.
         */
        final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long timeMs = System.currentTimeMillis();
                            long delayMs = INTERACTIVE_UPDATE_RATE_MS
                                    - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };

        boolean mRegisteredTimeZoneReceiver = false;

        boolean mAmbient;

        Time mTime;

        float mXOffset = 0;
        float mYOffset = 0;

        private int specW, specH;
        private View myLayout;
        private TextView day, month, hour, minute, second, sgv, delta, watch_time, timestamp;
        private final Point displaySize = new Point();

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(wearDripWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());
            Resources resources = wearDripWatchFace.this.getResources();

            mTime = new Time();

            // Inflate the layout that we're using for the watch face
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myLayout = inflater.inflate(R.layout.wear_drip_watchface_layout, null);

            // Load the display spec - we'll need this later for measuring myLayout
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            display.getSize(displaySize);

            // Find some views for later use
            sgv = (TextView) myLayout.findViewById(R.id.sgv);
            delta = (TextView) myLayout.findViewById(R.id.delta);
            timestamp = (TextView) myLayout.findViewById(R.id.timestamp);
            watch_time = (TextView) myLayout.findViewById(R.id.watch_time);


            LineChart lineChart = (LineChart) myLayout.findViewById(R.id.chart);
            lineChart.setDescription("");
            lineChart.setNoDataTextDescription("You need to provide data for the chart.");

            ArrayList<Entry> entries = new ArrayList<>();
            entries.add(new Entry(4f, 0));
            entries.add(new Entry(8f, 1));
            entries.add(new Entry(6f, 2));
            entries.add(new Entry(2f, 3));
            entries.add(new Entry(18f, 4));
            entries.add(new Entry(9f, 5));

            LineDataSet dataset = new LineDataSet(entries, "# of Calls");

            ArrayList<String> labels = new ArrayList<String>();
            labels.add("January");
            labels.add("February");
            labels.add("March");
            labels.add("April");
            labels.add("May");
            labels.add("June");

            LineData data = new LineData(labels, dataset);
            //dataset.setColors(ColorTemplate.COLORFUL_COLORS);
            //dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);
            //dataset.setColors(ColorTemplate.JOYFUL_COLORS);
            dataset.setColors(ColorTemplate.LIBERTY_COLORS);
            //dataset.setColors(ColorTemplate.PASTEL_COLORS);
            dataset.setDrawCubic(true);
            dataset.setDrawFilled(true);
            dataset.setDrawCircles(false);
            dataset.setDrawValues(false);

            lineChart.setPinchZoom(false);
            lineChart.setDragEnabled(false);
            lineChart.setScaleEnabled(false);
            lineChart.setDrawGridBackground(false);
            lineChart.setTouchEnabled(false);
            lineChart.setData(data);
            lineChart.animateY(5000);

            // get the legend (only possible after setting data)
            Legend l = lineChart.getLegend();
            l.setEnabled(false);

            XAxis xl = lineChart.getXAxis();
            xl.setDrawGridLines(true);
            xl.setEnabled(false);

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setDrawGridLines(false);

            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setEnabled(false);

        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            wearDripWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            wearDripWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            if (insets.isRound()) {
                mXOffset = mYOffset = 0;
            } else {
                mXOffset = mYOffset = 0;
            }

            // Recompute the MeasureSpec fields - these determine the actual size of the layout
            specW = View.MeasureSpec.makeMeasureSpec(displaySize.x, View.MeasureSpec.EXACTLY);
            specH = View.MeasureSpec.makeMeasureSpec(displaySize.y, View.MeasureSpec.EXACTLY);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;

                // Show/hide the seconds fields
                if (inAmbientMode) {
                    //second.setVisibility(View.GONE);
                    //myLayout.findViewById(R.id.chart).setVisibility(View.GONE);
                } else {
                    //second.setVisibility(View.VISIBLE);
                    //myLayout.findViewById(R.id.chart).setVisibility(View.VISIBLE);
                }

                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        String bgvalue;
        public void showBG() {
            BgReading mBgReading;
            mBgReading = BgReading.last();
            if(mBgReading != null) {
                double calculated_value = mBgReading.calculated_value;
                DecimalFormat df = new DecimalFormat("#.");
                bgvalue = String.valueOf(df.format(calculated_value));
            } else {
                bgvalue = "n/a";
            }
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Get the current Time
            mTime.setToNow();
            showBG();
            delta.setText("mg/dl");
            sgv.setText(bgvalue);
            watch_time.setText(String.format("%02d:%02d", mTime.hour, mTime.minute));
            timestamp.setText("--'");

            if (!mAmbient) {
                //second.setText(String.format("%02d", mTime.second));
            }

            // Update the layout
            myLayout.measure(specW, specH);
            myLayout.layout(0, 0, myLayout.getMeasuredWidth(), myLayout.getMeasuredHeight());

            // Draw it to the Canvas
            canvas.drawColor(Color.BLACK);
            canvas.translate(mXOffset, mYOffset);
            myLayout.draw(canvas);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }
}
