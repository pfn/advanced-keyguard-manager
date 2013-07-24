package com.hanhuy.android.bluetooth.keyguard;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.lang.annotation.ElementType;

public class AdminReceiver extends DeviceAdminReceiver{
    private final static String TAG = "AdminReceiver";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.v(TAG, "Enabled");
        KeyguardMediator.getInstance(context).notifyStateChanged();
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return context.getString(R.string.admin_disable_warning);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.v(TAG, "Disabled");
        KeyguardMediator.getInstance(context).notifyStateChanged();
        Settings s = Settings.getInstance(context);
        s.set(Settings.LOCK_DISABLED, false);
    }

    @Override
    public void onPasswordChanged(Context c, Intent intent) {
        Log.v(TAG, "Password Changed!");
        Settings s = Settings.getInstance(c);
        KeyguardMediator kgm = KeyguardMediator.getInstance(c);
        if (s.get(Settings.PASSWORD) != null && !kgm.passwordSetRecently()) {
            PendingIntent pending = PendingIntent.getActivity(
                    c, 0, new Intent(c, PasswordActivity.class), 0);
            Notification n = new NotificationCompat.Builder(c)
                    .setAutoCancel(true)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentIntent(pending)
                    .setContentTitle(
                            c.getString(R.string.notif_passwd_changed_title))
                    .setContentText(
                            c.getString(R.string.notif_passwd_changed_text))
                    .setTicker(
                            c.getString(R.string.notif_passwd_changed_text))
                    .build();
            s.set(Settings.PASSWORD, null);
            s.set(Settings.PASSWORD_HASH, null);
            s.set(Settings.LOCK_DISABLED, false);
            NotificationManager nm =
                    (NotificationManager) c.getSystemService(
                            Context.NOTIFICATION_SERVICE);
            nm.notify(KeyguardMediator.NOTIFICATION_RESET, n);
            kgm.notifyStateChanged();
        }
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
