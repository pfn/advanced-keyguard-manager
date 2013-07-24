package com.hanhuy.android.bluetooth.keyguard;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

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
        
        KeyguardManager kgm = (KeyguardManager) getSystemService(
                KEYGUARD_SERVICE);
        kgml = kgm.newKeyguardLock(TAG);
        
        kgml.disableKeyguard();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        kgml.reenableKeyguard();
    }
}
