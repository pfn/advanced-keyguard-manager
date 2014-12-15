package com.hanhuy.android.bluetooth.keyguard;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.acra.ACRA;

public class MainActivity extends ActionBarActivity {
    private final static int DIALOG_NO_PAIRED_DEVICES = 0;
    private Settings settings;

    final static String TAG = "BluetoothKeyguardMainActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = Settings.getInstance(this);
        setContentView(R.layout.main);

        if (!getResources().getBoolean(R.bool.is_tablet)) {
            ViewPager pager = (ViewPager) findViewById(R.id.pager);
            pager.setAdapter(new PagerAdapter());
        }
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

    private BroadcastReceiver keyguardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (hasWindowFocus()) {
                NotificationManager nm = (NotificationManager) getSystemService(
                        Context.NOTIFICATION_SERVICE);
                nm.cancel(LockMediator.NOTIFICATION_TOGGLE);
            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();

        NotificationManager nm = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        nm.cancel(LockMediator.NOTIFICATION_TOGGLE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(LockMediator.ACTION_STATE_CHANGED);
        registerReceiver(keyguardReceiver, filter);

        // yuck, but it seems there's no other way to toggle menu items after
        // enabling device admin
        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(keyguardReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem setPass = menu.findItem(R.id.set_password);
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        setPass.setEnabled(dpm.isAdminActive(
                new ComponentName(this, AdminReceiver.class)));
        MenuItem showNotifications = menu.findItem(R.id.show_notifications);
        showNotifications.setChecked(settings.get(Settings.SHOW_NOTIFICATIONS));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.set_password:
                Intent setp = new Intent(this, PasswordActivity.class);
                startActivity(setp);
                return true;
            case R.id.show_notifications: {
                boolean value = !item.isChecked();
                settings.set(Settings.SHOW_NOTIFICATIONS, value);
                item.setChecked(value);
                return true;
            }
            case R.id.submit_log: {
                AlertDialog.Builder b = new AlertDialog.Builder(this);
                b.setTitle("Submit debug logs");
                b.setMessage("Add details about this log");
                final EditText edit = new EditText(this);
                b.setView(edit);
                b.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                Toast.makeText(MainActivity.this, "Debug log sent", Toast.LENGTH_SHORT).show();
                                Exception e = new Exception("User submitted log: " + edit.getText());
                                e.setStackTrace(new StackTraceElement[] {
                                        new StackTraceElement(Build.BRAND,
                                                Build.MODEL,
                                                Build.PRODUCT,
                                                (int) (System.currentTimeMillis() / 1000))
                                });
                                ACRA.getErrorReporter().handleSilentException(e);
                            }
                        }
                );
                b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int i) {
                        d.dismiss();
                    }
                });
                AlertDialog d = b.show();
                final Button button = d.getButton(DialogInterface.BUTTON_POSITIVE);
                button.setEnabled(false);
                edit.addTextChangedListener(new TextWatcher() {
                    @Override public void afterTextChanged(Editable p1) {}
                    @Override public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) { }

                    @Override public void onTextChanged(CharSequence p1,
                                                        int p2, int p3, int p4) {
                        button.setEnabled(p1.length() > 0);
                    }

                });
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
