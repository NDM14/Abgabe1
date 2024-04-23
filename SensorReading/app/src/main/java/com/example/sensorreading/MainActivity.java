package com.example.sensorreading;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

    private static String TAG = MainActivity.class.getCanonicalName();

    private ISensorReadingService sensorReadingServiceProxy = null;
    private IGyroscopeService gyroscopeServiceProxy = null;

    private EditText periodInput;

    private int period = 10;
    private int accThreshold = 5;
    private int gyThreshold = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        periodInput = findViewById(R.id.periodInput);

        Intent i = new Intent(this, SensorReadingService.class);
        Intent gi = new Intent(this, GyroscopeService.class);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get period from input field
                String rawPeriodInput = periodInput.getText().toString();
                try {
                    period = Integer.parseInt(rawPeriodInput);
                } catch (NumberFormatException ex) {
                    Log.i(TAG, "Input is not a number; using default period");
                }
                Log.i(TAG, "period: " + period);

                //pass period and threshold to the service
                i.putExtra("period", period);
                i.putExtra("accThreshold", accThreshold);
                startService(i);

                gi.putExtra("period", period);
                gi.putExtra("gyThreshold", gyThreshold);
                startService(gi);

            }
        });

        FloatingActionButton stopFab = findViewById(R.id.stopFab);
        stopFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(i);
                stopService(gi);
            }
        });
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        Log.i(TAG, "Service connected");
        sensorReadingServiceProxy = ISensorReadingService.Stub.asInterface(iBinder);
        gyroscopeServiceProxy = IGyroscopeService.Stub.asInterface(iBinder);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        Log.i(TAG, "Service disconnected");
        sensorReadingServiceProxy = null;
        gyroscopeServiceProxy = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(sensorReadingServiceProxy != null) {
            unbindService(this);
        }
        if(gyroscopeServiceProxy != null) {
            unbindService(this);
        }
    }

}