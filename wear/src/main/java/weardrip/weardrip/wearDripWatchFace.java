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
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.eveningoutpost.dexdrip.UtilityModels.CollectionServiceStarter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
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
        CollectionServiceStarter.newStart(this);
        return new Engine();
    }
    boolean mLowBitAmbient;

    private LineChart lineChart;


    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.RIGHT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(false);
        return set;
    }
    ArrayList<String> XAxisTimeValue = new ArrayList<String>();

    public void addEntry() {
        LineData data = lineChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);
            // set.addEntry(...); // can be called as well

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            // add a new x-value first
            Date date = new Date();   // given date
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date

            int inthours = calendar.get(Calendar.HOUR_OF_DAY); // gets hour in 24h format
            int intminute = calendar.get(Calendar.MINUTE);
            String hourString = String.format("%02d:", inthours);
            String minuteString = String.format("%02d", intminute);
            XAxisTimeValue.add(hourString+minuteString);
            data.addXValue(XAxisTimeValue.get(data.getXValCount()));


            BgReading mBgReading;
            mBgReading = BgReading.last();
            float bgvalue = 0;
            if (mBgReading != null) {
                double calculated_value = mBgReading.calculated_value;
                bgvalue = (float) calculated_value;
            } else {
                bgvalue = 0;
            }

            data.addEntry(new Entry(bgvalue , set.getEntryCount()), 0);

            // let the chart know it's data has changed
            lineChart.notifyDataSetChanged();

            // limit the number of visible entries
            lineChart.setVisibleXRangeMaximum(400);
            // mChart.setVisibleYRange(30, AxisDependency.LEFT);

            // move to the latest entry
            lineChart.moveViewToX(data.getXValCount() - 121);

            // this automatically refreshes the chart (calls invalidate())
            // mChart.moveViewTo(data.getXValCount()-7, 55f,
            // AxisDependency.LEFT);
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine implements OnChartValueSelectedListener {
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
        private TextView sgv, delta, watch_time, timestamp;
        private final Point displaySize = new Point();

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(wearDripWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
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


            lineChart = (LineChart) myLayout.findViewById(R.id.chart);
            lineChart.setDescription("");
            lineChart.setNoDataTextDescription("You need to provide data for the chart.");
            lineChart.setDrawGridBackground(false);
            //lineChart.setBackgroundColor(-1);
            lineChart.invalidate();

            lineChart.setOnChartValueSelectedListener(this);

            // enable touch gestures
            lineChart.setTouchEnabled(false);

            // enable scaling and dragging
            lineChart.setDragEnabled(false);
            lineChart.setScaleEnabled(false);
            lineChart.setDrawGridBackground(false);

            // if disabled, scaling can be done on x- and y-axis separately
            lineChart.setPinchZoom(false);

            LineData data = new LineData();
            data.setValueTextColor(Color.WHITE);

            // add empty data
            lineChart.setData(data);


            // get the legend (only possible after setting data)
            Legend l = lineChart.getLegend();

            // modify the legend ...
            // l.setPosition(LegendPosition.LEFT_OF_CHART);
            l.setForm(Legend.LegendForm.LINE);
            l.setTextColor(Color.WHITE);

            XAxis xl = lineChart.getXAxis();
            xl.setTextColor(Color.WHITE);
            xl.setDrawGridLines(true);
            xl.setAvoidFirstLastClipping(true);
            xl.setSpaceBetweenLabels(5);
            xl.setEnabled(true);

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setTextColor(Color.WHITE);
            leftAxis.setAxisMaxValue(100f);
            leftAxis.setAxisMinValue(0f);
            leftAxis.setDrawGridLines(true);

            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setEnabled(true);

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
            if (mBgReading != null) {
                double calculated_value = mBgReading.calculated_value;
                DecimalFormat df = new DecimalFormat("#", new DecimalFormatSymbols(Locale.ENGLISH));
                bgvalue = String.valueOf(df.format(calculated_value));
            } else {
                bgvalue = "n/a";
            }
        }


        String timestamplastreading;
        public void getTimestampLastreading() {
            Long mTimeStampLastreading;
            mTimeStampLastreading = BgReading.getTimeSinceLastReading();
            if (mTimeStampLastreading != null) {
                long minutesago=((mTimeStampLastreading)/1000)/60;
                timestamplastreading = String.valueOf(minutesago);
            } else {
                timestamplastreading = "--'";
            }
        }

        String deltalastreading;
        public void unitizedDeltaString() {
            List<BgReading> last2 = BgReading.latest(2);
            if (BgReading.latest(2) != null) {
                if (last2.size() < 2 || last2.get(0).timestamp - last2.get(1).timestamp > 20 * 60 * 1000) {
                    // don't show delta if there are not enough values or the values are more than 20 mintes apart
                    deltalastreading = "???";
                }

                double value = BgReading.currentSlope() * 5 * 60 * 1000;

                if (Math.abs(value) > 100) {
                    // a delta > 100 will not happen with real BG values -> problematic sensor data
                    deltalastreading = "ERR";
                }

                DecimalFormat df = new DecimalFormat("#", new DecimalFormatSymbols(Locale.ENGLISH));
                String delta_sign = "";
                if (value > 0) {
                    delta_sign = "+";
                }
                deltalastreading = delta_sign + df.format(value);
            } else {
                deltalastreading = "---";
            }
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Get the current Time
            mTime.setToNow();
            showBG();
            getTimestampLastreading();
            unitizedDeltaString();
            delta.setText(deltalastreading);
            sgv.setText(bgvalue);
            watch_time.setText(String.format("%02d:%02d", mTime.hour, mTime.minute));
            timestamp.setText(timestamplastreading + "′");

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

        @Override
        public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

        }

        @Override
        public void onNothingSelected() {

        }

        @Override
        public void onTapCommand(
                @TapType int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case WatchFaceService.TAP_TYPE_TAP:
                    break;
                case WatchFaceService.TAP_TYPE_TOUCH:
                    addEntry();
                    break;
                case WatchFaceService.TAP_TYPE_TOUCH_CANCEL:
                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }
        }
    }
}
