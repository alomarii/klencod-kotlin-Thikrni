package com.alikhwa.thikrni;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;

/** إظهار إشعار التذكير بالصلاة على النبي. */
public final class Notifier {
    private Notifier() {}

    public static final int ID_REMINDER = 1001;
    public static final int ID_SERVICE = 1002;
    public static final String CH_SERVICE = "thikrni_service";

    public static void ensureServiceChannel(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager nm = c.getSystemService(NotificationManager.class);
            NotificationChannel ch = new NotificationChannel(CH_SERVICE, "التشغيل في الخلفية",
                    NotificationManager.IMPORTANCE_MIN);
            ch.setShowBadge(false);
            nm.createNotificationChannel(ch);
        }
    }

    public static void show(Context c, String reason) {
        Context app = c.getApplicationContext();
        Prefs p = new Prefs(app);
        NotificationManager nm = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
        String chId = "thikrni_rem_" + (p.sound() ? "s" : "m") + (p.vibrate() ? "v" : "q");

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel ch = new NotificationChannel(chId, "تذكير الصلاة على النبي",
                    NotificationManager.IMPORTANCE_HIGH);
            ch.setShowBadge(false);
            ch.enableVibration(p.vibrate());
            if (p.vibrate()) ch.setVibrationPattern(new long[]{0, 220, 120, 220});
            if (!p.sound()) ch.setSound(null, null);
            nm.createNotificationChannel(ch);
        }

        String title;
        switch (reason) {
            case "charge":
                title = "⚡ وأنت تشحن هاتفك.. صلِّ على النبي";
                break;
            case "unlock":
                title = "🌙 أهلًا بك.. صلِّ على النبي";
                break;
            default:
                title = "🕌 حان وقت الصلاة على النبي";
        }
        String phrase = Salawat.next();
        if (HijriUtil.isFriday() && !reason.equals("test")) {
            phrase = "اليوم الجمعة، أكثروا من الصلاة على النبي صلى الله عليه وسلم";
        }

        PendingIntent open = PendingIntent.getActivity(app, 0, new Intent(app, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent countIntent = new Intent(app, ReminderReceiver.class);
        countIntent.setAction(ReminderReceiver.ACTION_COUNT);
        PendingIntent countPi = PendingIntent.getBroadcast(app, 1, countIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification.Action action = new Notification.Action.Builder(
                Icon.createWithResource(app, R.drawable.ic_stat_crescent),
                "صلّيت على النبي ✓", countPi).build();

        Notification.Builder b = Build.VERSION.SDK_INT >= 26
                ? new Notification.Builder(app, chId)
                : new Notification.Builder(app);
        b.setSmallIcon(R.drawable.ic_stat_crescent)
                .setContentTitle(title)
                .setContentText(phrase)
                .setStyle(new Notification.BigTextStyle().bigText(phrase))
                .setContentIntent(open)
                .setAutoCancel(true)
                .setColor(0xFFD4AF37)
                .setCategory(Notification.CATEGORY_REMINDER)
                .addAction(action);

        if (Build.VERSION.SDK_INT >= 26) b.setTimeoutAfter(30000);
        if (Build.VERSION.SDK_INT < 26) {
            b.setPriority(Notification.PRIORITY_HIGH);
            int defaults = 0;
            if (p.sound()) defaults |= Notification.DEFAULT_SOUND;
            if (p.vibrate()) defaults |= Notification.DEFAULT_VIBRATE;
            b.setDefaults(defaults);
        }
        try {
            nm.notify(ID_REMINDER, b.build());
        } catch (Exception ignored) {
        }
    }
}
