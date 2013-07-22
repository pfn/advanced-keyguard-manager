package com.hanhuy.android.bluetooth.keyguard;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class BluetoothAclBroadcastReceiver extends BroadcastReceiver {
    final static String TAG = "BluetoothAclBroadcastReceiver";
    final static String PREF_CONNECTED_DEVICES = "connected_devices";

    @Override
    @SuppressWarnings("unchecked")
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE);
        String address = device.getAddress();
        
        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(ctx);

        String connectedDevices = prefs.getString(PREF_CONNECTED_DEVICES, null);
        HashSet<String> connected = new HashSet<String>(
                connectedDevices == null ? Collections.EMPTY_SET : 
                    Arrays.asList(connectedDevices.split(",")));

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            String selectedAddresses = prefs.getString(
                    MainActivity.PREF_BLUETOOTH_DEVICES, null);

            if (selectedAddresses == null)
                return;

            List<String> selected = Arrays.asList(selectedAddresses.split(","));
            if (!selected.contains(address))
                return;
            
            boolean added = connected.add(address);

            if (added && connected.size() == 1)
                setPatternLockEnabled(ctx, device.getName(), prefs, false);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            boolean removed = connected.remove(address);
            
            if (removed && connected.size() == 0)
                setPatternLockEnabled(ctx, device.getName(), prefs, true);
        } else {
            return;
        }

        String connectedString = null;
        if (connected.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String addr : connected) {
                sb.append(addr);
                sb.append(",");
            }
            sb.setLength(sb.length() - 1);
            connectedString = sb.toString();
        }
        if (!eq(connectedDevices, connectedString)) {
            prefs.edit().putString(
                    PREF_CONNECTED_DEVICES, connectedString).commit();
        }
    }
    
    private static void setPatternLockEnabled(
            final Context ctx, String name, SharedPreferences prefs,
            final boolean enabled) {
        Log.i(TAG, "Setting lock pattern enabled: " + enabled);
        LockPatternUtil.setLockPatternEnabled(ctx, enabled);
        Toast.makeText(ctx, String.format(ctx.getString(enabled ?
                R.string.keyguard_enabled : R.string.keyguard_disabled), name),
                Toast.LENGTH_LONG).show();
        
        if (prefs.getBoolean(MainActivity.PREF_CLEAR_KEYGUARD, false)) {
            if (enabled) {
                ctx.stopService(new Intent(ctx, KeyguardService.class));
            } else {
                ctx.startService(new Intent(ctx, KeyguardService.class));
            }
        }
    }
    
    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}
