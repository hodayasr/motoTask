package com.hodaya.firsttaskapplication;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.appcompat.widget.SwitchCompat;

public class BluetoothReceiver extends BroadcastReceiver {
    SwitchCompat mView;

    public BluetoothReceiver(SwitchCompat view){
        mView = view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
        switch(state) {
            case BluetoothAdapter.STATE_OFF:
            case BluetoothAdapter.STATE_TURNING_OFF:
                mView.setChecked(false);
                break;
            case BluetoothAdapter.STATE_ON:
            case BluetoothAdapter.STATE_TURNING_ON:
                mView.setChecked(true);
                break;
        }
    }
}
