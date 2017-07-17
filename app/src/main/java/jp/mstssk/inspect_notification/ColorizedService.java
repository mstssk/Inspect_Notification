package jp.mstssk.inspect_notification;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

public class ColorizedService extends Service {

    private static final String ACTION_START = "jp.mstssk.inspect_notification.action.START";
    private static final String ACTION_END = "jp.mstssk.inspect_notification.action.END";


    public static void start(Context context) {
        Intent intent = new Intent(context, ColorizedService.class);
        intent.setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public ColorizedService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_START)) {
            startForeground(901, createNotification());
        } else if (intent.getAction().equals(ACTION_END)) {
            stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification createNotification() {
        Intent intent = new Intent(this, ColorizedService.class);
        intent.setAction(ACTION_END);
        PendingIntent pendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pendingIntent = PendingIntent.getForegroundService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            pendingIntent = PendingIntent.getService(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, MainActivity.CHANNEL_ID)
                .setContentTitle("背景色")
                .setContentText("背景色の付いた通知です")
                .setSmallIcon(R.drawable.ic_notifications)
                .setColorized(true)
                .setColor(Color.GREEN)
                // .setOngoing(true) // startForegroundした通知は勝手にongoingになる
                .addAction(new NotificationCompat.Action(android.R.drawable.ic_delete, "Close",
                        pendingIntent));
        return builder.build();
    }
}
