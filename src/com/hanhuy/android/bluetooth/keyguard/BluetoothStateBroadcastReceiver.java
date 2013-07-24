package com.hanhuy.android.bluetooth.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BluetoothStateBroadcastReceiver extends BroadcastReceiver {
    final static String TAG = "BluetoothStateBroadcastReceiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        Settings s = Settings.getInstance(ctx);
        Log.i(TAG, "clearing connected device state");
        s.set(Settings.BLUETOOTH_CONNECTIONS, null);
        KeyguardMediator.getInstance(ctx).notifyStateChanged();
    }
}
