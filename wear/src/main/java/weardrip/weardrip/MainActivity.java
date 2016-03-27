package weardrip.weardrip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import com.eveningoutpost.dexdrip.Models.BgReading;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class MainActivity extends WearableActivity implements OnChartValueSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null && extras.getBoolean("stop", true)) {
            this.finish();
        }
        gsonstringobject();
        LineChart lineChart = (LineChart) findViewById(R.id.chart2);


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
        xl.setEnabled(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);

    }



    public void gsonstringobject(){
        BgReading m1BgReading;
        m1BgReading = BgReading.last();
        if(m1BgReading != null) {
            String gsonstring = m1BgReading.toS();
            Log.e("gson: ", gsonstring);
        } else {
            Log.e("gson: ", "null");
        }
    }


    @Override
    public void onValueSelected(Entry e, int dataSetIndex, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}