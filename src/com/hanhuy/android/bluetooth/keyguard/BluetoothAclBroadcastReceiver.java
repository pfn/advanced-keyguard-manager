package com.hanhuy.android.bluetooth.keyguard;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class BluetoothAclBroadcastReceiver extends BroadcastReceiver {
    final static String TAG = "BluetoothAclBroadcastReceiver";

    @Override
    public void onReceive(Context ctx, Intent intent) {
        String action = intent.getAction();
        Settings s = Settings.getInstance(ctx);
        BluetoothDevice device = intent.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE);
        String address = device.getAddress();

        List<String> connectedDevices = s.get(Settings.BLUETOOTH_CONNECTIONS);

        HashSet<String> connected = Sets.newHashSet(connectedDevices);

        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
            connected.add(address);
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            connected.remove(address);
        } else {
            return;
        }

        if (!Iterables.elementsEqual(connectedDevices, connected)) {
            s.set(Settings.BLUETOOTH_CONNECTIONS,
                    Lists.newArrayList(connected));
            Log.v(TAG, "connected devices: " + connected);
            KeyguardMediator.getInstance(ctx).notifyStateChanged();
        }
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }
}
