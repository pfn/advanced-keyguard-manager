package com.hanhuy.android.bluetooth.keyguard;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class PasswordActivity extends SherlockFragmentActivity {
    private final static String TAG = "PasswordActivity";

    public class TestFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup c, Bundle b) {
            View v = inflater.inflate(R.layout.fragment_verify_password, c, false);
            v.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
            v.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.v(TAG, "Replaced fragment");
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new TestFragment())
                            .setTransition(
                                    FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .commit();
                }
            });
            return v;
        }
    }
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new TestFragment()).commit();
    }
}