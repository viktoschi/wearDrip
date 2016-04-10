package weardrip.weardrip;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.dd.realmbrowser.RealmBrowser;
import android.widget.TextView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MainActivity extends FragmentActivity implements View.OnClickListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        DatePickerDialog.OnDateSetListener,
        TimePickerDialog.OnTimeSetListener{
    Button startsensor, stopsensor, stopcollectionservice, startcollectionservice;
    EditText calibration, doublecalibration, intercept, slope;
    int year, month ,day ,hour ,minute;
    private GoogleApiClient googleApiClient;
    public static final String REALM_FILE_NAME = "db10";
    private TextView mTxtTitle;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startsensor = (Button) findViewById(R.id.startsensor);
        startsensor.setOnClickListener(this);

        stopsensor = (Button) findViewById(R.id.stopsensor);
        stopsensor.setOnClickListener(this);

        startcollectionservice = (Button) findViewById(R.id.startcollectionservice);
        startcollectionservice.setOnClickListener(this);

        stopcollectionservice = (Button) findViewById(R.id.stopcollectionservice);
        stopcollectionservice.setOnClickListener(this);

        calibration = (EditText) findViewById(R.id.calibration);
        calibration.setInputType(InputType.TYPE_CLASS_NUMBER);
        calibration.setOnClickListener(this);

        doublecalibration = (EditText) findViewById(R.id.doublecalibration);
        doublecalibration.setInputType(InputType.TYPE_CLASS_NUMBER);
        doublecalibration.setOnClickListener(this);

        slope = (EditText) findViewById(R.id.slope);
        slope.setInputType(InputType.TYPE_CLASS_NUMBER);
        slope.setOnClickListener(this);

        intercept = (EditText) findViewById(R.id.intercept);
        intercept.setInputType(InputType.TYPE_CLASS_NUMBER);
        intercept.setOnClickListener(this);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        RealmBrowser.getInstance().addRealmModel(BGdata.class);

        mTxtTitle = (TextView) findViewById(R.id.txtTitle);
        findViewById(R.id.btnOpenFile).setOnClickListener(this);
        findViewById(R.id.btnOpenModel).setOnClickListener(this);

        updateTitle();

        RealmBrowser.showRealmFilesNotification(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.startsensor:
                sensorstartonClick();
                break;
            case R.id.stopsensor:
                sensorstoponClick();
                break;
            case R.id.startcollectionservice:
                startcollectionserviceonClick();
                break;
            case R.id.stopcollectionservice:
                stopcollectionserviceonClick();
                break;
            case R.id.calibration:
                calibrationonClick();
                break;
            case R.id.doublecalibration:
                doublecalibrationonClick();
                break;
            case R.id.slope:
                slopeonClick();
                break;
            case R.id.intercept:
                interceptonClick();
                break;
            case R.id.btnOpenFile:
                startRealmFilesActivity();
                break;
            case R.id.btnOpenModel:
                startRealmModelsActivity();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent i = new Intent(MainActivity.this, Preferences.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateTitle() {
        RealmConfiguration config = new RealmConfiguration.Builder(this)
                .name(REALM_FILE_NAME)
                .build();
        Realm realm = Realm.getInstance(config);
        int size = realm.allObjects(BGdata.class).size();
        mTxtTitle.setText(String.format("Items in database: %d", size));
        realm.close();
    }

    private void startRealmFilesActivity() {
        RealmBrowser.startRealmFilesActivity(this);
    }

    private void startRealmModelsActivity() {
        RealmBrowser.startRealmModelsActivity(this, REALM_FILE_NAME);
    }


    public void calibrationonClick(){

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.calibration_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editcalibration);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send to wear",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                calibration.setText(userInput.getText());
                                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearable_calibration").setUrgent();
                                putDataMapReq.getDataMap().putString("timestamp", Long.toString(System.currentTimeMillis()));
                                putDataMapReq.getDataMap().putString("startcalibration", Long.toString(System.currentTimeMillis()));
                                putDataMapReq.getDataMap().putString("calibration", userInput.getText().toString());
                                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public void doublecalibrationonClick(){

        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.doublecalibration_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput1 = (EditText) promptsView.findViewById(R.id.editdoublecalibration1);
        final EditText userInput2 = (EditText) promptsView.findViewById(R.id.editdoublecalibration2);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send to wear",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                String val1 = userInput1.getText().toString();
                                String val2 = userInput2.getText().toString();
                                String text = val1 + " " + val2;
                                doublecalibration.setText(String.valueOf(text));
                                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearable_doublecalibration").setUrgent();
                                putDataMapReq.getDataMap().putString("timestamp", Long.toString(System.currentTimeMillis()));
                                putDataMapReq.getDataMap().putString("startdoublecalibration", Long.toString(System.currentTimeMillis()));
                                putDataMapReq.getDataMap().putString("doublecalibration1", userInput1.getText().toString());
                                putDataMapReq.getDataMap().putString("doublecalibration2", userInput2.getText().toString());
                                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                                Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public void slopeonClick(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.slope_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editslope);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send to wear",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                slope.setText(userInput.getText());
                                //googleClient.connect();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public void interceptonClick(){
        // get prompts.xml view
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.intercept_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(promptsView);
        final EditText userInput = (EditText) promptsView.findViewById(R.id.editintercept);
        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("Send to wear",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                intercept.setText(userInput.getText());
                                //googleClient.connect();
                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    public void sensorstartonClick(){
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog datePickerDialog = DatePickerDialog.newInstance((DatePickerDialog.OnDateSetListener) this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        final TimePickerDialog timePickerDialog = TimePickerDialog.newInstance((TimePickerDialog.OnTimeSetListener) this, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false, false);
        timePickerDialog.show(getSupportFragmentManager(), "timepicker");
        datePickerDialog.setYearRange(2016, 2028);
        datePickerDialog.show(getSupportFragmentManager(), "datepicker");

        /*
        Notification notification = new NotificationCompat.Builder(getApplication())
                .setSmallIcon(R.drawable.common_google_signin_btn_icon_dark)
                .setContentTitle("Hello World")
                .setContentText("My first Android Wear notification")
                .extend(
                        new NotificationCompat.WearableExtender().setHintShowBackgroundOnly(false))
                .build();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplication());
        int notificationId = 1;
        notificationManager.notify(notificationId, notification);
        */
    }
    public void sensorstoponClick(){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearable_stopsensor").setUrgent();
        putDataMapReq.getDataMap().putString("timestamp", Long.toString(System.currentTimeMillis()));
        putDataMapReq.getDataMap().putString("StopSensor", Long.toString(System.currentTimeMillis()));
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
    }

    @Override
    public void onDateSet(DatePickerDialog datePickerDialog, int intyear, int intmonth, int intday) {
        year=intyear;
        month=intmonth+1;
        day=intday;
    }

    @Override
    public void onTimeSet(RadialPickerLayout view, int inthour, int intminute) {
        hour = inthour;
        minute = inthour;
        sendensordata();
    }

    public void sendensordata(){
        new AlertDialog.Builder(this)
                .setTitle("Sensor Date:")
                .setMessage("Sensor Started at: " + day + "." + month + "." + year + "  " + hour + ":" + minute)
                .setPositiveButton("Send to Wear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearable_startsensor").setUrgent();
                        putDataMapReq.getDataMap().putString("timestamp", Long.toString(System.currentTimeMillis()));
                        putDataMapReq.getDataMap().putString("StartSensor", Long.toString(System.currentTimeMillis()));
                        putDataMapReq.getDataMap().putInt("day", day);
                        putDataMapReq.getDataMap().putInt("year", year);
                        putDataMapReq.getDataMap().putInt("month", month);
                        putDataMapReq.getDataMap().putInt("hour", hour);
                        putDataMapReq.getDataMap().putInt("minute", minute);
                        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }


    public void startcollectionserviceonClick(){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearable_startcollectionservice").setUrgent();
        putDataMapReq.getDataMap().putString("timestamp", Long.toString(System.currentTimeMillis()));
        putDataMapReq.getDataMap().putString("StartCollectionService", Long.toString(System.currentTimeMillis()));
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
    }

    public void stopcollectionserviceonClick(){
        PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/wearable_stopcollectionservice").setUrgent();
        putDataMapReq.getDataMap().putString("timestamp", Long.toString(System.currentTimeMillis()));
        putDataMapReq.getDataMap().putString("StopCollectionService", Long.toString(System.currentTimeMillis()));
        PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);
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
