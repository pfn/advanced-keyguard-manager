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
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.Arrays;
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

            ctx.sendBroadcast(new Intent(ACTION_STATE_CHANGED));
        }
    }

    public boolean isSecurityEnabled() {
        boolean disableKG = false;
        if (!dpm.isAdminActive(new ComponentName(ctx, AdminReceiver.class))) {
            Log.v(TAG, "device administrator is not active");
            return !disableKG;
        }

        String encrypted = settings.get(Settings.PASSWORD);
        String hmac = settings.get(Settings.PASSWORD_HASH);
        if (encrypted == null || hmac == null) {
            Log.v(TAG, "password and/or hmac are null");
            return !disableKG;
        }

        String password = CryptoUtils.decrypt(encrypted);
        if (!hmac.equals(CryptoUtils.hmac(password))) {
            Log.v(TAG, "password does not match hmac");
            return !disableKG;
        }

        if (!disableKG && settings.get(Settings.WIFI_CLEAR_KEYGUARD)) {
            WifiManager wm = (WifiManager) ctx.getSystemService(
                    Context.WIFI_SERVICE);
            final WifiInfo current = wm.getConnectionInfo();
            String selectedAPs = settings.get(Settings.WIFI_NETWORKS);

            if (current != null) {
                disableKG |= Arrays.asList(
                        selectedAPs.split(",")).contains(current.getSSID());
            }
        }

        String selectedDevices = settings.get(Settings.BLUETOOTH_DEVICES);
        if (!disableKG && settings.get(Settings.BT_CLEAR_KEYGUARD) &&
                selectedDevices != null) {
            String connectedDevices = settings.get(
                    Settings.BLUETOOTH_CONNECTIONS);

            final List<String> connected = Lists.newArrayList(
                    (connectedDevices == null ?
                            "" : connectedDevices).split(","));
            final Set<String> selected = Sets.newHashSet(
                    selectedDevices.split(","));
            disableKG |= null != Iterables.find(connected, new Predicate<String>() {
                @Override
                public boolean apply(java.lang.String addr) {
                    return selected.contains(addr);
                }
            });
        }
        return !disableKG;
    }
}
