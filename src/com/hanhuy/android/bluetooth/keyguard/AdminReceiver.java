package com.hanhuy.android.bluetooth.keyguard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class AdminReceiver extends DeviceAdminReceiver{
    private final static String TAG = "AdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.v(TAG, "Enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "Your screen will no longer unlock automatically if disabled";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.v(TAG, "Disabled");
    }

    @Override
    public void onPasswordChanged(Context c, Intent intent) {
        Log.v(TAG, "Password Changed!");
        Notification n = new NotificationCompat.Builder(c)
                .setSmallIcon(android.R.drawable.ic_lock_lock)
                .setContentIntent(
                        PendingIntent.getActivity(c, 0, null, 0))
                .setContentTitle(
                        c.getString(R.string.notif_passwd_changed_title))
                .setContentText(c.getString(R.string.notif_passwd_changed_text))
                .build();
        Settings.getInstance(c).set(Settings.PASSWORD, null);
        Settings.getInstance(c).set(Settings.PASSWORD_HASH, null);
        NotificationManager nm =
                (NotificationManager) c.getSystemService(
                        Context.NOTIFICATION_SERVICE);
        nm.notify(1, n);
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        Log.v(TAG, "Login failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        Log.v(TAG, "Login succeeded");
    }
}
