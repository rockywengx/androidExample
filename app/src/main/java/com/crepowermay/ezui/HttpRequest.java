package com.crepowermay.ezui;
import android.os.Handler;
import android.os.Looper;

import okhttp3.*;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HttpRequest {

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();

    // GET 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> get(String url, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback);
    }

    // POST 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> post(String url, String json, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback);
    }

    // PUT 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> put(String url, String json, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .put(body)
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback);
    }
    // DELETE 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> delete(String url, String json, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {
        RequestBody body = RequestBody.create(json, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(url)
                .delete(body)
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback);
    }

    // 通用的請求處理邏輯，支持成功和失敗回調
    private <T> CompletableFuture<Boolean> sendRequestWithFunction(Request request, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.err.println("Request failed: " + e.getMessage());
                if (failureCallback != null) {
                    future.complete(failureCallback.apply(e));  // 通知失敗回調，返回 false
                } else {
                    future.complete(false);  // 默認失敗返回 false
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    Class<T> clazz = null;
                    try {
                        T result = gson.fromJson(responseBody, clazz);
                        if (successCallback != null) {
                            future.complete(successCallback.apply(result));  // 通知成功回調，返回 true
                        } else {
                            future.complete(true);  // 默認成功返回 true
                        }
                    } catch (Exception e) {
                        System.err.println("Parsing failed: " + e.getMessage());
                        if (failureCallback != null) {
                            future.complete(failureCallback.apply(e));  // 通知失敗回調
                        } else {
                            future.complete(false);  // 默認失敗返回 false
                        }
                    }
                } else {
                    IOException e = new IOException("Unexpected code " + response);
                    System.err.println("Request failed: " + e.getMessage());
                    if (failureCallback != null) {
                        future.complete(failureCallback.apply(e));  // 通知失敗回調
                    } else {
                        future.complete(false);  // 默認失敗返回 false
                    }
                }
            }
        });

        return future;  // 返回 CompletableFuture<Boolean>
    }
}
