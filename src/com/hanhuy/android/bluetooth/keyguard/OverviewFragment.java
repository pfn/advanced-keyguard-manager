package com.hanhuy.android.bluetooth.keyguard;

import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;

public class OverviewFragment extends Fragment {
    private CompoundButton toggle;
    private DevicePolicyManager dpm;
    private ComponentName cn;
    private Settings settings;
    private TextView lockscreenStatus;
    private TextView pinPasswordStatus;
    private View warning;
    private View disabledWarning;
    private TextView keyguardStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle s) {
        settings = Settings.getInstance(getActivity());
        View v = inflater.inflate(R.layout.fragment_overview, c, false);
        cn = new ComponentName(getActivity(), AdminReceiver.class);
        dpm = (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        toggle = (CompoundButton) v.findViewById(R.id.toggle_admin);
        lockscreenStatus = (TextView) v.findViewById(R.id.lockscreen_status);
        pinPasswordStatus = (TextView) v.findViewById(R.id.pin_password_status);
        keyguardStatus = (TextView) v.findViewById(R.id.keyguard_status);
        warning = v.findViewById(R.id.warning);
        disabledWarning = v.findViewById(R.id.disabled_warning);

        if (hasPermanentMenuKey()) {
            v.findViewById(
                    R.id.press_menu_for_more).setVisibility(View.VISIBLE);
        }
        toggle.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton c, boolean b) {
                        Intent addAdmin = new Intent(
                                DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        if (!b) {
                            Context ctx = getActivity();
                            if (settings.get(Settings.LOCK_DISABLED) &&
                                    CryptoUtils.isPasswordSaved(ctx)) {
                                dpm.resetPassword(
                                        CryptoUtils.getPassword(ctx), 0);
                                settings.set(Settings.LOCK_DISABLED, false);
                            }
                            dpm.removeActiveAdmin(cn);
                            disabledWarning.setVisibility(View.VISIBLE);
                            warning.setVisibility(View.GONE);

                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {
                                    getActivity().supportInvalidateOptionsMenu();
                                }
                            });
                        }
                        else {
                            addAdmin.putExtra(
                                    DevicePolicyManager.EXTRA_DEVICE_ADMIN, cn);
                            addAdmin.putExtra(
                                    DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                                    getActivity().getString(
                                            R.string.admin_add_explanation));
                            startActivity(addAdmin);
                        }
                    }
                });
        return v;
    }

    private BroadcastReceiver keyguardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
        IntentFilter filter = new IntentFilter();
        filter.addAction(KeyguardMediator.ACTION_STATE_CHANGED);
        getActivity().registerReceiver(keyguardReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(keyguardReceiver);
    }

    private void updateUI() {
        boolean isActive = dpm.isAdminActive(cn);
        disabledWarning.setVisibility(isActive ? View.GONE : View.VISIBLE);
        warning.setVisibility(isActive && areOtherAdminsSet() ?
                View.VISIBLE : View.GONE);
        toggle.setChecked(isActive);
        String encrypted = settings.get(Settings.PASSWORD);

        if (!CryptoUtils.isPasswordSaved(getActivity())) {
            pinPasswordStatus.setText(R.string.unset);
            pinPasswordStatus.setTextColor(0xffff0000);
        } else {
            boolean isPIN = CryptoUtils.isPIN(getActivity());
            pinPasswordStatus.setText(isPIN ? R.string.pin : R.string.password);
            pinPasswordStatus.setTextColor(0xff00aa00);

        }
        KeyguardMediator kgm = KeyguardMediator.getInstance(getActivity());
        boolean isSecure = kgm.isSecurityEnabled() || !dpm.isAdminActive(cn);

        lockscreenStatus.setText(isSecure ?
                R.string.enabled : R.string.bypassed);
        lockscreenStatus.setTextColor(
                isSecure ? 0xff00aa00 : 0xff770000);

        keyguardStatus.setText(true ?
            R.string.enabled : R.string.bypassed);
        keyguardStatus.setTextColor(
                true ? 0xff00aa00 : 0xff770000);
    }

    private boolean areOtherAdminsSet() {
        List<ComponentName> admins = dpm.getActiveAdmins();
        return Iterables.tryFind(admins, new Predicate<ComponentName>() {
            @Override
            public boolean apply(android.content.ComponentName c) {
                return !cn.equals(c) && dpm.hasGrantedPolicy(
                        c, DeviceAdminInfo.USES_POLICY_LIMIT_PASSWORD);
            }
        }).isPresent();
    }
    private boolean hasPermanentMenuKey() {
        int version = Build.VERSION.SDK_INT;
        if (version < Build.VERSION_CODES.HONEYCOMB) {
            return true;
        } else if (version >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            return ViewConfiguration.get(getActivity()).hasPermanentMenuKey();
        } else {
            // must be honeycomb
            return false;
        }
    }
}
