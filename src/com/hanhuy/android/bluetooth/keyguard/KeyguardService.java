package com.hanhuy.android.bluetooth.keyguard;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class KeyguardService extends Service {

    public final static String ACTION_CANCEL =
            "com.hanhuy.android.bluetooth.keyguard.KEYGUARD_QUIT";
    public final static String ACTION_PING =
            "com.hanhuy.android.bluetooth.keyguard.KEYGUARD_SERVICE_PING";
    public final static String ACTION_PONG =
            "com.hanhuy.android.bluetooth.keyguard.KEYGUARD_SERVICE_PONG";
    private final static String TAG = "KeyguardService";
    private KeyguardManager kgm;

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
        filter.addAction(ACTION_CANCEL);
        registerReceiver(receiver, filter);

        kgm = (KeyguardManager) getSystemService(
                KEYGUARD_SERVICE);
        kgml = kgm.newKeyguardLock(TAG);
        kgml.disableKeyguard();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        @SuppressWarnings("deprecation")
        public void onReceive(Context context, Intent intent) {
        if (ACTION_PING.equals(intent.getAction())) {
            sendBroadcast(new Intent(ACTION_PONG));
        } else if (ACTION_CANCEL.equals(intent.getAction())) {
            stopForeground(true);
            stopSelf();
        }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notification n = new NotificationCompat.Builder(this)
                .setContentTitle(getString(R.string.disabler_running))
                .setSmallIcon(R.drawable.ic_lock)
                .setPriority(Notification.PRIORITY_MIN)
                .setOngoing(true)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel,
                        getString(R.string.enable), PendingIntent.getBroadcast(
                        this, 0, new Intent(ACTION_CANCEL),
                        PendingIntent.FLAG_UPDATE_CURRENT))
                .build();

        startForeground(1, n);
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
