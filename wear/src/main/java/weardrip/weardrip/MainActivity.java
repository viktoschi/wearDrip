package weardrip.weardrip;

import android.content.Context;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;


public class MainActivity extends WearableActivity {
    private View myLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_main);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null && extras.getBoolean("stop", true)) {
            this.finish();
        }
        gsonstringobject();
        // Inflate the layout that we're using for the watch face
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        myLayout = inflater.inflate(R.layout.wear_drip_watchface_layout, null);


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

    public void gsonstringobject() {
        BgReading m1BgReading;
        m1BgReading = BgReading.last();
        if (m1BgReading != null) {
            String gsonstring = m1BgReading.toS();
            Log.e("gson: ", gsonstring);
        } else {
            Log.e("gson: ", "null");
        }
    }
}