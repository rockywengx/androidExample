package com.crepowermay.ezui;

import android.app.AlertDialog;
import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;

public class Confirm {

    private final AlertDialog dialog;
    private final AtomicBoolean isDialogShowing = new AtomicBoolean(false);

    /**
     * 確認視窗
     * @param context 上下文
     * @param title 標題
     * @param message 訊息
     * @param onConfirm 確認後執行
     * @param onCancel 取消後執行
     */
    public Confirm(Context context, String title, String message, Runnable onConfirm, Runnable onCancel) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            if (onConfirm != null) {
                onConfirm.run();
            }
            isDialogShowing.set(false);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (onCancel != null) {
                onCancel.run();
            }
            isDialogShowing.set(false);
        });
        dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false); // 禁止點擊對話框外部關閉
    }

    public void invoke(boolean isShow) {
        if (isShow) {
            show();
        } else {
            hide();
        }
    }

    public void show() {
        if (dialog == null || isDialogShowing.get()) {
            return;
        }
        dialog.show();
        isDialogShowing.set(true);
    }

    public void hide() {
        if (dialog == null) {
            return;
        }
        dialog.hide();
        isDialogShowing.set(false);
    }

}
