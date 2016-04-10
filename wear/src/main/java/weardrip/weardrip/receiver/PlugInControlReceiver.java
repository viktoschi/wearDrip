package weardrip.weardrip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PlugInControlReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (action.equals(Intent.ACTION_POWER_CONNECTED)) {
        } else if (action.equals(Intent.ACTION_POWER_DISCONNECTED)) {
        }
    }
}