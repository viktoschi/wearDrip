package weardrip.weardrip;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowManager;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class wearBIGChart extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        static final int MSG_UPDATE_TIME = 0;

        private View myLayout;
        private final Point displaySize = new Point();


        float mXOffset = 0;
        float mYOffset = 0;

        private int specW, specH;



        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            // Inflate the layout that we're using for the watch face
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            myLayout = inflater.inflate(R.layout.activity_bigchart, null);

            // Load the display spec - we'll need this later for measuring myLayout
            Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            display.getSize(displaySize);


            LineChart lineChart = (LineChart) myLayout.findViewById(R.id.chart);
            lineChart.setPinchZoom(false);
            lineChart.setDragEnabled(false);
            lineChart.setScaleEnabled(false);
            lineChart.setDrawGridBackground(false);
            lineChart.setTouchEnabled(false);
            lineChart.animateY(5000);


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
            lineChart.setData(data);


            //dataset.setColors(ColorTemplate.COLORFUL_COLORS);
            //dataset.setColors(ColorTemplate.VORDIPLOM_COLORS);
            //dataset.setColors(ColorTemplate.JOYFUL_COLORS);
            dataset.setColors(ColorTemplate.LIBERTY_COLORS);
            //dataset.setColors(ColorTemplate.PASTEL_COLORS);
            dataset.setDrawCubic(true);
            dataset.setDrawFilled(true);
            dataset.setDrawCircles(false);
            dataset.setDrawValues(false);


            // get the legend (only possible after setting data)
            Legend l = lineChart.getLegend();
            l.setEnabled(false);

            XAxis xl = lineChart.getXAxis();
            xl.setDrawGridLines(true);
            xl.setEnabled(true);

            YAxis leftAxis = lineChart.getAxisLeft();
            leftAxis.setDrawGridLines(false);

            YAxis rightAxis = lineChart.getAxisRight();
            rightAxis.setEnabled(false);

        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            if (insets.isRound()) {
                // Shrink the face to fit on a round screen
                mYOffset = mXOffset = displaySize.x * 0.1f;
                displaySize.y -= 2 * mXOffset;
                displaySize.x -= 2 * mXOffset;
            } else {
                mXOffset = mYOffset = 0;
            }

            // Recompute the MeasureSpec fields - these determine the actual size of the layout
            specW = View.MeasureSpec.makeMeasureSpec(displaySize.x, View.MeasureSpec.EXACTLY);
            specH = View.MeasureSpec.makeMeasureSpec(displaySize.y, View.MeasureSpec.EXACTLY);
        }


        @Override
        public void onDraw(Canvas canvas, Rect bounds) {



            // Update the layout
            myLayout.measure(specW, specH);
            myLayout.layout(0, 0, myLayout.getMeasuredWidth(), myLayout.getMeasuredHeight());

            // Draw it to the Canvas
            canvas.drawColor(Color.BLACK);
            canvas.translate(mXOffset, mYOffset);
            myLayout.draw(canvas);
        }

    }

}
