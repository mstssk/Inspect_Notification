package jp.mstssk.inspect_notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

// FIXME 何故かAppCompatActivityだとICSで起動できないのでActivityにしている
public class MainActivity extends Activity {

    static final String CHANNEL_ID = "CHANNEL_ID";
    private static final String EXTRA_ACTION_NAME = "EXTRA_ACTION_NAME";
    private static final String EXTRA_NOTIFY_ID = "EXTRA_NOTIFY_ID";
    private static final String KEY_TEXT_REPLY = "KEY_TEXT_REPLY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LinkedHashMap<String, Runnable> map = new LinkedHashMap<>();
        map.put("通常の通知", this::showNormalNotification);
        map.put("setNumber", this::showWithNumber);
        map.put("setSubText", this::showWithSubText);
        map.put("大きいアイコン", this::showLargeIcon);
        map.put("起動オプション", this::showWithDefaults);
        map.put("タップして起動", this::showWithPendingIntent);
        map.put("BigTextStyle", this::showBigTextStyle);
        map.put("BigPictureStyle", this::showBigPictureStyle);
        map.put("InboxStyle", this::showInboxStyle);
        map.put("アクション", this::showWithActions);
        map.put("ダイレクトリプライ", this::showWithDirectReply);
        map.put("タイムアウト", this::showWithTimeout);
        map.put("背景色（Foreground service）", this::startForgroundServiceWithColorizedNotification);
        List<String> list = new ArrayList<>(map.keySet());

        ListView listView = findViewById(android.R.id.list);
        listView.setAdapter(new ArrayAdapter<String>(this, android.R.layout
                .simple_list_item_1, list));
        listView.setOnItemClickListener((adapterView, view, i, l) -> map.get(list.get(i)).run());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ensureNotificationChannel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (getIntent().hasExtra(EXTRA_ACTION_NAME)) {
            String action = getIntent().getStringExtra(EXTRA_ACTION_NAME);
            int notifyId = getIntent().getIntExtra(EXTRA_NOTIFY_ID, 0);
            Toast.makeText(this, action, Toast.LENGTH_SHORT).show();
            getNotificationManager().cancel(notifyId);
        }

        Bundle remoteInput = RemoteInput.getResultsFromIntent(getIntent());
        if (remoteInput != null) {
            String reply = remoteInput.getString(KEY_TEXT_REPLY);
            int notifyId = getIntent().getIntExtra(EXTRA_NOTIFY_ID, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notifications)
                    .setContentTitle("返信しました")
                    .setContentText("「" + reply + "」")
                    .setTimeoutAfter(3000);
            Toast.makeText(this, "" + notifyId, Toast.LENGTH_SHORT).show();
            getNotificationManager().notify(notifyId, builder.build());
        }
    }

    private void showNormalNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                MainActivity.this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("ContentTitle: タイトル")
                .setContentText("ContentText: コンテンツの内容")
                .setContentInfo("ContentInfo: 情報欄")
                .setTicker("Ticker: アプリからの通知概要")
                .setColor(Color.RED)
                .setLights(Color.GREEN, 500, 500)
                .setContentIntent(contentIntent)
                .setAutoCancel(true);

        getNotificationManager().notify(10, builder.build());
    }

    private void showWithNumber() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("setNumber")
                .setContentText("setNumber(123)の通知")
                .setNumber(123);
        getNotificationManager().notify(15, builder.build());
    }

    private void showWithSubText() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("setSubText")
                .setContentText("setSubTextの通知")
                .setSubText("SubText: 補助テキスト");
        getNotificationManager().notify(17, builder.build());
    }

    private void showLargeIcon() {
        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("大きなアイコン")
                .setContentText("大きなアイコンの通知です")
                .setLargeIcon(largeIcon);
        getNotificationManager().notify(20, builder.build());
    }

    private void showWithDefaults() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("振動・音・光付き")
                .setContentText("振動・音・光付きの通知です")
                .setDefaults(NotificationCompat.DEFAULT_SOUND
                        | NotificationCompat.DEFAULT_VIBRATE
                        | NotificationCompat.DEFAULT_LIGHTS);
        getNotificationManager().notify(30, builder.build());
    }

    private void showWithPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(
                MainActivity.this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("通知からアプリを起動する")
                .setContentText("")
                .setContentIntent(contentIntent);
        getNotificationManager().notify(40, builder.build());
    }

    private void showBigTextStyle() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications);
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle(builder);
        style.setBigContentTitle("BigTextStyle");
        style.setSummaryText("BigTextStyleの通知です");
        style.bigText("コンテンツのテキスト");
        getNotificationManager().notify(50, builder.build());
    }

    private void showBigPictureStyle() {
        Bitmap pic = BitmapFactory.decodeResource(getResources(), R.drawable.bigpicture);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications);
        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle(builder);
        style.setBigContentTitle("BigPictureStyle");
        style.setSummaryText("BigPictureStyleの通知です");
        style.bigPicture(pic);
        getNotificationManager().notify(60, builder.build());
    }

    private void showInboxStyle() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications);
        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle(builder);
        style.setBigContentTitle("InboxStyle");
        style.setSummaryText("InboxStyleの通知です");
        style.addLine("複数行(1)");
        style.addLine("複数行(2)");
        style.addLine("複数行(3)");
        style.addLine("複数行(4)");
        getNotificationManager().notify(70, builder.build());
    }

    private void showWithActions() {
        final int notifyId = 80;
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("アクション")
                .setContentText("アクション付きの通知です");
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_ACTION_NAME, "Del");
            intent.putExtra(EXTRA_NOTIFY_ID, notifyId);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(android.R.drawable.ic_input_delete, "Del", pendingIntent);
        }
        {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(EXTRA_ACTION_NAME, "Add");
            intent.putExtra(EXTRA_NOTIFY_ID, notifyId);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 2,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(android.R.drawable.ic_input_add, "Add", pendingIntent);
        }
        getNotificationManager().notify(notifyId, builder.build());
    }

    private void showWithDirectReply() {
        final int notifyId = 90;
        RemoteInput remoteInput = new RemoteInput.Builder(KEY_TEXT_REPLY)
                .setLabel("RemoteInput:ReplyLabel")
                .build();
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(EXTRA_NOTIFY_ID, notifyId);
        PendingIntent replyPendingIntent = PendingIntent.getActivity(this, 3,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(android.R.drawable.ic_menu_send,
                        "Action:ReplayLabel", replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("ダイレクトリプライ")
                .setContentText("ダイレクトリプライ付きの通知です")
                .addAction(action)
                .setRemoteInputHistory(new String[]{"追加コメント"});
        getNotificationManager().notify(notifyId, builder.build());
    }

    private void showWithTimeout() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("タイムアウト")
                .setContentText("表示期限付きの通知です。5秒後に消えます")
                .setTimeoutAfter(5000);
        getNotificationManager().notify(100, builder.build());
    }

    private void startForgroundServiceWithColorizedNotification() {
        ColorizedService.start(getApplicationContext());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void ensureNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "通知チャンネル", NotificationManager.IMPORTANCE_DEFAULT);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    private NotificationManager getNotificationManager() {
        return (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
    }
}
