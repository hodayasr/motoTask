package com.hodaya.firsttaskapplication;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tastytoast.TastyToast;


public class AirplaneReceiver extends BroadcastReceiver {
    public static final String STATE="state";
    TextView mView;

    public AirplaneReceiver(TextView view){
        mView = view;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean state = intent.getBooleanExtra(STATE, false);
        mView.setText(context.getString(state ? R.string.airplane_on : R.string.airplane_off));
        TastyToast.simple(context, context.getString(state ? R.string.airplane_on_toast : R.string.airplane_off_toast),
                Toast.LENGTH_SHORT,TastyToast.SHAPE_RECTANGLE,false );
    }
}
