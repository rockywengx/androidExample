package com.crepowermay.ezui;
import android.os.Handler;
import android.os.Looper;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public class HttpRequest {

    private final OkHttpClient client = new OkHttpClient();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Gson gson = new Gson();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    // GET 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> get(Class<T> clazz, String url, Map<String, Object> params, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(url).newBuilder();
        if (params != null) {
            for (Map.Entry<String, Object> param : params.entrySet()) {
                // 处理查询参数（假设所有传入的参数都是查询参数）
                urlBuilder.addQueryParameter(param.getKey(), param.getValue().toString());
            }
        }

        // 构建最终请求URL
        HttpUrl finalUrl = urlBuilder.build();

        // 创建请求对象
        Request request = new Request.Builder()
                .url(finalUrl)
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback, clazz);
    }

    // POST 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> post(Class<T> clazz, String url, Map<String, Object> params, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {
        // 将请求体对象序列化为JSON
        String jsonBody = gson.toJson(params);

        // 构建请求体
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        // 创建请求对象
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback, clazz);
    }

    // PUT 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> put(Class<T> clazz, String url, Map<String, Object> params, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {

        // 将请求体对象序列化为JSON
        String jsonBody = gson.toJson(params);

        // 构建请求体
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .put(requestBody)
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback, clazz);
    }
    // DELETE 方法，接受外部傳入的 Function 作為回調
    public <T> CompletableFuture<Boolean> delete(Class<T> clazz, String url, Map<String, Object> params, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback) {
        // 将请求体对象序列化为JSON
        String jsonBody = gson.toJson(params);

        // 构建请求体
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(url)
                .delete(requestBody)
                .build();

        return sendRequestWithFunction(request, successCallback, failureCallback, clazz);
    }

    /**
     * 錯誤處理
     * @param call
     * @param e
     */
    public void handleFailure(Call call, IOException e) {
        System.err.println("Request failed: " + e.getMessage());
    }

    // 通用的請求處理邏輯，支持成功和失敗回調
    private <T> CompletableFuture<Boolean> sendRequestWithFunction(Request request, Function<T, Boolean> successCallback, Function<Throwable, Boolean> failureCallback, Class<T> clazz) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                handleFailure(call, e);
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
                    String responseBody = null;
                    if (response.body() != null) {
                        responseBody = response.body().string();
                    }
                    try {
                        JsonReader reader = new JsonReader(new StringReader(responseBody));
                        reader.setLenient(true);
                        T result = gson.fromJson(reader, clazz);

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
