package com.hanhuy.android.bluetooth.keyguard;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

import static com.hanhuy.android.bluetooth.keyguard.Settings.device;
import static com.hanhuy.android.bluetooth.keyguard.Settings.network;

public class LockMediator {
    public final static int NOTIFICATION_RESET = 1;
    public final static int NOTIFICATION_TOGGLE = 2;
    public final static String ACTION_STATE_CHANGED =
            "com.hanhuy.android.bluetooth.keyguard.KGM_STATE_CHANGE";
    private final static String TAG = "LockMediator";
    private static LockMediator instance;
    private final Context ctx;
    private final DevicePolicyManager dpm;
    private final Settings settings;
    private final KeyguardManager kgm;
    private final PowerManager pm;

    public static class Status {
        public final boolean security;
        public final boolean keyguard;
        public final boolean requireUnlock;
        public Status(
                final boolean security,
                final boolean keyguard,
                final boolean requireUnlock) {
            this.security = security;
            this.keyguard = keyguard;
            this.requireUnlock = requireUnlock;
        }
    }
    private LockMediator(Context c) {
        ctx = c;
        settings = Settings.getInstance(ctx);
        pm = (PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
        kgm = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);
        dpm = (DevicePolicyManager) ctx.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
    }

    public static LockMediator getInstance(Context c) {
        if (instance == null)
            instance = new LockMediator(c.getApplicationContext());
        return instance;
    }

    public void notifyStateChanged() {
        final boolean disabled = settings.get(Settings.LOCK_DISABLED);
        final Status status = getLockMediatorStatus();
        final boolean shouldDisable = !status.security;

        if (!dpm.isAdminActive(new ComponentName(ctx, AdminReceiver.class))) {
            Log.v(TAG, "device administrator is not active");
            return;
        }

        if (status.keyguard) {
            ctx.stopService(new Intent(ctx, KeyguardService.class));
        } else {
            if (!status.requireUnlock || disabled ||
                    (pm.isScreenOn() && !kgm.inKeyguardRestrictedInputMode())) {
                ctx.startService(new Intent(ctx, KeyguardService.class));
            }
        }

        if (disabled != shouldDisable && CryptoUtils.isPasswordSaved(ctx)) {

            if (status.requireUnlock && !disabled && (!pm.isScreenOn() ||
                    kgm.inKeyguardRestrictedInputMode())) {
                Log.v(TAG, "Unlock is required before disabling");
                return;
            }

            Log.v(TAG, "disabling lock screen: " + !shouldDisable);
            settings.set(Settings.LOCK_DISABLED, shouldDisable);
            updatePasswordSetTime();

            dpm.resetPassword(shouldDisable ? "" : CryptoUtils.getPassword(ctx), 0);

            if (settings.get(Settings.SHOW_NOTIFICATIONS)) {
                PendingIntent pending = PendingIntent.getActivity(
                        ctx, 0, new Intent(ctx, MainActivity.class), 0);
                String text = ctx.getString(shouldDisable ?
                        R.string.lockscreen_disabled :
                        R.string.lockscreen_enabled);
                Notification n = new NotificationCompat.Builder(ctx)
                        .setAutoCancel(true)
                        .setTicker(text)
                        .setSmallIcon(R.drawable.ic_lock)
                        .setContentIntent(pending)
                        .setContentTitle(ctx.getString(R.string.notif_title))
                        .setContentText(text)
                        .build();
                NotificationManager nm =
                        (NotificationManager) ctx.getSystemService(
                                Context.NOTIFICATION_SERVICE);
                nm.notify(LockMediator.NOTIFICATION_TOGGLE, n);
            }
            ctx.sendBroadcast(new Intent(ACTION_STATE_CHANGED));
        }
    }

    public Status getLockMediatorStatus() {
        boolean disableLock = false;
        boolean disableKG = false;
        boolean requireUnlock = false;

        if (!CryptoUtils.isPasswordSaved(ctx)) {
            Log.v(TAG, "password and/or hmac not set [properly]");
            return new Status(!disableLock, !disableKG, false);
        }

        if (settings.get(Settings.WIFI_CLEAR_KEYGUARD)) {
            WifiManager wm = (WifiManager) ctx.getSystemService(
                    Context.WIFI_SERVICE);
            final WifiInfo current = wm.getConnectionInfo();
            List<String> selectedAPs = settings.get(Settings.WIFI_NETWORKS);

            if (current != null) {
                String ssid = current.getSSID();
                if (ssid != null) {
                    Set<String> selected = Sets.newHashSet(selectedAPs);
                    String altSSID = !Strings.isNullOrEmpty(ssid) &&
                            ssid.charAt(0) != '"' ? "\"" + ssid + "\"" : null;
                    boolean hasNetworks = selected.contains(ssid) ||
                            (altSSID != null && selected.contains(altSSID));
                    if (hasNetworks) {
                        Log.v(TAG, String.format("Found networks: %s in %s",
                                current.getSSID(), selectedAPs));
                    }
                    disableKG |= settings.get(
                            network(ssid, Settings.DISABLE_KEYGUARD)) ||
                            (altSSID != null &&
                                    settings.get(network(altSSID,
                                            Settings.DISABLE_KEYGUARD)));
                    requireUnlock |= settings.get(
                            network(ssid, Settings.REQUIRE_UNLOCK)) ||
                            (altSSID != null &&
                                    settings.get(network(altSSID,
                                            Settings.REQUIRE_UNLOCK)));
                    disableLock |= hasNetworks;
                }
            }
        }

        final boolean[] _disableKG = { false };
        final boolean[] _requireUnlock = { false };
        List<String> selectedDevices = settings.get(Settings.BLUETOOTH_DEVICES);
        if (settings.get(Settings.BT_CLEAR_KEYGUARD) &&
                selectedDevices.size() > 0) {
            List<String> connectedDevices = settings.get(
                    Settings.BLUETOOTH_CONNECTIONS);

            final Set<String> selected = Sets.newHashSet(
                    selectedDevices);
            boolean hasDevices = Iterables.tryFind(connectedDevices,
                    new Predicate<String>() {
                        @Override
                        public boolean apply(java.lang.String addr) {
                            _disableKG[0] |= settings.get(
                                    device(addr, Settings.DISABLE_KEYGUARD));
                            _requireUnlock[0] |= settings.get(
                                    device(addr, Settings.REQUIRE_UNLOCK));
                            return selected.contains(addr);
                        }
                    }).isPresent();
            if (hasDevices) {
                Log.v(TAG, String.format( "Found devices: %s in %s",
                        connectedDevices, selectedDevices));
            }
            disableKG |= _disableKG[0];
            requireUnlock |= _requireUnlock[0];
            disableLock |= hasDevices;
        }
        return new Status(!disableLock, !disableKG, requireUnlock);
    }

    public void updatePasswordSetTime() {
        settings.set(Settings.LAST_STATE_CHANGE, System.currentTimeMillis());
    }

    // there is a window of opportunity where a settings-changed password
    // reset will mess this up
    public boolean passwordSetRecently() {
        return System.currentTimeMillis() -
                settings.get(Settings.LAST_STATE_CHANGE) < 20 * 10000;
    }
}
