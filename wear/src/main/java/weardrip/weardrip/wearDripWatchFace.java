package weardrip.weardrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.TextView;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.eveningoutpost.dexdrip.UtilityModels.CollectionServiceStarter;
import com.eveningoutpost.dexdrip.UtilityModels.Intents;
import com.github.johnpersano.supertoasts.SuperToast;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

import io.realm.Realm;
import io.realm.RealmObject;


public class wearDripWatchFace extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */

    @Override
    public Engine onCreateEngine() {
        CollectionServiceStarter.newStart(this);
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine implements OnChartValueSelectedListener,
            SharedPreferences.OnSharedPreferenceChangeListener {
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

        final BroadcastReceiver newDataReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                showBG();
                unitizedDeltaString();
                refreshData();
                GsonBG();
                invalidate();
            }
        };

        boolean mRegisterednewDataReceiver = false;
        boolean mRegisteredTimeZoneReceiver = false;

        boolean mAmbient;
        boolean mLowBitAmbient;

        private LineChart lineChart;
        ArrayList<String> XAxisTimeValue = new ArrayList<String>();

        String timestamplastreading = "--";
        String bgvalue = "n/a";
        String deltalastreading = "---";
        double calculated_value = 0.0;

        Time mTime;

        float mXOffset = 0;
        float mYOffset = 0;
        private int specW, specH;

        private View myLayout;
        private TextView sgv, delta, watch_time, timestamp;
        private final Point displaySize = new Point();
        public int timeframe;
        public Boolean chartcubic = false;

        protected SharedPreferences sharedPrefs;

        Realm realm;


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

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            timeframe = Integer.parseInt(sharedPrefs.getString("chart_timeframe", "12"));
            sharedPrefs.registerOnSharedPreferenceChangeListener(this);

            SetupChart();
        }

        public void SetupChart() {
            lineChart = (LineChart) myLayout.findViewById(R.id.chart);
            lineChart.setDescription("");
            lineChart.setDrawBorders(false);
            lineChart.setNoDataTextDescription("You need to provide data for the chart.");
            lineChart.setDrawGridBackground(false);
            lineChart.setOnChartValueSelectedListener(this);
            lineChart.setTouchEnabled(false);
            lineChart.setDragEnabled(false);
            lineChart.setPinchZoom(false);
            lineChart.setScaleXEnabled(true);
            lineChart.setScaleYEnabled(true);
            lineChart.invalidate();
            LineData data = new LineData();
            data.setValueTextColor(Color.WHITE);
            // add empty data
            lineChart.setData(data);
            // get the legend (only possible after setting data)
            Legend l = lineChart.getLegend();
            l.setEnabled(false);
            // x axis setup
            XAxis xl = lineChart.getXAxis();
            xl.setTextColor(Color.WHITE);
            xl.setDrawGridLines(false);
            xl.setAvoidFirstLastClipping(false);
            xl.setSpaceBetweenLabels(3);
            xl.setEnabled(true);
            xl.setDrawAxisLine(false);
            xl.removeAllLimitLines();
            //right y axis setup
            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setEnabled(false);
            //left y axis setup
            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setTextColor(Color.WHITE);
            leftAxis.setLabelCount(6, true);
            leftAxis.setAxisMaxValue(400f);
            leftAxis.setAxisMinValue(0f);
            leftAxis.setDrawGridLines(false);
            leftAxis.setStartAtZero(false);
            leftAxis.setEnabled(true);
            leftAxis.setDrawAxisLine(false);
            leftAxis.setDrawZeroLine(false);
            leftAxis.setGranularityEnabled(false);
            //define min max line
            LimitLine max = new LimitLine(150f);
            max.enableDashedLine(10f, 10f, 0f);
            LimitLine min = new LimitLine(50f);
            min.enableDashedLine(10f, 10f, 0f);
            // reset all limit lines to avoid overlapping lines
            leftAxis.removeAllLimitLines();
            //add min max line
            leftAxis.addLimitLine(max);
            leftAxis.addLimitLine(min);

            lineChart.invalidate();
        }

        private LineDataSet createSet() {
            LineDataSet set = new LineDataSet(null, "BG Data");
            set.setAxisDependency(YAxis.AxisDependency.LEFT);
            set.setColor(ColorTemplate.getHoloBlue());
            set.setCircleColor(Color.WHITE);
            set.setLineWidth(2f);
            set.setCircleRadius(3f);
            set.setFillAlpha(65);
            set.setFillColor(ColorTemplate.getHoloBlue());
            set.setHighLightColor(Color.rgb(244, 117, 117));
            set.setValueTextColor(Color.WHITE);
            set.setValueTextSize(9f);
            set.setDrawValues(false);
            //set.setColors(ColorTemplate.COLORFUL_COLORS);
            //set.setColors(ColorTemplate.VORDIPLOM_COLORS);
            //set.setColors(ColorTemplate.JOYFUL_COLORS);
            //set.setColors(ColorTemplate.LIBERTY_COLORS);
            //set.setColors(ColorTemplate.PASTEL_COLORS);
            //set.setDrawCubic(chartcubic);
            return set;
        }

        private void refreshData() {
            LineData data = lineChart.getData();
            if(data != null) {
                LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
                if (set == null) {
                    set = createSet();
                    data.addDataSet(set);
                }

                if(set.getEntryCount() == timeframe) {
                    data.removeXValue(0);
                    set.removeEntry(0);
                    for (Entry entry : set.getYVals()) {
                        entry.setXIndex(entry.getXIndex() - 1);
                    }
                }

                // add x-value
                Date date = new Date();
                Calendar calendar = GregorianCalendar.getInstance();
                calendar.setTime(date);
                int inthours = calendar.get(Calendar.HOUR_OF_DAY);
                int intminute = calendar.get(Calendar.MINUTE);
                String hourString = String.format("%02d:", inthours);
                String minuteString = String.format("%02d", intminute);
                XAxisTimeValue.add(hourString + minuteString);
                data.addXValue(XAxisTimeValue.get(data.getXValCount()));

                // choose a dataSet
                data.addEntry(new Entry((float) calculated_value, set.getEntryCount()), 0);
                lineChart.notifyDataSetChanged();

                lineChart.setVisibleYRangeMaximum((float) calculated_value, YAxis.AxisDependency.LEFT);
                // this automatically refreshes the chart (calls invalidate())
                lineChart.moveViewTo(data.getXValCount(), (float) calculated_value, YAxis.AxisDependency.LEFT);
            }
        }

        public void GsonBG() {
            realm = Realm.getInstance(getApplicationContext());
            BgReading mBgReading;
            mBgReading = BgReading.last();
            if (mBgReading != null) {
                Gson gson = new GsonBuilder()
                        .setExclusionStrategies(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                return f.getDeclaringClass().equals(RealmObject.class);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        })
                        .create();

                String json = mBgReading.toS();
                BGdata bgdata = gson.fromJson(json, BGdata.class);
                realm.beginTransaction();
                BGdata copyOfBGdata = realm.copyToRealm(bgdata);
                realm.commitTransaction();
                Log.v("realmio", String.valueOf(copyOfBGdata));

            } else {
            }
        }

        public void showBG() {
            BgReading mBgReading;
            mBgReading = BgReading.last();
            if (mBgReading != null) {
                calculated_value = mBgReading.calculated_value;
                DecimalFormat df = new DecimalFormat("#", new DecimalFormatSymbols(Locale.ENGLISH));
                bgvalue = String.valueOf(df.format(calculated_value));
            } else {
                bgvalue = "n/a";
            }
        }

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
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
            unregisterReceiver();

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
                registerReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void unregisterReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = false;
                wearDripWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
            }

            if (mRegisterednewDataReceiver) {
                mRegisterednewDataReceiver = false;
                wearDripWatchFace.this.unregisterReceiver(newDataReceiver);
            }
        }

        private void registerReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                mRegisteredTimeZoneReceiver = true;
                IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
                wearDripWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
            }

            if (!mRegisterednewDataReceiver) {
                mRegisterednewDataReceiver = true;
                IntentFilter filter = new IntentFilter(Intents.ACTION_NEW_BG_ESTIMATE_NO_DATA);
                wearDripWatchFace.this.registerReceiver(newDataReceiver, filter);
            }
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

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            // Get the current Time
            mTime.setToNow();
            delta.setText(deltalastreading);
            sgv.setText(bgvalue);
            watch_time.setText(String.format("%02d:%02d", mTime.hour, mTime.minute));
            getTimestampLastreading();
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
            Log.i("Entry selected", e.toString());
            Log.i("LOWHIGH", "low: " + lineChart.getLowestVisibleXIndex() + ", high: " + lineChart.getHighestVisibleXIndex());
            Log.i("MIN MAX", "xmin: " + lineChart.getXChartMin() + ", xmax: " + lineChart.getXChartMax() + ", ymin: " + lineChart.getYChartMin() + ", ymax: " + lineChart.getYChartMax());
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
                    SuperToast.create(getApplicationContext(), "Hello world!", SuperToast.Duration.LONG).show();
                    break;
                case WatchFaceService.TAP_TYPE_TOUCH_CANCEL:
                    break;
                default:
                    super.onTapCommand(tapType, x, y, eventTime);
                    break;
            }
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            timeframe = Integer.parseInt(sharedPrefs.getString("chart_timeframe", "12"));
            chartcubic = sharedPrefs.getBoolean("chart_cubic", true);
            invalidate();
            lineChart.invalidate();

        }
    }
}