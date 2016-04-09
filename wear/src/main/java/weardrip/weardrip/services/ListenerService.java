package weardrip.weardrip.services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.activeandroid.query.Select;
import com.eveningoutpost.dexdrip.ImportedLibraries.dexcom.SyncingService;
import com.eveningoutpost.dexdrip.Models.ActiveBluetoothDevice;
import com.eveningoutpost.dexdrip.Models.BgReading;
import com.eveningoutpost.dexdrip.Models.Calibration;
import com.eveningoutpost.dexdrip.Models.Sensor;
import com.eveningoutpost.dexdrip.UtilityModels.CollectionServiceStarter;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.Calendar;
import java.util.List;

public class ListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "ListenerService";

    private static final String WEARABLE_STOPSENSOR = "/wearable_stopsensor";
    private static final String WEARABLE_STARTSENSOR = "/wearable_startsensor";
    private static final String WEARABLE_PREFERNCES = "/wearable_prefernces";
    private static final String WEARABLE_STOPCOLLECTIONSERVICE = "/wearable_stopcollectionservice";
    private static final String WEARABLE_STARTCOLLECTIONSERVICE = "/wearable_startcollectionservice";
    private static final String WEARABLE_CALIBRATION = "/wearable_calibration";
    private static final String WEARABLE_DOUBLECALIBRATION = "/wearable_doublecalibration";

    private Context mContext;

    GoogleApiClient googleApiClient;

    public class DataRequester extends AsyncTask<Void, Void, Void> {

        DataRequester(Context context) {
            mContext = context;

        }

        @Override
        protected Void doInBackground(Void... params) {
            if (googleApiClient.isConnected()) {

            } else
                googleApiClient.connect();
            return null;
        }
    }

    public void requestData() {
        new DataRequester(this).execute();
    }

    public void googleApiConnect() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
        Wearable.MessageApi.addListener(googleApiClient, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            googleApiConnect();
            requestData();
        }
        return START_STICKY;
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {


        for (DataEvent event : dataEvents) {
            DataMap dataMap;

            if (event.getType() == DataEvent.TYPE_CHANGED) {

                String path = event.getDataItem().getUri().getPath();
                if (path.equals(WEARABLE_STOPSENSOR)) {
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    if (dataMap.containsKey("StopSensor")) {
                        SuperToast.create(getApplicationContext(), "sensor stopped!", SuperToast.Duration.LONG).show();
                    } else {
                        SuperToast.create(getApplicationContext(),
                                "Sensor not active please start new sensor.",
                                SuperToast.Duration.LONG).show();
                    }
                }

                if (path.equals(WEARABLE_STARTSENSOR)) {
                    if (Sensor.isActive()) {
                        SuperToast.create(getApplicationContext(),
                                "sensor is active: " + Sensor.isActive(),
                                SuperToast.Duration.LONG).show();
                    } else {
                        dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                        int year = dataMap.getInt("year");
                        int month = dataMap.getInt("month");
                        int day = dataMap.getInt("day");
                        int hour = dataMap.getInt("hour");
                        int minute = dataMap.getInt("minute");
                        //new calendar
                        Calendar calendar = Calendar.getInstance();
                        calendar.set(day, month, year, hour, minute, 0);
                        long startTime = calendar.getTime().getTime();
                        //init sensor start
                        if (dataMap.containsKey("StartSensor")) {
                            Sensor.create(startTime);
                            SuperToast.create(getApplicationContext(),
                                    "New Sensor started at: " + startTime,
                                    SuperToast.Duration.LONG).show();
                        } else {
                            SuperToast.create(getApplicationContext(),
                                    "Sensor still active please stop current sensor: " + startTime,
                                    SuperToast.Duration.LONG).show();
                        }
                    }
                }

                if (path.equals((WEARABLE_PREFERNCES))){
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    prefs.edit().putString("dex_txid", dataMap.getString("txid")).apply();
                    prefs.edit().putString("getAddress", dataMap.getString("getAddress")).apply();
                    prefs.edit().putString("getName", dataMap.getString("getName")).apply();
                    prefs.edit().putString("CollectionMethod", dataMap.getString("CollectionMethod")).apply();
                    prefs.edit().putString("dex_collection_method", dataMap.getString("CollectionMethod")).apply();
                    String getAddress = prefs.getString("getAddress", "00:00:00:00:00:00");
                    String getName = prefs.getString("getName", "");
                    String dex_txid = prefs.getString("dex_txid", "");
                    String CollectionMethod = prefs.getString("CollectionMethod", "");
                    SuperToast.create(getApplicationContext(),
                            "Prefernces recieved: " + " getAdress: " + getAddress
                                    + " getName: " + getName
                                    + " dex_txid: " + dex_txid
                                    + " CollectionMethod: " + CollectionMethod,
                            SuperToast.Duration.LONG).show();
                }

                if (path.equals((WEARABLE_STOPCOLLECTIONSERVICE))){
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    if (dataMap.containsKey("StopCollectionService")) {
                        SuperToast.create(getApplicationContext(),
                                "ActiveBluetoothDevice forget!",
                                SuperToast.Duration.LONG).show();
                        BluetoothManager mBluetoothManager;
                        mBluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
                        final BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
                        ActiveBluetoothDevice.forget();
                        bluetoothAdapter.disable();
                        Handler mHandler = new Handler();
                        final Handler mHandler2 = new Handler();
                        mHandler.postDelayed(new Runnable() {
                            public void run() {
                                bluetoothAdapter.enable();
                                mHandler2.postDelayed(new Runnable() {
                                    public void run() {
                                        CollectionServiceStarter.restartCollectionService(mContext);
                                    }
                                }, 5000);
                            }
                        }, 1000);
                    }
                }

                if (path.equals((WEARABLE_STARTCOLLECTIONSERVICE))){
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    if (dataMap.containsKey("StartCollectionService")) {
                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                        String getAddress = prefs.getString("getAddress", "00:00:00:00:00:00");
                        String getName = prefs.getString("getName", "");

                        ActiveBluetoothDevice btDevice = new Select().from(ActiveBluetoothDevice.class)
                                .orderBy("_ID desc")
                                .executeSingle();
                        if (btDevice == null) {
                            ActiveBluetoothDevice newBtDevice = new ActiveBluetoothDevice();
                            newBtDevice.name = getName;
                            newBtDevice.address = getAddress;
                            newBtDevice.save();
                        } else {
                            btDevice.name = getName;
                            btDevice.address = getAddress;
                            btDevice.save();
                        }
                        Context context = this;
                        CollectionServiceStarter restartCollectionService = new CollectionServiceStarter(context);
                        restartCollectionService.restartCollectionService(this);
                        SuperToast.create(getApplicationContext(),
                                "DexCollectionService started",
                                SuperToast.Duration.LONG).show();
                    }
                }

                if (path.equals((WEARABLE_CALIBRATION))){
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    if (dataMap.containsKey("startcalibration")) {
                        double calValue = Double.parseDouble(dataMap.getString("calibration", ""));
                        Calibration.create(calValue, this);
                        Log.d("NEW CALIBRATION", "Calibration value: " + calValue);
                        if (Sensor.isActive()) {
                            SyncingService.startActionCalibrationCheckin(this);
                            SuperToast.create(getApplicationContext(),
                                    "Calibration value: " + calValue,
                                    SuperToast.Duration.LONG).show();
                        } else {
                            SuperToast.create(getApplicationContext(),
                                    "CALIBRATION ERROR, sensor not active",
                                    SuperToast.Duration.LONG).show();
                        }
                    }
                }

                if (path.equals((WEARABLE_DOUBLECALIBRATION))){
                    dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                    if (dataMap.containsKey("startdoublecalibration")) {
                        if (BgReading.latestUnCalculated(2).size() < 2) {
                            SuperToast.create(getApplicationContext(),
                                    "Please wait, need 2 readings from transmitter first.",
                                    SuperToast.Duration.LONG).show();
                        } else {
                            List<Calibration> calibrations = Calibration.latest(2);
                            if (calibrations.size() < 2) {
                                double calValue1 = Double.parseDouble(dataMap.getString("doublecalibration1", ""));
                                double calValue2 = Double.parseDouble(dataMap.getString("doublecalibration2", ""));
                                Calibration.initialCalibration(calValue1, calValue2, this);
                                SuperToast.create(getApplicationContext(),
                                        "Double Calibration values: " + calValue1 + " " + calValue2,
                                        SuperToast.Duration.LONG).show();
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        requestData();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "suspended GoogleAPI");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "connectionFailed GoogleAPI");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
        if (googleApiClient != null) {
            Wearable.MessageApi.removeListener(googleApiClient, this);
        }
    }
}
