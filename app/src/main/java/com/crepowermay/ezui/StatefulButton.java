package com.crepowermay.ezui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@SuppressLint("ViewConstructor")
public class StatefulButton extends RelativeLayout {

    private final UiStyle uiStyle;
    private final Button button;
    private final ProgressBar progressBar;
    private final AtomicBoolean isLoading = new AtomicBoolean(false);
    // onClickEvent
    private Function<View, CompletableFuture<Boolean>> action; // 宣告 action 屬性
    private int timeoutMillis = 100000; // 設定 timeout 時間
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public StatefulButton(Context context) {
        super(context);
        this.uiStyle = new UiStyle();
        this.button = new Button(context);
        this.progressBar = new ProgressBar(context);
        init();
    }

    public StatefulButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.uiStyle = new UiStyle();
        this.button = new Button(context, attrs);
        this.progressBar = new ProgressBar(context, attrs);
        init();
    }

    public StatefulButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.uiStyle = new UiStyle();
        this.button = new Button(context, attrs, defStyleAttr);
        this.progressBar = new ProgressBar(context, attrs, defStyleAttr);
        init();
    }

    public StatefulButton(Context context, AttributeSet attrs, int defStyleAttr, UiStyle uiStyle) {
        super(context, attrs, defStyleAttr);
        this.uiStyle = uiStyle;
        this.button = new Button(context, attrs, defStyleAttr);
        this.progressBar = new ProgressBar(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化按鈕
        button.setBackgroundColor(uiStyle.normalBackgroundColor);
        button.setTextColor(uiStyle.normalTextColor);
        button.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(button);

        // 初始化進度條
        progressBar.setVisibility(View.GONE);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        addView(progressBar);

        // 設置佈局
        RelativeLayout.LayoutParams buttonParams = (RelativeLayout.LayoutParams) button.getLayoutParams();
        buttonParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        button.setLayoutParams(buttonParams);

        RelativeLayout.LayoutParams progressBarParams = (RelativeLayout.LayoutParams) progressBar.getLayoutParams();
        progressBarParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        progressBar.setLayoutParams(progressBarParams);
    }

    public void setButtonLayoutParams(RelativeLayout.LayoutParams params) {
        button.setLayoutParams(params);
    }

    public void setProgressBarLayoutParams(RelativeLayout.LayoutParams params) {
        progressBar.setLayoutParams(params);
    }

    public void setLoading(boolean loading) {
        isLoading.set(loading);
        if (loading) {
            button.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            setEnabled(false);
        } else {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                button.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                setEnabled(true);
            }, 500); // 延遲半秒解鎖
        }
    }

    public boolean isLoading() {
        return isLoading.get();
    }

    public void setText(CharSequence text) {
        button.setText(text);
    }

    /**
     * 長運算用
     * 設定 action
     * @param action
     */
    public void setAction(Function<View, CompletableFuture<Boolean>> action) {
        this.action = action; // 設定 action
    }

    public void setOnClickListener(OnClickListener listener) {
        button.setOnClickListener(v -> {
            if (isLoading.compareAndSet(false, true)) {
                setLoading(true);

                Handler timeoutHandler = new Handler(Looper.getMainLooper());
                Runnable timeoutRunnable = () -> setLoading(false);

                if (action != null) {
                    action.apply(v)
                            .thenAccept(success -> {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    if (success) {
                                        listener.onClick(v);
                                    }
                                    setLoading(false);
                                });
                            }).exceptionally(e -> {
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    System.err.println("Action failed: " + e.getMessage());
                                    setLoading(false);
                                });
                                return null;
                            });
                } else {
                    new Thread(() -> {
                        listener.onClick(v);
                        new Handler(Looper.getMainLooper()).post(() -> setLoading(false)); // 確保在主線程中執行
                    }).start();
                }

                //timeoutHandler.postDelayed(timeoutRunnable, timeoutMillis); // 設定 timeout
            }
        });
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        button.setEnabled(enabled);
        if (enabled) {
            button.setBackgroundColor(uiStyle.normalBackgroundColor);
            button.setTextColor(uiStyle.normalTextColor);
        } else {
            button.setBackgroundColor(uiStyle.disabledBackgroundColor);
            button.setTextColor(uiStyle.disabledTextColor);
        }
    }

}
