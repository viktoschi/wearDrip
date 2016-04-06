package com.eveningoutpost.dexdrip.UtilityModels;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

import com.eveningoutpost.dexdrip.Models.Calibration;
//import com.eveningoutpost.dexdrip.Models.UserError.Log;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;
import com.eveningoutpost.dexdrip.Models.BgReading;
//import com.eveningoutpost.dexdrip.Services.SyncService;
//import com.eveningoutpost.dexdrip.ShareModels.Models.ShareUploadPayload;
//import com.eveningoutpost.dexdrip.utils.BgToSpeech;
//import com.eveningoutpost.dexdrip.ShareModels.BgUploader;
//import com.eveningoutpost.dexdrip.WidgetUpdateService;
//import com.eveningoutpost.dexdrip.wearintegration.WatchUpdaterService;
//import com.eveningoutpost.dexdrip.xDripWidget;

import java.util.List;

/**
 * Created by stephenblack on 11/7/14.
 */
@Table(name = "BgSendQueue", id = BaseColumns._ID)
public class BgSendQueue extends Model {

    @Column(name = "bgReading", index = true)
    public BgReading bgReading;

    @Column(name = "success", index = true)
    public boolean success;

    @Column(name = "mongo_success", index = true)
    public boolean mongo_success;

    @Column(name = "operation_type")
    public String operation_type;

    public static List<BgSendQueue> mongoQueue(boolean xDripViewerMode) {
    	List<BgSendQueue> values = new Select()
                .from(BgSendQueue.class)
                .where("mongo_success = ?", false)
                .where("operation_type = ?", "create")
                .orderBy("_ID desc")
                .limit(xDripViewerMode ? 500 : 30)
                .execute();
    	if (xDripViewerMode) {
    		 java.util.Collections.reverse(values);
    	}
    	return values;
    	
    }

    private static void addToQueue(BgReading bgReading, String operation_type) {
        BgSendQueue bgSendQueue = new BgSendQueue();
        bgSendQueue.operation_type = operation_type;
        bgSendQueue.bgReading = bgReading;
        bgSendQueue.success = false;
        bgSendQueue.mongo_success = false;
        bgSendQueue.save();
        Log.d("BGQueue", "New value added to queue!");
    }
    
    public static void handleNewBgReading(BgReading bgReading, String operation_type, Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "sendQueue");
        wakeLock.acquire();
        try {
        	
       		addToQueue(bgReading, operation_type);

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

            Intent updateIntent = new Intent(Intents.ACTION_NEW_BG_ESTIMATE_NO_DATA);
            context.sendBroadcast(updateIntent);


        } finally {
            wakeLock.release();
        }
    }

    public void deleteThis() {
        this.delete();
    }

    public static int getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if(level == -1 || scale == -1) {
            return 50;
        }
        return (int)(((float)level / (float)scale) * 100.0f);
    }
}
