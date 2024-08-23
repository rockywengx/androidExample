package com.crepowermay.ezui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.WindowManager;

public class Message {

    private final AlertDialog dialog;
    private final Handler handler = new Handler(Looper.getMainLooper());

    /**
     * 訊息視窗
     * @param context 上下文
     */
    public Message(Context context) {
        this(context, 3000);
    }

    /**
     * 訊息視窗
     * @param context 上下文
     * @param autoCloseMillis 自動關閉時間（毫秒） 默認 3000 毫秒
     */
    public Message(Context context, int autoCloseMillis) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            // 設置對話框在視窗上方出現
            WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
            params.gravity = Gravity.TOP;
            dialog.getWindow().setAttributes(params);

            // 自動關閉對話框
            handler.postDelayed(dialog::dismiss, autoCloseMillis);
        });
    }

    public void show(String title, String message) {
        handler.post(() -> {
            if (dialog == null) {
                return;
            }
            dialog.setTitle(title);
            dialog.setMessage(message);
            dialog.show();
        });
    }

    public void hide() {
        handler.post(() -> {
            if (dialog == null) {
                return;
            }
            dialog.hide();
        });
    }
}
