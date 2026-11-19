package com.alikhwa.thikrni;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    public static final String ACTION_ALARM = "com.alikhwa.thikrni.ALARM";
    public static final String ACTION_COUNT = "com.alikhwa.thikrni.COUNT";

    @Override
    public void onReceive(Context context, Intent intent) {
        String a = intent.getAction();
        if (ACTION_ALARM.equals(a)) {
            Prefs p = new Prefs(context);
            if (p.enabled()) {
                Notifier.show(context, "alarm");
                ReminderScheduler.schedule(context);
            }
        } else if (ACTION_COUNT.equals(a)) {
            new Prefs(context).incCount();
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.cancel(Notifier.ID_REMINDER);
        }
    }
}
