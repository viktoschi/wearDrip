package weardrip.weardrip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

public class Preferences extends PreferenceActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    String CollectionMethod, txid, getAddress;
    Button sendpreferencesbutton;
    private GoogleApiClient googleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        setContentView(R.layout.preferences);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        sendpreferencesbutton = (Button) findViewById(R.id.sendpreferencesbutton);
        sendpreferencesbutton.setOnClickListener(this);
    }

    private void sendconfigButtonclick() {
        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(this);

        CollectionMethod = SP.getString("CollectionMethod", "NA");
        txid = SP.getString("txid", "NA");
        getAddress = SP.getString("getAddress", "NA");

        new AlertDialog.Builder(this)
                .setTitle("saved config:")
                .setMessage(
                        "txid: " + txid + "\n"
                                + "getAddress: " + getAddress + "\n"
                                + "CollectionMethod: " + CollectionMethod + "\n"
                )
                .setPositiveButton("Send to Wear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearable_prefernces").setUrgent();
                        putDataMapReq.getDataMap().putString("timestamp", Long.toString(System.currentTimeMillis()));
                        putDataMapReq.getDataMap().putString("txid", txid);
                        putDataMapReq.getDataMap().putString("getAddress", getAddress);
                        putDataMapReq.getDataMap().putString("getName", "xbridge");
                        putDataMapReq.getDataMap().putString("CollectionMethod", CollectionMethod);
                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sendpreferencesbutton:
                sendconfigButtonclick();
                break;
        }
    }


    // Connect to the data layer when the Activity starts
    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int cause) {
        new AlertDialog.Builder(this)
                .setTitle("Connection Suspended!")
                .setMessage("Connection to Wear Suspended")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        new AlertDialog.Builder(this)
                .setTitle("Connection Failed!")
                .setMessage("Connection to Wear Failed")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }
}
