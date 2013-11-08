package com.ajx.oscilloscope.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.usb.UsbManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DeviceManager {
    private static final int TIMEOUT = 1000;
    private final String TAG = DeviceManager.class.getSimpleName();
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    parseBuffer(data);
                }
            };
    private int baudRate;
    private int dataBits;
    private int stopBits;
    private int parity;
    private boolean swapBytes;
    private boolean allowBadBytes;
    private DataListener dataListener;
    private Context context;
    private UsbSerialDriver driver;
    private SerialInputOutputManager mSerialIoManager;

    public DeviceManager(Context context, DataListener dataListener) {
        this.context = context;
        this.dataListener = dataListener;
    }

    private static boolean sampleIsValid(int value) {
        return !(value > 1023 || value < 0);
    }

    private static byte[] byteToArray(byte b) {
        byte[] arr = new byte[1];
        arr[0] = b;
        return arr;
    }

    public void connect() throws IOException {
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        driver = UsbSerialProber.findFirstDevice(usbManager); // TODO: support multiple devices
        if (driver != null) {
            Log.i(TAG, "DeviceManager found. Using driver " + driver.getClass().getSimpleName());
            try {
                initializeDevice();
            } catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                try {
                    driver.close();
                } catch (IOException e2) {
                    // Do nothing
                }
                driver = null;
                throw new IOException("Error setting up device: " + e.getMessage(), e);
            }
        } else {
            Log.e(TAG, "Failed to load driver: device not attached");
            throw new IOException("Failed to load driver: device not attached");
        }
    }

    public void disconnect() throws IOException {
        if (driver != null) {
            try {
                driver.close();
                Log.i(TAG, "Disconnected");
            } catch (IOException e) {
                // Do nothing
            } finally {
                driver = null;
            }
        } else {
            Log.e(TAG, "Could not close connection: device is not connected");
            throw new IOException("Could not close connection: device is not connected");
        }
    }

    public void startListening() throws IOException {
        if (driver != null) {
            Log.i(TAG, "Starting IO manager..");
            mSerialIoManager = new SerialInputOutputManager(driver, mListener);
            mExecutor.submit(mSerialIoManager);
            Log.i(TAG, "Starting device..");
            try {
                sendStartCommand();
            } catch (IOException e) {
                Log.e(TAG, "Could not start listening to device" + e.getMessage(), e);
                throw new IOException("Could not start listening to device");
            }
        }
    }

    public void stopListening() throws IOException {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping IO manager..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
            Log.i(TAG, "Stopping device..");
            try {
                sendStopCommand();
            } catch (IOException e) {
                Log.e(TAG, "Could not stop listening to device" + e.getMessage(), e);
                throw new IOException("Could not stop listening to device");
            }
        }
    }

    public void sendCommand(byte[] command) throws IOException {
        if (driver != null) {
            try {
                driver.write(command, TIMEOUT);
            } catch (IOException e) {
                Log.e(TAG, "Error writing to device: " + e.getMessage(), e);
                throw e;
            }
        } else {
            Log.e(TAG, "Error writing to device: no device attached");
            throw new IOException("DeviceManager not found");
        }
    }

    public boolean isConnected() {
        return driver != null;
    }

    public boolean isListening() {
        return mSerialIoManager != null;
    }

    public void updateConfig() {
        baudRate = SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SharedPreferencesKeys.BAUD_RATE, 500000);
        dataBits = SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SharedPreferencesKeys.DATA_BITS, UsbSerialDriver.DATABITS_8);
        stopBits = SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SharedPreferencesKeys.STOP_BITS, UsbSerialDriver.STOPBITS_2);
        parity = SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SharedPreferencesKeys.PARITY, UsbSerialDriver.PARITY_NONE);
        swapBytes = SharedPreferencesHelper.getBoolean(context, SharedPreferencesHelper.SharedPreferencesKeys.SWAP_BYTES, true);
        allowBadBytes = SharedPreferencesHelper.getBoolean(context, SharedPreferencesHelper.SharedPreferencesKeys.ALLOW_BAD_BYTES, false);
    }

    private void parseBuffer(byte[] buffer) {
        int i = 0;
        int length = buffer.length;
        while (i < length - 1) {
            int value = (buffer[i + 1] << 8 | (buffer[i] & 0xFF)); // AND with 0xFF to handle unsigned types in Java
            int inverseValue = (buffer[i] << 8) | (buffer[i + 1] & 0xFF);
            if (sampleIsValid(value)) {
                dataListener.onValueReceived(value);
            } else {
                if (swapBytes && sampleIsValid(inverseValue)) {
                    dataListener.onValueReceived(inverseValue);
                } else {
                    Log.e(TAG, "Bad byte received!");
                    if (allowBadBytes) {
                        dataListener.onValueReceived(value);
                    }
                }
            }
            i += 2;
            if (i >= length - 1) return;
        }
    }

    // TODO: add config support
    private void sendStartCommand() throws IOException {
        byte c = 0x52;
        sendCommand(byteToArray(c));
    }

    // TODO: add config support
    private void sendStopCommand() throws IOException {
        byte c = 0x53;
        sendCommand(byteToArray(c));
    }

    private void initializeDevice() throws IOException {
        driver.open();
        driver.setParameters(baudRate, dataBits, stopBits, parity);
    }

    public interface DataListener {
        public void onValueReceived(int value);
    }

}
