package vn.edu.ueh.ngocha.squiditytempprj.View.notification;

import vn.edu.ueh.ngocha.squiditytempprj.View.util.Str;
import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import vn.edu.ueh.ngocha.squiditytempprj.Model.entity.Notification;
import vn.edu.ueh.ngocha.squiditytempprj.R;
import vn.edu.ueh.ngocha.squiditytempprj.View.activity.MainActivity;

/**
 * Notification Channel của Squidity và hàm đẩy thông báo ra thanh thông báo hệ thống.
 * Làm theo bài 8.1 Notifications: tạo NotificationChannel, dựng thông báo bằng
 * NotificationCompat.Builder, gửi bằng NotificationManager.notify().
 */
public class NtfChannel {

    public static final String CHANNEL_ID = "squidity_notifications";
    public static final String CHANNEL_NAME = "Squidity notifications";

    private NtfChannel() {
    }

    // Gọi một lần trong onCreate của MainActivity
    public static void setup(AppCompatActivity activity) {
        createChannel(activity);
        requestPermissionIfNeeded(activity);
    }

    // Từ Android 8.0 (API 26) mọi thông báo phải thuộc một Channel
    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(Str.t(R.string.notif_channel_desc));

            NotificationManager notifyManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (notifyManager != null) {
                notifyManager.createNotificationChannel(notificationChannel);
            }
        }
    }

    // Từ Android 13 (API 33) phải xin quyền POST_NOTIFICATIONS khi chạy (slide chưa có phần này)
    private static void requestPermissionIfNeeded(AppCompatActivity activity) {
        if (hasPermission(activity)) {
            return;
        }
        ActivityResultLauncher<String> launcher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                new ActivityResultCallback<Boolean>() {
                    @Override
                    public void onActivityResult(Boolean granted) {
                        // Từ chối thì vẫn xem được thông báo trong popup của app
                    }
                });
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS);
    }

    public static boolean hasPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true;
        }
        return ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Đẩy một thông báo ra thanh thông báo. Chạm vào thì mở lại app
    public static void show(Context context, Notification notification) {
        if (!hasPermission(context)) {
            return;
        }

        // Bước 1: Intent mở MainActivity
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Bước 2: bọc vào PendingIntent. FLAG_IMMUTABLE bắt buộc từ Android 12 (slide cũ chỉ ghi FLAG_UPDATE_CURRENT)
        int notificationId = notification.getNotifId().hashCode();
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                context, notificationId, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Bước 3: dựng thông báo bằng NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(notification.getContent())
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getContent()))
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Bước 4: gửi bằng NotificationManager.notify()
        NotificationManager notifyManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notifyManager != null) {
            notifyManager.notify(notificationId, builder.build());
        }
    }
}