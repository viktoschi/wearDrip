package weardrip.weardrip;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;

import com.eveningoutpost.dexdrip.Models.BgReading;


public class MainActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null && extras.getBoolean("stop", true)) {
            this.finish();
        }
        gsonstringobject();

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
}