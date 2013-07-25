package com.hanhuy.android.bluetooth.keyguard;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class KeyguardService extends Service {

    public final static String ACTION_PING =
            "com.hanhuy.android.bluetooth.keyguard.KEYGUARD_SERVICE_PING";
    public final static String ACTION_PONG =
            "com.hanhuy.android.bluetooth.keyguard.KEYGUARD_SERVICE_PONG";
    private final static String TAG = "KeyguardService";

    private final Handler handler = new Handler();

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
        registerReceiver(receiver, filter);

        KeyguardManager kgm = (KeyguardManager) getSystemService(
                KEYGUARD_SERVICE);
        kgml = kgm.newKeyguardLock(TAG);
        
        disableRunner.run();
    }

    // something seems to re-enable the keyguard on us, request disabling
    // every 15 minutes--device sleep should let this take longer between
    // delayed executions
    private final Runnable disableRunner = new Runnable() {
        @Override
        public void run() {
            if (kgml != null)
                kgml.disableKeyguard();
            handler.postDelayed(disableRunner, 15 * 60 * 1000);
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sendBroadcast(new Intent(ACTION_PONG));
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(disableRunner);
        unregisterReceiver(receiver);
        Log.v(TAG, "keyguard disabler destroyed");
        kgml.reenableKeyguard();
    }
}
