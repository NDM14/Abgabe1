package com.example.sensorreading;

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

public class SensorReadingService extends Service implements SensorEventListener {

    private static String TAG = SensorReadingService.class.getCanonicalName();

    private SensorReadingServiceImpl impl;

    private Context mContext;

    private int accThreshold = 5;

    private Toast mToast = null;

    private class SensorReadingServiceImpl extends ISensorReadingService.Stub {

        @Override
        public void readSensorData(int period, int accThreshold) throws RemoteException {
            Log.i(TAG, "Reading sensor data");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        impl = new SensorReadingServiceImpl();
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
        if (extras != null) {
            period = (int) extras.get("period");
        }

        SensorManager sensorManager;
        Sensor sensor;
        boolean sensorListenerRegistered = false;

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (sensorManager.registerListener(this, sensor, period)) {
            Log.i(TAG, "Accelerometer sensor listener registered");
            sensorListenerRegistered = true;
        } else {
            Log.e(TAG, " Could not register accelerometer sensor listener");
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

        float accX = event.values[0];
        float accY = event.values[1];
        float accZ = event.values[2];

        //default Y acceleration is 9.81
        accY = (float) (accY - 9.81);

        if(Math.abs(accX) > accThreshold || Math.abs(accY) > accThreshold || Math.abs(accZ) > accThreshold) {
            showToast("Woah slow down");
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
