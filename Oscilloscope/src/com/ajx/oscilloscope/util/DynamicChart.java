package com.ajx.oscilloscope.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import java.util.ArrayList;
import java.util.List;

public class DynamicChart {
    private static double referenceVoltage;
    private static int adcResolution;
    private static double gain;
    private static double multiplier;
    private final String TAG = DynamicChart.class.getSimpleName();
    private boolean plotRawData;
    private GraphView.GraphViewData[] graphData;
    private GraphViewSeries plotSeries;
    private GraphView chart;
    private boolean isCalibrating = false;
    private List<Double> calibrationValues;
    private int valuesCount = 0;
    private int maxValuesCount = 100;
    private double maxValue;
    private double minValue;
    private ViewGroup view;
    private Context context;

    public DynamicChart(Context context, ViewGroup view) {
        this.context = context;
        this.view = view;
    }

    private static double resultToVoltage(double value, double gain) {
        return value * (referenceVoltage / adcResolution) * multiplier + gain;
    }

    private static double resultToVoltage(double value) {
        return resultToVoltage(value, gain);
    }

    public void initializeChart() {
        updateConfig();
        isCalibrating = false;
        calibrationValues = new ArrayList<Double>();
        valuesCount = 0;
        graphData = new GraphView.GraphViewData[]{new GraphView.GraphViewData(0, 0)};
        plotSeries = new GraphViewSeries(graphData);
        chart = new LineGraphView(context, "");
        chart.addSeries(plotSeries);
        chart.setManualYAxisBounds(maxValue, minValue);
        chart.setShowLegend(false);
        chart.getGraphViewStyle().setHorizontalLabelsColor(Color.BLACK);
        chart.getGraphViewStyle().setVerticalLabelsWidth(45);
        chart.getGraphViewStyle().setTextSize(14);
        chart.getGraphViewStyle().setNumVerticalLabels(10);
        chart.setKeepScreenOn(true);
        chart.setScrollable(false);
        view.addView(chart);
    }

    public void updateConfig() {
        minValue = (SharedPreferencesHelper.getDouble(context, SharedPreferencesHelper.SharedPreferencesKeys.MIN_VALUE, 0));
        maxValue = (SharedPreferencesHelper.getDouble(context, SharedPreferencesHelper.SharedPreferencesKeys.MAX_VALUE, 1024));
        setBufferOffset(SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SharedPreferencesKeys.BUFFER_OFFSET, 200));
        plotRawData = SharedPreferencesHelper.getBoolean(context, SharedPreferencesHelper.SharedPreferencesKeys.PLOT_RAW_DATA, false);
        referenceVoltage = SharedPreferencesHelper.getDouble(context, SharedPreferencesHelper.SharedPreferencesKeys.REFERENCE_VOLTAGE, 2.56);
        multiplier = SharedPreferencesHelper.getDouble(context, SharedPreferencesHelper.SharedPreferencesKeys.MULTIPLIER, 1);
        adcResolution = SharedPreferencesHelper.getInt(context, SharedPreferencesHelper.SharedPreferencesKeys.ADC_RESOLUTION, 1024);
        gain = SharedPreferencesHelper.getDouble(context, SharedPreferencesHelper.SharedPreferencesKeys.GAIN, 0);
    }

    public void setKeepScreenOn(boolean value) {
        chart.setKeepScreenOn(value);
    }

    public void appendData(double data) {
        if (isCalibrating) {
            calibrate(data);
        }
        if (!plotRawData) {
            data = resultToVoltage(data);
        }
        plotSeries.appendData(new GraphView.GraphViewData(valuesCount++, data), false, maxValuesCount);
        chart.redrawAll();
    }

    public void setBufferOffset(int value) {
        maxValuesCount = value;
    }

    public void setGain(double value) {
        gain = value;
    }

    public void calibrate() {
        if (!isCalibrating) {
            isCalibrating = true;
            calibrationValues.clear();
        }
    }

    private void calibrate(double value) {
        if (calibrationValues.size() == 32) {
            int n = calibrationValues.size();
            double mean = 0;
            for (double d : calibrationValues) {
                mean += d;
            }
            mean /= n;
            double meanSquare = 0;
            for (double d : calibrationValues) {
                meanSquare += Math.pow((d - mean), 2);
            }
            double sigma = Math.sqrt(meanSquare / (n - 1));
            double cmean = 0;
            n = 0;
            for (double d : calibrationValues) {
                if (d >= mean - 3 * sigma && d <= mean + 3 * sigma) {
                    cmean += d;
                    n++;
                }
            }
            cmean /= n;
            this.isCalibrating = false;
            cmean = resultToVoltage(-cmean, 0);
            Log.i(TAG, "Calibrated value = " + cmean);
            setGain(cmean);
            SharedPreferencesHelper.putDouble(context, SharedPreferencesHelper.SharedPreferencesKeys.GAIN, cmean);
        } else {
            calibrationValues.add(value);
        }
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
//        //FIXME: it does not work
//        if (key.equals(KEY_PREF_MIN_VALUE)) {
//            setMinValue(Integer.valueOf(sharedPreferences.getString(key, "0")));
//        } else if (key.equals(KEY_PREF_MAX_VALUE)) {
//            setMaxValue(Integer.valueOf(sharedPreferences.getString(key, "1024")));
//        } else if (key.equals(KEY_PREF_BUFFER_OFFSET)) {
//            setBufferOffset(Integer.valueOf(sharedPreferences.getString(key, "200")));
//        }
//    }
}
