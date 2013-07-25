package com.hanhuy.android.bluetooth.keyguard;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PasswordActivity extends ActionBarActivity {
    private final static String TAG = "PasswordActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fragment start = CryptoUtils.isPasswordSaved(this) ?
                new Verify() : new New();
        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, start).commit();
    }

    public static class Verify extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle b) {
            final Handler handler = new Handler();
            View v = inf.inflate(R.layout.fragment_password, c, false);
            v.findViewById(R.id.cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                        }
                    });
            final View next = v.findViewById(R.id.next);
            final TextView warning = (TextView) v.findViewById(R.id.warning);
            warning.setText(R.string.try_again);
            TextView info = (TextView) v.findViewById(R.id.password_info);
            final boolean isPIN = CryptoUtils.isPIN(getActivity());
            v.findViewById(R.id.pin_password_selection).setVisibility(
                    View.INVISIBLE);
            info.setText(getString(R.string.verify_pin_password,
                    getString(isPIN ?
                            R.string.pin : R.string.password)));
            final TextView field = (TextView) v.findViewById(
                    R.id.password_field);
            field.requestFocus();
            if (isPIN)
                setPINEntry(field);
            else
                setPasswordEntry(field);

            field.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence cs, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(
                        CharSequence cs, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int len = editable.length();
                    next.setEnabled(len > 0);
                }
            });
            final Runnable warningHider = new Runnable() {

                @Override
                public void run() {
                    warning.setVisibility(View.INVISIBLE);
                }
            };
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String entered = field.getText().toString();
                    if (!CryptoUtils.verifyPassword(getActivity(), entered)) {
                        warning.setVisibility(View.VISIBLE);
                        handler.removeCallbacks(warningHider);
                        handler.postDelayed(warningHider, 5000);
                        field.setText("");
                    } else {
                        warning.setVisibility(View.INVISIBLE);
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                .replace(android.R.id.content, new New())
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                    }
                }
            });
            return v;
        }
    }

    public static class New extends Fragment {
        private TextView field;
        private TextView warning;
        private RadioGroup typeSelection;
        private boolean isPIN = true;
        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle b) {
            View v = inf.inflate(R.layout.fragment_password, c, false);
            final View next = v.findViewById(R.id.next);
            v.findViewById(R.id.cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                        }
                    });
            typeSelection = (RadioGroup) v.findViewById(
                    R.id.pin_password_selection);
            typeSelection.setOnCheckedChangeListener(
                    new RadioGroup.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(RadioGroup g, int i) {
                            switch (i) {
                                case R.id.pin:
                                    setPINEntry(field);
                                    warning.setText(R.string.pin_minimum);
                                    isPIN = true;
                                    break;
                                case R.id.password:
                                    setPasswordEntry(field);
                                    warning.setText(R.string.password_minimum);
                                    isPIN = false;
                                    break;
                            }
                        }
                    });
            warning = (TextView) v.findViewById(R.id.warning);
            field = (TextView) v.findViewById(R.id.password_field);
            field.requestFocus();
            field.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence cs, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(
                        CharSequence cs, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int len = editable.length();
                    warning.setVisibility(len > 0 && len < 4 ?
                            View.VISIBLE : View.INVISIBLE);

                    next.setEnabled(len >= 4);
                    for (int i = 0; i < typeSelection.getChildCount(); i++) {
                        typeSelection.getChildAt(i).setEnabled(len == 0);
                    }
                    typeSelection.setEnabled(len == 0);
                }
            });
            setPINEntry(field);
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    nextStep();
                }
            });
            return v;
        }

        private void nextStep() {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new Confirm(
                            isPIN, field.getText().toString()))
                    .setTransition(
                            FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    public static class Confirm extends Fragment {
        private final boolean isPIN;
        private final String password;
        Confirm(boolean isPIN, String password) {
            this.isPIN = isPIN;
            this.password = password;
        }

        @Override
        public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle b) {
            View v = inf.inflate(R.layout.fragment_password, c, false);
            v.findViewById(R.id.cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            getActivity().finish();
                        }
                    });
            final View next = v.findViewById(R.id.next);
            v.findViewById(R.id.pin_password_selection).setVisibility(
                    View.INVISIBLE);
            TextView info = (TextView) v.findViewById(R.id.password_info);
            info.setText(getString(R.string.confirm_pin_password,
                    getString(isPIN ? R.string.pin : R.string.password)));
            final TextView warning = (TextView) v.findViewById(R.id.warning);
            warning.setText(R.string.try_again);
            final TextView field = (TextView) v.findViewById(
                    R.id.password_field);
            field.requestFocus();
            if (isPIN)
                setPINEntry(field);
            else
                setPasswordEntry(field);

            field.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(
                        CharSequence cs, int i, int i2, int i3) {
                }

                @Override
                public void onTextChanged(
                        CharSequence cs, int i, int i2, int i3) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                    int len = editable.length();
                    next.setEnabled(len > 0);
                }
            });
            final Handler handler = new Handler();
            final Runnable warningHider = new Runnable() {
                @Override
                public void run() {
                    warning.setVisibility(View.INVISIBLE);
                }
            };
            next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String entered = field.getText().toString();
                    if (!password.equals(entered)) {
                        warning.setVisibility(View.VISIBLE);
                        handler.removeCallbacks(warningHider);
                        handler.postDelayed(warningHider, 5000);
                        field.setText("");
                    } else {
                        Settings s = Settings.getInstance(getActivity());
                        s.set(Settings.PASSWORD, CryptoUtils.encrypt(password));
                        s.set(Settings.PASSWORD_HASH,
                                CryptoUtils.hmac(password));
                        DevicePolicyManager dpm =
                                (DevicePolicyManager) getActivity()
                                        .getSystemService(
                                                Context.DEVICE_POLICY_SERVICE);
                        LockMediator kgm =
                                LockMediator.getInstance(getActivity());
                        kgm.updatePasswordSetTime();
                        dpm.resetPassword(password, 0);
                        kgm.notifyStateChanged();
                        Toast.makeText(getActivity(), R.string.password_changed,
                                Toast.LENGTH_SHORT).show();
                        getActivity().finish();
                    }
                }
            });
            return v;
        }
    }

    static void setPasswordEntry(TextView field) {
        field.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
    }
    static void setPINEntry(TextView field) {
        if (Build.VERSION.SDK_INT < 11) {
            field.setInputType(InputType.TYPE_CLASS_NUMBER);
            field.setTransformationMethod(
                    PasswordTransformationMethod.getInstance());
        } else
            field.setInputType(InputType.TYPE_CLASS_NUMBER |
                    InputType.TYPE_NUMBER_VARIATION_PASSWORD);
    }
}