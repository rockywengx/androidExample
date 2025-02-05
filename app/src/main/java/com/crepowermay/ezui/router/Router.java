package com.crepowermay.ezui.router;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.crepowermay.ezui.Confirm;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class Router {


    private static Class<?> previousClass;

    public static Class<?> getPreviousClass() {
        return previousClass;
    }

    @SuppressLint("StaticFieldLeak")
    private static ViewGroup previousViewGroup;

    public static Class<?> getPreviousViewGroup() {
        return previousViewGroup.getClass();
    }

    private static Fragment previousFragment;

    public static Class<?> getPreviousFragment() {
        return previousFragment.getClass();
    }

    private static boolean isPreviousClear;

    private static boolean isPreviousFinish;


    /**
     * 跳轉至指定頁面
     * @param context 上下文
     * @param cls 目標頁面
     */
    public static void to(Context context, Class<?> cls) {
        previousClass = context.getClass();
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
        isPreviousFinish = false;
    }

    public static void to(Context context, Class<?> cls, boolean shouldFinishCurrent) {
        previousClass = context.getClass();
        Intent intent = new Intent(context, cls);
        context.startActivity(intent);
        if (shouldFinishCurrent && context instanceof Activity) {
            ((Activity) context).finish();
            isPreviousFinish = shouldFinishCurrent;
        }
    }


    /**
     * 跳轉至指定頁面
     * @param context 上下文
     * @param cls 目標頁面
     * @param params 參數
     * @param shouldFinishCurrent 控制是否關閉當前頁面
     * @param <T>
     */
    public static <T extends Serializable> void to(
        Context context,
        Class<?> cls,
        T params,
        boolean shouldFinishCurrent
    ) {
        previousClass = context.getClass();
        Intent intent = new Intent(context, cls);
        intent.putExtra("params", params);
        context.startActivity(intent);

        if (shouldFinishCurrent && context instanceof Activity) {
            ((Activity) context).finish();
            isPreviousFinish = shouldFinishCurrent;
        }
    }

    public static void to(
            Context context,
            Class<?> cls,
            Map<String, Object> params,
            boolean shouldFinishCurrent
    ) {
        previousClass = context.getClass();
        Intent intent = new Intent(context, cls);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (entry.getValue() instanceof String) {
                intent.putExtra(entry.getKey(), (String) entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                intent.putExtra(entry.getKey(), (Integer) entry.getValue());
            } else if (entry.getValue() instanceof Boolean) {
                intent.putExtra(entry.getKey(), (Boolean) entry.getValue());
            } else if (entry.getValue() instanceof Float) {
                intent.putExtra(entry.getKey(), (Float) entry.getValue());
            } else if (entry.getValue() instanceof Double) {
                intent.putExtra(entry.getKey(), (Double) entry.getValue());
            } else if (entry.getValue() instanceof Serializable) {
                intent.putExtra(entry.getKey(), (Serializable) entry.getValue());
            }
        }
        context.startActivity(intent);

        if (shouldFinishCurrent && context instanceof Activity) {
            ((Activity) context).finish();
            isPreviousFinish = shouldFinishCurrent;
        }
    }


    /**
     * 返回上一頁
     * @param context 上下文
     */
    public static void back(Context context) {
        if (context instanceof Activity) {
            if (isPreviousFinish) {
                Intent intent = new Intent(context, previousClass);
                context.startActivity(intent);
            } else {
                if(previousClass == null){
                    Confirm confirm = new Confirm(context, "提示", "是否要離開應用程式？",
                            ()->{
                                ((Activity) context).finish();
                            },
                            ()->{

                            });
                    confirm.show();
                } else {
                    ((Activity) context).finish();
                }
            }
        }
    }

    public static void back(Context context, boolean shouldFinishCurrent) {
        if (context instanceof Activity) {
            if (isPreviousFinish) {
                Intent intent = new Intent(context, previousClass);
                context.startActivity(intent);
                if (shouldFinishCurrent) {
                    ((Activity) context).finish();
                }
            } else {
                if(previousClass == null){
                    Confirm confirm = new Confirm(context, "提示", "是否要離開應用程式？",
                        ()->{
                            ((Activity) context).finish();
                        },
                        ()->{

                        });
                    confirm.show();
                } else {
                    ((Activity) context).finish();
                }
            }
        }
    }

    /**
     * 替換指定 ViewGroup 中的 Fragment
     * @param viewGroup 目標 ViewGroup
     * @param fragment 目標 Fragment
     */
    public static void replaceFragment(ViewGroup viewGroup, Fragment fragment, boolean isClear) {
        Context context = viewGroup.getContext();
        if (context instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            Fragment currentFragment = fragmentManager.findFragmentById(viewGroup.getId());
            if (currentFragment != null) {
                previousFragment = currentFragment;
                previousViewGroup = viewGroup;
            }

            if (isClear) {
                fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                isPreviousClear = true;
            } else {
                isPreviousClear = false;
            }

            transaction.replace(viewGroup.getId(), fragment);
            transaction.addToBackStack(null); // 添加到返回棧
            transaction.commit();
        } else {
            throw new IllegalStateException("Context must be an instance of FragmentActivity");
        }
    }
    /**
     * 返回上一個 Fragment
     * @param context 上下文
     */
    public static void backFragment(Context context) {
        if (context instanceof FragmentActivity) {
            FragmentManager fragmentManager = ((FragmentActivity) context).getSupportFragmentManager();
            if (fragmentManager.getBackStackEntryCount() > 0) {
                if (isPreviousClear && previousFragment != null) {
                    replaceFragment(previousViewGroup, previousFragment, false);
                } else {
                    fragmentManager.popBackStack();
                }
            } else {
                throw new IllegalStateException("No fragments in back stack");
            }
        } else {
            throw new IllegalStateException("Context must be an instance of FragmentActivity");
        }
    }
}
