package com.geoffreybuttercrumbs.arewethereyet;

import android.content.Context;
import android.os.PowerManager;

public class AlarmWakeLock {
    static PowerManager.WakeLock createFullWakeLock(Context context) {
        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "AreWeThereYet");
    }
}
