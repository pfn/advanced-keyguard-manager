package com.hanhuy.android.bluetooth.keyguard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final static int DIALOG_NO_PAIRED_DEVICES = 0;
    
    final static String TAG = "BluetoothKeyguardMainActivity";

    private ListView listView;
    private CheckBox disableKg;

    public final static String PREF_BLUETOOTH_DEVICES = "bluetooth_devices";
    public final static String PREF_CLEAR_KEYGUARD = "clear_keyguard";
    private final static String DEVICEPICKER_ACTION =
        "android.bluetooth.devicepicker.action.LAUNCH";
    private SharedPreferences prefs;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        listView = (ListView) findViewById(R.id.paired_devices_list);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        disableKg = (CheckBox) findViewById(R.id.enable_clear_keyguard);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> devices = bt.getBondedDevices();
        
        disableKg.setChecked(prefs.getBoolean(PREF_CLEAR_KEYGUARD, false));
        
        final BluetoothDevice[] pairedDevices =
                new BluetoothDevice[devices.size()];
        devices.toArray(pairedDevices);
        if (devices.size() == 0) {
            showDialog(DIALOG_NO_PAIRED_DEVICES);
        } else {
            ArrayAdapter<BluetoothDevice> arrayAdapter =
                    new ArrayAdapter<BluetoothDevice>(this,
                            android.R.layout.simple_list_item_multiple_choice,
                            pairedDevices) {
                @Override
                public View getView(int position, View convertView,
                        ViewGroup parent) {
                    convertView = super.getView(position, convertView, parent);
                    ((TextView)convertView).setText(
                            pairedDevices[position].getName());
                    return convertView;
                }
            };
            listView.setAdapter(arrayAdapter);
            String selected = prefs.getString(PREF_BLUETOOTH_DEVICES, null);
            if (selected != null) {
                List<String> selectedList = Arrays.asList(selected.split(","));
                for (int i = 0, j = devices.size(); i < j; i++) {
                    if (selectedList.contains(
                            arrayAdapter.getItem(i).getAddress())) {
                        listView.setItemChecked(i, true);
                    }
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        boolean disable = disableKg.isChecked();
        prefs.edit().putBoolean(PREF_CLEAR_KEYGUARD, disable).commit();

        SparseBooleanArray ary = listView.getCheckedItemPositions();
        int length = ary.size();


        ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
        for (int i = 0; i < length; i++) {
            if (ary.get(i))
                devices.add((BluetoothDevice) listView.getItemAtPosition(i));
        }
        if (devices.size() == 0) {
            Toast.makeText(this, R.string.no_devices_selected,
                    Toast.LENGTH_SHORT).show();
        }
        
        StringBuilder sb = new StringBuilder();
        for (BluetoothDevice d : devices) {
            sb.append(d.getAddress());
            sb.append(",");;
        }
        if (sb.length() > 0)
            sb.setLength(sb.length() - 1);
        String oldPref = prefs.getString(PREF_BLUETOOTH_DEVICES, "");
        String pref = sb.toString();
        if (!oldPref.equals(pref)) {
            prefs.edit().putString(PREF_BLUETOOTH_DEVICES, pref).commit();
            Toast.makeText(this, R.string.toast_change_config_notice,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        if (id != DIALOG_NO_PAIRED_DEVICES)
            throw new IllegalArgumentException("Unknown dialog: " + id);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage(R.string.no_paired_devices);
        builder.setPositiveButton("Pair", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(DEVICEPICKER_ACTION));
            }
        });
        builder.setNegativeButton("Close", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        return builder.create();
    }
}
