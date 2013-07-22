package com.hanhuy.android.bluetooth.keyguard;

import java.io.File;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings;

public class LockPatternUtil {
    final static String LOCK_PATTERN_FILE_NAME = "/system/gesture.key";
    final static File LOCK_PATTERN_FILE;
    private LockPatternUtil() { }
    static {
        LOCK_PATTERN_FILE = new File(
                Environment.getDataDirectory().getAbsoluteFile(),
                LOCK_PATTERN_FILE_NAME);
    }
    
    public static boolean hasLockPattern() {
        return LOCK_PATTERN_FILE.exists() && LOCK_PATTERN_FILE.length() > 0;
    }
    
    public static void setLockPatternEnabled(Context c, boolean enabled) {
        if (!enabled || hasLockPattern()) {
            ContentResolver cr = c.getContentResolver();
            Settings.System.putInt(cr,
                    Settings.System.LOCK_PATTERN_ENABLED, enabled ? 1 : 0);
        }
    }
}
