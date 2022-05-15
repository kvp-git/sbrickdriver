package com.example.sbrickdriver;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements  BtLECallbacks
{
    private BtLE btLE;
    private String address;
    private byte[] speedTable;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (checkSelfPermission(permissions[0]) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(permissions, 0);

        speedTable = new byte[1+4*3];
        speedTable[0] = 0x01;
        speedTable[1+0*3] = 0x00;
        speedTable[1+1*3] = 0x01;
        speedTable[1+2*3] = 0x02;
        speedTable[1+3*3] = 0x03;
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
        tvStatus.setText(address);
        sbSpeed1.setProgress(255);
        sbSpeed2.setProgress(255);
        sbSpeed3.setProgress(255);
        sbSpeed4.setProgress(255);
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
            sbSpeed1.setProgress(255);
            sbSpeed2.setProgress(255);
            sbSpeed3.setProgress(255);
            sbSpeed4.setProgress(255);
            /*
            byte [] cmd = new byte[5];
            cmd[0] = 0x00;
            cmd[1] = 0x00;
            cmd[2] = 0x01;
            cmd[3] = 0x02;
            cmd[4] = 0x03;
            btLE.writeCommand(cmd);
            */
        });
        sbSpeed1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int value, boolean isFromUser)
            {
                speedCommand(0, value - 255);
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
                speedCommand(1, value - 255);
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
                speedCommand(2, value - 255);
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
                speedCommand(3, value - 255);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar){}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });
        bStop1.setOnClickListener(view ->
                sbSpeed1.setProgress(255));
        bStop2.setOnClickListener(view ->
                sbSpeed2.setProgress(255));
        bStop3.setOnClickListener(view ->
                sbSpeed3.setProgress(255));
        bStop4.setOnClickListener(view ->
                sbSpeed4.setProgress(255));

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
        cmd[0] = 0x0d; // watchdog timeout
        cmd[1] = 50; // 5 seconds
        btLE.writeCommand(cmd);
        sendSpeed();
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

    private synchronized void speedCommand(int channel, int value)
    {
        Log.e("Main", "speedCommand:" + channel + " " + value);
        byte direction = (byte)((value < 0) ? 1 : 0);
        byte speed = 0;
        if(value < 0) speed = (byte)(-value);
        if(value > 0) speed = (byte)(value);
        speedTable[1+channel*3+1] = direction;
        speedTable[1+channel*3+2] = speed;
        /*
        byte [] cmd = new byte[4];
        cmd[0] = 0x01;
        cmd[1] = (byte)(channel);
        cmd[2] = direction;
        cmd[3] = speed;
        btLE.writeCommand(cmd);
        */
    }

    private synchronized void sendSpeed()
    {
        if(btLE.isReady())
            btLE.writeCommand(speedTable);
        new Handler(getMainLooper()).postDelayed(() -> sendSpeed(), 100);
    }
}
