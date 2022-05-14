package com.example.sbrickdriver;

import android.bluetooth.le.ScanResult;

public interface BtLECallbacks
{
    void connected();
    void disconnected();
    void readDone(int status, byte[] value);
    void writeDone(int status);
}
