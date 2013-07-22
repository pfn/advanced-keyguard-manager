package com.hanhuy.android.bluetooth.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class BluetoothStateBroadcastReceiver extends BroadcastReceiver {
    final static String TAG = "BluetoothStateBroadcastReceiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(ctx);

        Log.i(TAG, "clearing connected device state");
        prefs.edit().putString(
                BluetoothAclBroadcastReceiver.PREF_CONNECTED_DEVICES,
                null).commit();
        LockPatternUtil.setLockPatternEnabled(ctx, true);
        ctx.stopService(new Intent(ctx, KeyguardService.class));
    }
}
