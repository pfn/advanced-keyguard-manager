package com.hanhuy.android.bluetooth.keyguard;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

// TODO use alarm manager to refresh keyguard lock
public class KeyguardService extends Service {
    
    final static String TAG = "KeyguardService";
    private KeyguardManager.KeyguardLock kgml;
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    @SuppressWarnings("deprecated")
    public void onCreate() {
        super.onCreate();

        Log.v(TAG, "Starting keyguard disabler");
        KeyguardManager kgm = (KeyguardManager) getSystemService(
                KEYGUARD_SERVICE);
        kgml = kgm.newKeyguardLock(TAG);
        
        kgml.disableKeyguard();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "keyguard disabler destroyed");
        kgml.reenableKeyguard();
    }
}
