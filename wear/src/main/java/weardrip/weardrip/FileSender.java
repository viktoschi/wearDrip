package weardrip.weardrip;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.Calendar;

import io.realm.Realm;

public class FileSender extends AsyncTask<Void, Void, Void> {
    private static final String TAG = "AssetsSender";
    private Asset asset;
    private Context context;
    private GoogleApiClient mGoogleAppiClient;


    public FileSender(Asset asset, Context context) {
        this.asset = asset;
        this.context = context;
    }

    public static void syncRealm(Context context) {
        File writableFolder = context.getFilesDir();
        File realmFile = new File(writableFolder, Realm.DEFAULT_REALM_NAME);
        Asset realAsset = Tools.assetFromFile(realmFile);
        new FileSender(realAsset, context).execute();
    }

    @Override
    protected Void doInBackground(Void... params) {
        sendData(asset);
        return null;
    }

    @Override
    protected void onPreExecute() {
        mGoogleAppiClient = new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
        mGoogleAppiClient.connect();
    }

    private void sendData(Asset asset) {
        if (asset == null) {
            return;
        }
        PutDataMapRequest dataMap = PutDataMapRequest.create(Tools.WEAR_PATH);
        byte[] arr = asset.getData();
        dataMap.getDataMap().putByteArray(Tools.DATA_ASSET_FILE, arr);
        dataMap.getDataMap().putLong("timestamp", Calendar.getInstance().getTimeInMillis());
        PutDataRequest request = dataMap.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleAppiClient, request);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                Log.d(TAG, "onResult result:" + dataItemResult.getStatus());
            }
        });
    }
}