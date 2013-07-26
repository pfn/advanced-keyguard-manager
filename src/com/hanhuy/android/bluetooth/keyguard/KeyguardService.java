package com.hanhuy.android.bluetooth.keyguard;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

public class KeyguardService extends Service {

    public final static String ACTION_PING =
            "com.hanhuy.android.bluetooth.keyguard.KEYGUARD_SERVICE_PING";
    public final static String ACTION_PONG =
            "com.hanhuy.android.bluetooth.keyguard.KEYGUARD_SERVICE_PONG";
    private final static String TAG = "KeyguardService";

    @SuppressWarnings("deprecation")
    private KeyguardManager.KeyguardLock kgml;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, "Starting keyguard disabler");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PING);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, filter);

        KeyguardManager kgm = (KeyguardManager) getSystemService(
                KEYGUARD_SERVICE);
        kgml = kgm.newKeyguardLock(TAG);
        kgml.disableKeyguard();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_PING.equals(intent.getAction())) {
                sendBroadcast(new Intent(ACTION_PONG));
            } else {
                if (kgml != null)
                    kgml.disableKeyguard();
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        Log.v(TAG, "keyguard disabler destroyed");
        kgml.reenableKeyguard();
    }
}
