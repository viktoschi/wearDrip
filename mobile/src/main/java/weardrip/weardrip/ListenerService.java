package weardrip.weardrip;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import io.realm.Realm;

public class ListenerService extends WearableListenerService {
    private static final String TAG = ListenerService.class.getSimpleName();
    public static final String WEAR_PATH = "/realm_data";
    public static final String DATA_STORY_CHANGED = "realm.storychanged";
    public static final String DATA_ASSET_FILE = "realm.asset.file";


    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        dataEvents.close();
        for (DataEvent event : events) {
            Uri uri = event.getDataItem().getUri();
            String path = uri.getPath();
            if (WEAR_PATH.equals(path)) {
                DataMapItem item = DataMapItem.fromDataItem(event.getDataItem());
                byte[] realmAsset = item.getDataMap().getByteArray(DATA_ASSET_FILE);
                if (realmAsset != null) {
                    toFile(realmAsset);
                    getBaseContext().sendBroadcast(new Intent(DATA_STORY_CHANGED));
                }
            }
        }
    }

    private void toFile(byte[] byteArray) {
        File writableFolder = ListenerService.this.getFilesDir();
        File realmFile = new File(writableFolder, Realm.DEFAULT_REALM_NAME);
        if (realmFile.exists()) {
            realmFile.delete();
        }
        try {
            FileOutputStream fos = new FileOutputStream(realmFile.getPath());
            fos.write(byteArray);
            fos.close();
        } catch (java.io.IOException e) {
            Log.d(TAG, "toFile exception: " + e.getLocalizedMessage());
        }
    }
}