package com.eveningoutpost.dexdrip.UtilityModels;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import com.eveningoutpost.dexdrip.Services.DexCollectionService;

import java.io.IOException;

//import com.eveningoutpost.dexdrip.Models.UserError.Log;
//import com.eveningoutpost.dexdrip.Services.DailyIntentService;
//import com.eveningoutpost.dexdrip.Services.SyncService;

/**
 * Created by stephenblack on 12/22/14.
 */


public class CollectionServiceStarter {


    private final static String TAG = CollectionServiceStarter.class.getSimpleName();
    private Context mContext;


    public CollectionServiceStarter(Context context) {
        mContext = context;
    }

    public static boolean isBTWixel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String collection_method = prefs.getString("dex_collection_method", "BluetoothWixel");
        return collection_method.compareTo("BluetoothWixel") == 0;
    }

    public static boolean isBTWixel(String collection_method) {
        return collection_method.equals("BluetoothWixel");
    }

    public static boolean isDexbridgeWixel(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String collection_method = prefs.getString("dex_collection_method", "BluetoothWixel");
        return collection_method.compareTo("DexbridgeWixel") == 0;
    }

    public static boolean isDexbridgeWixel(String collection_method) {
        return collection_method.equals("DexbridgeWixel");
    }

    public static void newStart(Context context) {
        CollectionServiceStarter collectionServiceStarter = new CollectionServiceStarter(context);
        collectionServiceStarter.start(context);
    }

    public static void restartCollectionService(Context context) {
        CollectionServiceStarter collectionServiceStarter = new CollectionServiceStarter(context);
        collectionServiceStarter.stopBtWixelService();
        collectionServiceStarter.start(context);
    }

    public static void restartCollectionService(Context context, String collection_method) {
        CollectionServiceStarter collectionServiceStarter = new CollectionServiceStarter(context);
        collectionServiceStarter.stopBtWixelService();
        collectionServiceStarter.start(context, collection_method);
    }

    public void start(Context context, String collection_method) {
        mContext = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        if (isBTWixel(collection_method) || isDexbridgeWixel(collection_method)) {
            Log.d("DexDrip", "Starting bt wixel collector");
            startBtWixelService();
        }


        Log.d(TAG, collection_method);

        // Start logging to logcat
        if (prefs.getBoolean("store_logs", false)) {
            String filePath = Environment.getExternalStorageDirectory() + "/xdriplogcat.txt";
            try {
                String[] cmd = {"/system/bin/sh", "-c", "ps | grep logcat  || logcat -f " + filePath +
                        " -v threadtime AlertPlayer:V com.eveningoutpost.dexdrip.Services.WixelReader:V *:E "};
                Runtime.getRuntime().exec(cmd);
            } catch (IOException e2) {
                Log.e(TAG, "running logcat failed, is the device rooted?", e2);
            }
        }

    }

    public void start(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String collection_method = prefs.getString("dex_collection_method", "BluetoothWixel");

        start(context, collection_method);
    }

    private void startBtWixelService() {
        Log.d(TAG, "starting bt wixel service");
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mContext.startService(new Intent(mContext, DexCollectionService.class));
        }
    }

    private void stopBtWixelService() {
        Log.d(TAG, "stopping bt wixel service");
        mContext.stopService(new Intent(mContext, DexCollectionService.class));
    }


    /*
    private void startDailyIntentService() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 4);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        PendingIntent pi = PendingIntent.getService(mContext, 0, new Intent(mContext, DailyIntentService.class),PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
    }
*/
}