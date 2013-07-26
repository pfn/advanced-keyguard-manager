package com.hanhuy.android.bluetooth.keyguard;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Arrays;

public class LockOptionsFragment extends DialogFragment {
    private final String scope;
    private final String name;
    private final BaseAdapter parentAdapter;
    public LockOptionsFragment(String scope, String name, BaseAdapter adapter) {
        this.scope = scope;
        this.name = name;
        parentAdapter = adapter;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle b) {
        View v = inflater.inflate(R.layout.fragment_lock_options, c, false);
        final Settings settings = Settings.getInstance(getActivity());
        final ListView list = (ListView) v.findViewById(R.id.lock_options_list);
        getDialog().setTitle(getString(R.string.lock_options_for, name));
        @SuppressWarnings("unchecked") // stupid list creation...
        final ArrayAdapter<Settings.Setting<Boolean>> adapter =
                new ArrayAdapter<Settings.Setting<Boolean>>(getActivity(),
                        android.R.layout.simple_list_item_multiple_choice,
                        Arrays.asList(
                                Settings.REQUIRE_UNLOCK,
                                Settings.DISABLE_KEYGUARD)) {

                    @Override
                    public View getView(int pos, View cview, ViewGroup p) {
                        if (cview == null)
                            cview = super.getView(pos, cview, p);
                        TextView view = (TextView) cview;
                        int text = 0;
                        int drawable = 0;
                        if (Settings.REQUIRE_UNLOCK == getItem(pos)) {
                            text = R.string.require_unlock;
                            drawable = R.drawable.ic_unlock;
                        } else if (Settings.DISABLE_KEYGUARD == getItem(pos)) {
                            text = R.string.disable_keyguard;
                            drawable = R.drawable.ic_display;
                        }
                        view.setText(text);
                        view.setCompoundDrawablesRelativeWithIntrinsicBounds(
                                drawable, 0, 0, 0);
                        return cview;
                    }
                };
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> l, View v, int i, long id) {
                settings.set(adapter.getItem(i).prefix(scope),
                        list.isItemChecked(i));
                parentAdapter.notifyDataSetChanged();
                LockMediator.getInstance(getActivity()).notifyStateChanged();
            }
        });

        int len = adapter.getCount();
        for (int i = 0; i < len; i++) {
            Settings.Setting<Boolean> setting = adapter.getItem(i);
            list.setItemChecked(i, settings.get(setting.prefix(scope)));
        }
        return v;
    }
}