package com.hanhuy.android.bluetooth.keyguard;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity {
    private final static int DIALOG_NO_PAIRED_DEVICES = 0;
    
    final static String TAG = "BluetoothKeyguardMainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter());
    }

    private class PagerAdapter extends FragmentPagerAdapter {

        private PagerAdapter() {
            super(getSupportFragmentManager());
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: return new OverviewFragment();
                case 1: return new WifiFragment();
                case 2: return new BluetoothFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            String title = null;
            switch (position) {
                case 0: title = getString(R.string.overview);  break;
                case 1: title = getString(R.string.wifi);      break;
                case 2: title = getString(R.string.bluetooth); break;
            }
            return title;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // yuck, but it seems there's no other way to toggle menu items after
        // enabling device admin
        supportInvalidateOptionsMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getSupportMenuInflater().inflate(R.menu.main, menu);
        MenuItem setPass = menu.findItem(R.id.set_password);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        setPass.setEnabled(dpm.isAdminActive(
                new ComponentName(this, AdminReceiver.class)));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_password:
                Intent setp = new Intent(this, PasswordActivity.class);
                startActivity(setp);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
