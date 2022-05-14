package com.example.sbrickdriver;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements  BtLECallbacks
{
    private BtLE btLE;
    private String address;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(permissions, 0);

        address = getSharedPreferences("kvp", Context.MODE_PRIVATE)
                .getString("address", "00:07:80:BD:15:76");
        Button bSave = findViewById(R.id.saveButton);
        Button bStop = findViewById(R.id.stopButton);
        TextView tvStatus = findViewById(R.id.statusText);
        EditText etAddress = findViewById(R.id.addressText);
        SeekBar sbSpeed1 = findViewById(R.id.inputBar1);
        SeekBar sbSpeed2 = findViewById(R.id.inputBar2);
        SeekBar sbSpeed3 = findViewById(R.id.inputBar3);
        SeekBar sbSpeed4 = findViewById(R.id.inputBar4);
        Button bStop1 = findViewById(R.id.inputStop1);
        Button bStop2 = findViewById(R.id.inputStop2);
        Button bStop3 = findViewById(R.id.inputStop3);
        Button bStop4 = findViewById(R.id.inputStop4);

        etAddress.setText(address);
        bSave.setOnClickListener(view ->
        {
            address = etAddress.getText().toString();
            getSharedPreferences("kvp", Context.MODE_PRIVATE)
                    .edit()
                    .putString("address", address)
                    .commit();
            btLE.disconnect();
            btLE.connect(this, address);
        });
        bStop.setOnClickListener(view ->
        {
            sbSpeed1.setProgress(0);
            sbSpeed2.setProgress(0);
            sbSpeed3.setProgress(0);
            sbSpeed4.setProgress(0);
        });
        sbSpeed1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isFromUser)
            {
                speedCommand(0, value);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });
        sbSpeed2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isFromUser)
            {
                speedCommand(1, value);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });
        sbSpeed3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isFromUser)
            {
                speedCommand(2, value);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });
        sbSpeed4.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isFromUser)
            {
                speedCommand(3, value);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });
        bStop1.setOnClickListener(view ->
                sbSpeed1.setProgress(0));
        bStop2.setOnClickListener(view ->
                sbSpeed2.setProgress(0));
        bStop3.setOnClickListener(view ->
                sbSpeed3.setProgress(0));
        bStop4.setOnClickListener(view ->
                sbSpeed4.setProgress(0));

        btLE = new BtLE(this, this);
        if(!address.isEmpty())
            btLE.connect(this, address);
    }

    @Override
    public void connected()
    {
        TextView tvStatus = findViewById(R.id.statusText);
        tvStatus.setText(address + " connected");
        byte[] cmd = new byte[2];
        cmd[0] = 0x0d;
        cmd[1] = 0x00;
        btLE.writeCommand(cmd);
    }

    @Override
    public void disconnected()
    {
        TextView tvStatus = findViewById(R.id.statusText);
        tvStatus.setText(address + " disconnected");
    }

    @Override
    public void readDone(int status, byte[] value)
    {
        // TODO!!!
    }

    @Override
    public void writeDone(int status)
    {
        // TODO!!!
    }

    private void speedCommand(int channel, int value)
    {
        Log.e("Main", "speedCommand:" + channel + " " + value);
        byte direction = (byte)((value < 0) ? 1 : 0);
        byte speed = 0;
        if(value < 0) speed = (byte)(-value);
        if(value > 0) speed = (byte)(value);
        byte [] cmd = new byte[4];
        cmd[0] = 0x01;
        cmd[1] = (byte)(channel);
        cmd[2] = direction;
        cmd[3] = speed;
        btLE.writeCommand(cmd);
    }
}
