package com.ajx.oscilloscope;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ajx.oscilloscope.util.DeviceManager;
import com.ajx.oscilloscope.util.DynamicChart;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class UsartActivity extends Activity {
    private final String TAG = UsartActivity.class.getSimpleName();
    private DynamicChart chart;
    private DeviceManager deviceManager;
    private TextView logView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.usart);
        logView = (TextView) findViewById(R.id.logView);
        RelativeLayout l = (RelativeLayout) findViewById(R.id.chartLayout);
        chart = new DynamicChart(this, l);
        chart.initializeChart();
        deviceManager = new DeviceManager(this, new DeviceManager.DataListener() {

            @Override
            public void onValueReceived(final int value) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chart.appendData(value);
                    }
                });
            }
        });
    }

    public void onConnectButtonClick(View view) {
        Button button = (Button) findViewById(R.id.buttonConnect);
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(this);
        logView.setText("");
        try {
            deviceManager.updateConfig();
            chart.updateConfig();
        } catch (Exception e) {
            Toast.makeText(this, "Error: could not update settings", Toast.LENGTH_SHORT).show();
        }
        boolean calibrateOnStartup = config.getBoolean("calibrate_on_startup", false);
        try {
            if (!deviceManager.isConnected()) {
                deviceManager.connect();
                deviceManager.startListening();
                if (deviceManager.isListening()) {
                    Toast.makeText(this, "Connection established.", Toast.LENGTH_SHORT).show();
                    if (calibrateOnStartup) chart.calibrate();
                    chart.setKeepScreenOn(true);
                }
            } else {
                deviceManager.stopListening();
                deviceManager.disconnect();
                if (!deviceManager.isConnected()) {
                    Toast.makeText(this, "Disconnected.", Toast.LENGTH_SHORT).show();
                    chart.setKeepScreenOn(false);
                }
            }
        } catch (IOException ex) {
            Toast.makeText(this, "Failed to connect.", Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            Log.d(TAG, "Error while disconnecting: " + ex.getMessage());
            Toast.makeText(this, "An error has occured. See log for details.", Toast.LENGTH_SHORT).show();
        } finally {
            if (!deviceManager.isConnected()) {
                button.setBackgroundResource(R.drawable.play);
            } else {
                button.setBackgroundResource(R.drawable.stop);
            }
        }
    }

    public void onSettingsButtonClick(View view) {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    @Deprecated
    private void _$addRandomData() {
        Timer t = new Timer();
        t.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        chart.appendData(Math.random() * 100);
                    }
                });
            }
        }, 20, 20);
    }
}
