package com.hanhuy.android.bluetooth.keyguard;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.hanhuy.android.bluetooth.keyguard.Settings.network;

public class WifiFragment extends Fragment {
    private final static String TAG = "WifiFragment";
    private ListView listView;
    private CheckBox disableKg;
    private Settings settings;
    private View networksContainer;
    private View noNetworksContainer;
    private ArrayAdapter<WifiConfiguration> adapter;

    private final static String ACTION_WIFI_SETTINGS =
            "android.settings.WIFI_SETTINGS";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle b) {
        View v = inflater.inflate(R.layout.fragment_wifi, c, false);
        settings = Settings.getInstance(getActivity());
        listView = (ListView) v.findViewById(R.id.paired_devices_list);
        disableKg = (CheckBox) v.findViewById(R.id.enable_clear_keyguard);
        disableKg.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton c, boolean b) {
                        settings.set(Settings.WIFI_CLEAR_KEYGUARD, b);
                        LockMediator.getInstance(
                                getActivity()).notifyStateChanged();
                        listView.setEnabled(b);
                    }
                });
        networksContainer = v.findViewById(R.id.devices_container);
        noNetworksContainer = v.findViewById(R.id.no_networks_container);
        v.findViewById(R.id.add_network).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(ACTION_WIFI_SETTINGS));
                    }
                });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(
                    AdapterView<?> adapterView, View view, int i, long l) {
                updateSelections();
            }
        });
        listView.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(
                            AdapterView<?> list, View view, int i, long l) {
                        boolean isChecked = listView.isItemChecked(i);
                        if (isChecked) {
                            WifiConfiguration item = adapter.getItem(i);
                            String ssid = item.SSID;
                            if (ssid.startsWith("\"") && ssid.endsWith("\""))
                                ssid = ssid.substring(1, ssid.length() - 1);
                            DialogFragment d = new LockOptionsFragment(
                                    network(item.SSID), ssid, adapter);
                            d.show(getFragmentManager(), "LockOptions");
                        }
                        return true;
                    }
                });
        return v;
    }

    private void updateSelections() {
        SparseBooleanArray ary = listView.getCheckedItemPositions();
        int length = listView.getAdapter().getCount();

        ArrayList<WifiConfiguration> networks = Lists.newArrayList();
        for (int i = 0; i < length; i++) {
            if (ary.get(i)) {
                networks.add((WifiConfiguration) listView.getItemAtPosition(i));
            } else {
                Settings.Setting<Boolean> disablekg = network(
                        adapter.getItem(i).SSID, Settings.DISABLE_KEYGUARD);
                boolean wasDisabled = settings.get(disablekg);
                settings.set(disablekg, false);
                if (wasDisabled)
                    adapter.notifyDataSetChanged();
            }
        }

        List<String> pref = Lists.transform(networks,
                new Function<WifiConfiguration,String>() {
                    @Override
                    public String apply(WifiConfiguration d) {
                        return d.SSID;
                    }
                });

        List<String> oldPref = settings.get(Settings.WIFI_NETWORKS);
        Log.v(TAG, String.format("Size: %d, Old: [%s], new: [%s]",
                networks.size(), oldPref, pref));

        if (!Iterables.elementsEqual(pref, oldPref)) {
            settings.set(Settings.WIFI_NETWORKS, pref);
        }
        LockMediator.getInstance(getActivity()).notifyStateChanged();
    }

    private BroadcastReceiver connectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            refreshNetworks();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        refreshNetworks();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        getActivity().registerReceiver(connectivityReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(connectivityReceiver);
    }

    private void refreshNetworks() {
        WifiManager wm = (WifiManager) getActivity().getSystemService(
                Context.WIFI_SERVICE);
        final WifiInfo current = wm.getConnectionInfo();
        final List<WifiConfiguration> networks = wm.getConfiguredNetworks();

        disableKg.setChecked(settings.get(Settings.WIFI_CLEAR_KEYGUARD));
        listView.setEnabled(disableKg.isChecked());

        if (networks == null || networks.size() == 0) {
            noNetworksContainer.setVisibility(View.VISIBLE);
            networksContainer.setVisibility(View.GONE);
        } else {
            noNetworksContainer.setVisibility(View.GONE);
            networksContainer.setVisibility(View.VISIBLE);
            adapter = new ArrayAdapter<WifiConfiguration>(getActivity(),
                    android.R.layout.simple_list_item_multiple_choice,
                    networks) {
                        @Override
                        public View getView(int position, View convertView,
                                            ViewGroup parent) {
                            convertView = super.getView(
                                    position, convertView, parent);
                            String _ssid = networks.get(position).SSID;
                            String ssid = _ssid;
                            if (ssid.startsWith("\"") && ssid.endsWith("\""))
                                ssid = ssid.substring(1, ssid.length() - 1);

                            TextView v = (TextView) convertView;
                            int drawableLeft = 0;
                            boolean disableKG = settings.get(
                                    network(_ssid, Settings.DISABLE_KEYGUARD));
                            boolean requireUnlock = settings.get(
                                    network(_ssid, Settings.REQUIRE_UNLOCK));
                            if (disableKG && requireUnlock) {
                                drawableLeft = R.drawable.lock_and_keyguard;
                            } else if (disableKG) {
                                drawableLeft = R.drawable.ic_display;
                            } else if (requireUnlock) {
                                drawableLeft = R.drawable.ic_lock_inverse;
                            }
                            v.setCompoundDrawablesWithIntrinsicBounds(
                                    drawableLeft, 0, 0, 0);
                            if (current != null &&
                                    current.getSSID().equals(_ssid)) {
                                v.setTextColor(0xff00aa00);
                            }
                            v.setText(ssid);
                            return convertView;
                        }
                    };
            listView.setAdapter(adapter);
            Set<String> selected = Sets.newHashSet(
                    settings.get(Settings.WIFI_NETWORKS));
            for (int i = 0, j = networks.size(); i < j; i++) {
                if (selected.contains(adapter.getItem(i).SSID)) {
                    listView.setItemChecked(i, true);
                }
            }
        }
    }
}
