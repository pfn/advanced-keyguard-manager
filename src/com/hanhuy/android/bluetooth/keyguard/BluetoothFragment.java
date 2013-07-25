package com.hanhuy.android.bluetooth.keyguard;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import static com.hanhuy.android.bluetooth.keyguard.Settings.device;

public class BluetoothFragment extends Fragment {
    private final static String TAG = "BluetoothFragment";
    private final static int REQUEST_BLUETOOTH_ON = 0;
    private final static String DEVICEPICKER_ACTION =
            "android.bluetooth.devicepicker.action.LAUNCH";
    private Settings settings;
    private ListView listView;
    private CheckBox disableKg;
    private View devicesContainer;
    private View noDevicesContainer;
    private View noAdapterContainer;
    private View btDisabledContainer;
    private ArrayAdapter<BluetoothDevice> adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle b) {
        View v = inflater.inflate(R.layout.fragment_bluetooth, c, false);
        settings = Settings.getInstance(getActivity());
        listView = (ListView) v.findViewById(R.id.paired_devices_list);
        disableKg = (CheckBox) v.findViewById(R.id.enable_clear_keyguard);
        disableKg.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton c, boolean b) {
                        settings.set(Settings.BT_CLEAR_KEYGUARD, b);
                        KeyguardMediator.getInstance(
                                getActivity()).notifyStateChanged();
                        listView.setEnabled(b);
                    }
                });
        devicesContainer = v.findViewById(R.id.devices_container);
        noDevicesContainer = v.findViewById(R.id.no_devices_container);
        noAdapterContainer = v.findViewById(R.id.no_adapter_container);
        btDisabledContainer = v.findViewById(R.id.bluetooth_disabled_container);
        v.findViewById(R.id.pair_device).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(DEVICEPICKER_ACTION));
                    }
                });
        v.findViewById(R.id.enable_bluetooth).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent i = new Intent(
                                BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(i, REQUEST_BLUETOOTH_ON);
                    }
                });
        listView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(
                            AdapterView<?> a, View v, int i, long l) {
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
                            BluetoothDevice item = adapter.getItem(i);
                            String addr = item.getAddress();
                            boolean disable = !settings.get(
                                    device(addr, Settings.DISABLE_KEYGUARD));
                            settings.set(device(
                                    addr, Settings.DISABLE_KEYGUARD), disable);
                            adapter.notifyDataSetChanged();
                            KeyguardMediator.getInstance(
                                    getActivity()).notifyStateChanged();
                        }
                        return true;
                    }
                });
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        refreshDevices();
    }

    private BroadcastReceiver bluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
            if (bt.getState() == BluetoothAdapter.STATE_ON) {
                refreshDevices();
            }
        }
    };

    private void refreshDevices() {
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices;
        devices = bt != null ?
                bt.getBondedDevices() : Sets.<BluetoothDevice>newHashSet();

        disableKg.setChecked(settings.get(Settings.BT_CLEAR_KEYGUARD));
        listView.setEnabled(disableKg.isChecked());

        final BluetoothDevice[] pairedDevices =
                new BluetoothDevice[devices.size()];
        devices.toArray(pairedDevices);
        if (bt == null) {
            noAdapterContainer.setVisibility(View.VISIBLE);
            noDevicesContainer.setVisibility(View.GONE);
            devicesContainer.setVisibility(View.GONE);
            btDisabledContainer.setVisibility(View.GONE);
        } else if (!bt.isEnabled()) {
            btDisabledContainer.setVisibility(View.VISIBLE);
            noDevicesContainer.setVisibility(View.GONE);
            devicesContainer.setVisibility(View.GONE);
        } else if (devices.size() == 0) {
            noDevicesContainer.setVisibility(View.VISIBLE);
            devicesContainer.setVisibility(View.GONE);
            btDisabledContainer.setVisibility(View.GONE);
        } else {
            btDisabledContainer.setVisibility(View.GONE);
            noDevicesContainer.setVisibility(View.GONE);
            devicesContainer.setVisibility(View.VISIBLE);
            List<String> connectedList = settings.get(
                    Settings.BLUETOOTH_CONNECTIONS);
            final Set<String> connected = Sets.newHashSet(connectedList);
            adapter = new ArrayAdapter<BluetoothDevice>(getActivity(),
                    android.R.layout.simple_list_item_multiple_choice,
                    pairedDevices) {
                @Override
                public View getView(int position, View convertView,
                                    ViewGroup parent) {
                    convertView = super.getView(
                            position, convertView, parent);
                    TextView v = (TextView) convertView;
                    int drawableLeft = 0;
                    String addr = pairedDevices[position].getAddress();
                    if (settings.get(device(
                            addr, Settings.DISABLE_KEYGUARD))) {
                        drawableLeft = R.drawable.ic_lock_inverse;
                    }
                    v.setCompoundDrawablesWithIntrinsicBounds(
                            drawableLeft, 0, 0, 0);
                    v.setText(
                            pairedDevices[position].getName());
                    if (connected.contains(addr)) {
                        v.setTextColor(0xff00aa00);
                    }
                    return convertView;
                }
            };
            listView.setAdapter(adapter);
            List<String> selected = settings.get(Settings.BLUETOOTH_DEVICES);
            Set<String> selectedList = Sets.newHashSet(selected);
            for (int i = 0, j = devices.size(); i < j; i++) {
                if (selectedList.contains(adapter.getItem(i).getAddress())) {
                    listView.setItemChecked(i, true);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        getActivity().registerReceiver(bluetoothStateReceiver, filter);
        refreshDevices();
    }

    private void updateSelections() {
        SparseBooleanArray ary = listView.getCheckedItemPositions();
        int length = listView.getAdapter().getCount();

        ArrayList<BluetoothDevice> devices = Lists.newArrayList();
        for (int i = 0; i < length; i++) {
            if (ary.get(i)) {
                devices.add((BluetoothDevice) listView.getItemAtPosition(i));
            } else {
                Settings.Setting<Boolean> disablekg = device(
                        adapter.getItem(i).getAddress(),
                        Settings.DISABLE_KEYGUARD);
                boolean wasDisabled = settings.get(disablekg);
                settings.set(disablekg, false);
                if (wasDisabled)
                    adapter.notifyDataSetChanged();
            }
        }

        List<String> pref = Lists.transform(devices,
                new Function<BluetoothDevice,String>() {
                    @Override
                    public String apply(BluetoothDevice d) {
                        return d.getAddress();
                    }
                });

        List<String> oldPref = settings.get(Settings.BLUETOOTH_DEVICES);

        if (!Iterables.elementsEqual(pref, oldPref)) {
            settings.set(Settings.BLUETOOTH_DEVICES, pref);
        }
        KeyguardMediator.getInstance(getActivity()).notifyStateChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(bluetoothStateReceiver);
    }
}
