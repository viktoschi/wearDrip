package weardrip.weardrip;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;

public class MainActivity extends WearableActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bundle extras = this.getIntent().getExtras();
        if (extras != null && extras.getBoolean("stop", true)) {
            this.finish();
        }
    }
}