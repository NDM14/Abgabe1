package com.example.sensorreading;

import android.hardware.SensorEventListener;


import static android.app.ProgressDialog.show;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;
import androidx.localbroadcastmanager.*;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class GyroscopeService extends Service implements SensorEventListener {
    private SensorManager sensorManager;
    private LocalBroadcastManager localBroadcastManager;
    private static String TAG = GyroscopeService.class.getCanonicalName();

    private GyroscopeServiceImpl impl;

    private Context mContext;

    private int gyThreshold = 1;

    private Toast mToast = null;

    private class GyroscopeServiceImpl extends IGyroscopeService.Stub {
        @Override
        public void readSensorData(int period, int gyThreshold) throws RemoteException {
            Log.i(TAG, "Reading sensor data");
        }
    }


    @Override
    public void onCreate (){
        super.onCreate();

        impl = new GyroscopeServiceImpl();

    }
    @Override
    public IBinder onBind(Intent intent) {
        return impl;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mContext=getApplicationContext();

        int period = 10;
        Bundle extras = intent.getExtras();
        if (extras == null) {
            Log.d(TAG, "using default period 10");

        } else {
            Log.d(TAG, "using configured period");
            period = (int) extras.get("period");
        }

        SensorManager sensorManager;
        Sensor sensor;
        boolean sensorListenerRegistered = false;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (sensorManager.registerListener(this, sensor, period)) {
            Log.i(TAG, "Gyroscope sensor listener registered");
            sensorListenerRegistered = true;
        } else {
            Log.e(TAG, " Could not register gyroscope sensor listener");
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Destroying service");
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gyX = event.values[0];
        float gyY = event.values[1];
        float gyZ = event.values[2];

        if(Math.abs(gyX) > gyThreshold || Math.abs(gyY) > gyThreshold || Math.abs(gyZ) > gyThreshold) {
            showToast("Woah stop rotating");
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    //function to show toast from service is taken from https://stackoverflow.com/questions/44897906/toast-not-working-in-service
    private void showToast(String text) {
        if (mContext != null) {
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //cancel previous toats because too many toasts crash the app
                    if (mToast != null) mToast.cancel();
                    mToast = Toast.makeText(mContext, text, Toast.LENGTH_SHORT);
                    mToast.show();
                }
            });

        }
    }
}
