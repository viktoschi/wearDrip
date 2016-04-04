package weardrip.weardrip.services;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.util.Log;
import android.view.SurfaceHolder;


import java.lang.Override;
import java.lang.Runnable;
import java.lang.String;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import weardrip.weardrip.MainActivity;
import weardrip.weardrip.wearDripWatchFace;

public class wearDripWatchFaceService extends CanvasWatchFaceService {
    private static final long TICK_PERIOD_MILLIS = TimeUnit.SECONDS.toMillis(1);

    @Override
    public Engine onCreateEngine() {
        return new SimpleEngine();
    }

    private class SimpleEngine extends CanvasWatchFaceService.Engine {

        private static final String ACTION_TIME_ZONE = "time-zone";
        private static final String TAG = "SimpleEngine";

        private wearDripWatchFace watchFace;
        private Handler timeTick;


        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(wearDripWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setAmbientPeekMode(WatchFaceStyle.AMBIENT_PEEK_MODE_HIDDEN)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());

            timeTick = new Handler(Looper.myLooper());
            startTimerIfNecessary();

            //watchFace = wearDripWatchFace.newInstance(wearDripWatchFaceService.this);

        }

        private void startTimerIfNecessary() {
            timeTick.removeCallbacks(timeRunnable);

            if (isVisible() && !isInAmbientMode()) {
                timeTick.post(timeRunnable);
            }
        }

        private final Runnable timeRunnable = new Runnable() {
            @Override
            public void run() {
                onSecondTick();

                if (isVisible() && !isInAmbientMode()) {
                    timeTick.postDelayed(this, TICK_PERIOD_MILLIS);
                }
            }
        };

        private void onSecondTick() {
            invalidateIfNecessary();
        }

        private void invalidateIfNecessary() {

            if (isVisible() && !isInAmbientMode()) {
                invalidate();
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerTimeZoneReceiver();
               // googleApiClient.connect();
                registeronChargeReceiver();
            } else {
                unregisterTimeZoneReceiver();
                //releaseGoogleApiClient();
                unregisteronChargeReceiver();
            }
            startTimerIfNecessary();
        }



        private void unregisterTimeZoneReceiver() {
            unregisterReceiver(timeZoneChangedReceiver);
        }

        private void registerTimeZoneReceiver() {
            IntentFilter timeZoneFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            registerReceiver(timeZoneChangedReceiver, timeZoneFilter);
        }

        private BroadcastReceiver timeZoneChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
                   // watchFace.updateTimeZoneWith(intent.getStringExtra(ACTION_TIME_ZONE));
                }
            }
        };

        private void unregisteronChargeReceiver() {
            unregisterReceiver(onCharger);
        }

        private void registeronChargeReceiver() {
            IntentFilter onCharge = new IntentFilter(Intent.ACTION_POWER_CONNECTED);
            registerReceiver(onCharger, onCharge);
        }

        private BroadcastReceiver onCharger = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (Intent.ACTION_POWER_CONNECTED.equals(intent.getAction())){
                    Log.d("watch ", "on charger");
                    Runnable r = new Runnable() {
                        @Override
                        public void run(){
                            Intent startIntent = new Intent(wearDripWatchFaceService.this, MainActivity.class);
                            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            wearDripWatchFaceService.this.startActivity(startIntent);
                        }
                    };
                    Handler h = new Handler();
                    h.postDelayed(r, 1000);
                }

               if (Intent.ACTION_POWER_DISCONNECTED.equals(intent.getAction())) {
                    Log.d("watch ", "not on charger");
                    //Intent startIntent = new Intent(wearDripWatchFaceService.this, MainActivity.class);
                    //intent.putExtra("stop", true);
                    //startActivity(startIntent);
               }
            }
        };

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            super.onDraw(canvas, bounds);
            //watchFace.draw(canvas, bounds);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            //watchFace.setAntiAlias(!inAmbientMode);
           // watchFace.setShowSeconds(!isInAmbientMode());

            if (inAmbientMode) {
                //watchFace.updateBackgroundColourToDefault();
               // watchFace.updateDateAndTimeColourToDefault();
             //   watchFace.showBG();
            } else {
               // watchFace.restoreBackgroundColour();
               // watchFace.restoreDateAndTimeColour();
               // watchFace.showBG();
            }
            invalidate();
            startTimerIfNecessary();
        }


        int mTapCount=0;
        private void changeBackground() {
            mTapCount++;
            switch(mTapCount % 2) {
                case 0:
                  //  watchFace.wfChangeCase0();
                    invalidate();
                    break;
                case 1:
                   // watchFace.wfChangeCase1();
                    invalidate();
                    break;
            }
        }
    }
}