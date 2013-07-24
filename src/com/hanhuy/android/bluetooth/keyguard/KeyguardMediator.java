package com.hanhuy.android.bluetooth.keyguard;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class KeyguardMediator {
    public final static String ACTION_STATE_CHANGED =
            "com.hanhuy.android.bluetooth.keyguard.KGM_STATE_CHANGE";
    private final static String TAG = "KeyguardMediator";
    private static KeyguardMediator instance;
    private final Context ctx;
    private final DevicePolicyManager dpm;
    private final Settings settings;

    private KeyguardMediator (Context c) {
        ctx = c;
        settings = Settings.getInstance(ctx);
        dpm = (DevicePolicyManager) ctx.getSystemService(
                Context.DEVICE_POLICY_SERVICE);
    }

    public static KeyguardMediator getInstance(Context c) {
        if (instance == null)
            instance = new KeyguardMediator(c.getApplicationContext());
        return instance;
    }

    public void notifyStateChanged() {
        boolean disabled = settings.get(Settings.LOCK_DISABLED);
        boolean newState = isSecurityEnabled();
        if (disabled != newState) {

            Log.v(TAG, "toggling lock screen state: " + newState);
            ctx.sendBroadcast(new Intent(ACTION_STATE_CHANGED));
            settings.set(Settings.LOCK_DISABLED, newState);
            updatePasswordSetTime();
            dpm.resetPassword(disabled ? "" : CryptoUtils.getPassword(ctx), 0);
        }
    }

    public boolean isSecurityEnabled() {
        boolean disableKG = false;
        if (!dpm.isAdminActive(new ComponentName(ctx, AdminReceiver.class))) {
            Log.v(TAG, "device administrator is not active");
            return !disableKG;
        }

        if (!CryptoUtils.isPasswordSaved(ctx)) {
            Log.v(TAG, "password and/or hmac not set properly");
            return !disableKG;
        }

        if (!disableKG && settings.get(Settings.WIFI_CLEAR_KEYGUARD)) {
            WifiManager wm = (WifiManager) ctx.getSystemService(
                    Context.WIFI_SERVICE);
            final WifiInfo current = wm.getConnectionInfo();
            List<String> selectedAPs = settings.get(Settings.WIFI_NETWORKS);

            if (current != null) {
                boolean hasNetworks = Sets.newHashSet(
                        selectedAPs).contains(current.getSSID());
                if (hasNetworks) {
                    Log.v(TAG, String.format( "Found networks: %s in %s",
                            current.getSSID(), selectedAPs));
                }
                disableKG |= hasNetworks;
            }
        }

        List<String> selectedDevices = settings.get(Settings.BLUETOOTH_DEVICES);
        if (!disableKG && settings.get(Settings.BT_CLEAR_KEYGUARD) &&
                selectedDevices.size() > 0) {
            List<String> connectedDevices = settings.get(
                    Settings.BLUETOOTH_CONNECTIONS);

            final Set<String> selected = Sets.newHashSet(
                    selectedDevices);
            boolean hasDevices = Iterables.tryFind(connectedDevices,
                    new Predicate<String>() {
                        @Override
                        public boolean apply(java.lang.String addr) {
                            return selected.contains(addr);
                        }
                    }).isPresent();
            if (hasDevices) {
                Log.v(TAG, String.format( "Found devices: %s in %s",
                        connectedDevices, selectedDevices));
            }
            disableKG |= hasDevices;
        }
        return !disableKG;
    }

    private long changeTime = 0;
    public void updatePasswordSetTime() {
        changeTime = System.currentTimeMillis();
    }

    // there is a window of opportunity where a settings-changed password
    // reset will mess this up
    public boolean passwordSetRecently() {
        return System.currentTimeMillis() - changeTime < 10000;
    }
}
