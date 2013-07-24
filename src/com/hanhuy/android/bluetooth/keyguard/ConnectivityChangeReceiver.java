package com.hanhuy.android.bluetooth.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ConnectivityChangeReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        KeyguardMediator.getInstance(context).notifyStateChanged();
    }
}
