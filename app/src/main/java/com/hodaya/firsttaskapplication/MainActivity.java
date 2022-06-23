package com.hodaya.firsttaskapplication;

import android.Manifest;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.tastytoast.TastyToast;


public class MainActivity extends AppCompatActivity {
    private static final String TAG = "FirstTask_Activity";
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final String SHARED_PREFS = "sharedPrefs";
    private static final String NAME = "name";
    SharedPreferences mSharedPreferences;
    TextView mAirplaneListener;
    Button mOpenCameraBtn, mSetNameBtn;
    SwitchCompat mBluetoothModeBtn;
    BluetoothAdapter mBluetoothAdapter;
    EditText mInput;
    String mNameText;
    AirplaneReceiver mReceiver;
    BluetoothReceiver mBluetoothRec;
    BluetoothManager mBluetoothManager;
    ImageButton mPlayMusicBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAirplaneListener = findViewById(R.id.airplaneModeTxt);
        mOpenCameraBtn = findViewById(R.id.openCameraBtn);
        mBluetoothModeBtn = findViewById(R.id.switchBluetoothMode);
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mSharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        mNameText = getString(R.string.empty);
        mSetNameBtn = findViewById(R.id.setNewName);
        mPlayMusicBtn=findViewById(R.id.play_music_btn);
        Log.i(TAG, "onCreate: ");

        // Airplane Mode //
        airPlaneMode();

        // Open camera //
        mOpenCameraBtn.setOnClickListener(view -> checkCameraPermission());

        // Bluetooth ON/OFF //
        checkBlueToothState();
        bluetoothMode();
        bluetoothOnChecked();

        // Shared Preferences  - NAME //
        sayHello();

        // Set New NAME button //
        mSetNameBtn.setOnClickListener(view -> alertDialogSetName());

        mPlayMusicBtn.setOnClickListener(view -> playMusic());
    }

    private void playMusic() {
        if(!isMyServiceRunning()) {
            startService(new Intent(this,MusicService.class));
            TastyToast.simple(this, getString(R.string.ringplaying), Toast.LENGTH_SHORT,TastyToast.SHAPE_ROUND,false);
        } else {
            TastyToast.simple(this, getString(R.string.ringstopping), Toast.LENGTH_SHORT,TastyToast.SHAPE_ROUND,false);
            stopService(new Intent(this,MusicService.class));
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (MusicService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        airPlaneMode();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: ");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        unregisterReceiver(mReceiver);
        unregisterReceiver(mBluetoothRec);
    }



    private void sayHello() {
        mInput = new EditText(this);
        loadName();

        if (mNameText.equals(getString(R.string.empty))) {
            alertDialogSetName();
        } else {
            TastyToast.simple(MainActivity.this, getString(R.string.hello) + mNameText, Toast.LENGTH_SHORT, TastyToast.SHAPE_RECTANGLE, false);
        }
    }

    private void loadName() {
        mNameText = mSharedPreferences.getString(NAME, getString(R.string.empty));
    }

    private void saveName(String name) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(NAME, name);
        editor.apply();
    }

    private void alertDialogSetName() {
        mInput = new EditText(getApplicationContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(getString(R.string.insert_your_name))
                .setView(mInput)
                .setPositiveButton(getString(R.string.insert), (dialog, id) -> {
                    // save name in SharedPreferences
                    String inputName = mInput.getText().toString();
                    String expression = getString(R.string.letters_expression);
                    if (mInput.length()<=2 || !(inputName.replace(" ", "").matches(expression))) {
                        TastyToast.error(this, getString(R.string.name_not_valid), TastyToast.LENGTH_SHORT, TastyToast.SHAPE_ROUND, false);
                        dialog.cancel();
                    } else {
                        saveName(inputName);
                        loadName();
                        sayHello();
                    }}).setNegativeButton("Cancel", (dialog, whichButton) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void airPlaneMode() {
        if (isAirplaneModeOn(this)) {
            mAirplaneListener.setText(getString(R.string.airplane_on));
        } else {
            mAirplaneListener.setText(getString(R.string.airplane_off));
        }
        mReceiver = new AirplaneReceiver(mAirplaneListener);
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        registerReceiver(mReceiver, intentFilter);
    }

    private static boolean isAirplaneModeOn(Context context) {
        return Settings.System.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, getString(R.string.no_permission));
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, MainActivity.CAMERA_PERMISSION_CODE);
        } else {
            Log.i(TAG, getString(R.string.permission_already_granted));
            openCamera();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                TastyToast.error(this, getString(R.string.permission_denied), TastyToast.LENGTH_SHORT, TastyToast.SHAPE_RECTANGLE, false);
            }
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivity(cameraIntent);
    }

    private void bluetoothMode() {
        mBluetoothRec = new BluetoothReceiver(mBluetoothModeBtn);
        IntentFilter bluetoothFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mBluetoothRec, bluetoothFilter);
    }

    private void checkBlueToothState() {
        if (mBluetoothManager == null) {
            TastyToast.error(this, getString(R.string.bluetooth_mngr_is_null), TastyToast.LENGTH_SHORT, TastyToast.SHAPE_ROUND, false);
        }
        if (mBluetoothAdapter == null) {
            TastyToast.error(this, getString(R.string.bluetooth_not_support), TastyToast.LENGTH_SHORT, TastyToast.SHAPE_RECTANGLE, false);
        } else {
            mBluetoothModeBtn.setChecked(mBluetoothAdapter.isEnabled());
        }
    }

    private void bluetoothOnChecked() {
        mBluetoothModeBtn.setOnClickListener(view -> {
            if (mBluetoothModeBtn.isChecked()) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, getString(R.string.no_permission));
                        mBluetoothModeBtn.setChecked(false);
                    }
                    startActivity(enableBtIntent);
                    Log.i(TAG, getString(R.string.bluetooth_on));
                }
            } else {
                Log.i(TAG, getString(R.string.bluetooth_off));
                mBluetoothAdapter.disable();
            }
        });
    }
}
