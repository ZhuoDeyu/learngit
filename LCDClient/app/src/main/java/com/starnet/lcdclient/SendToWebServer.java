package com.starnet.lcdclient;


import android.util.Log;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.ContentValues.TAG;

public class SendToWebServer {
    public static final int LOGIN_CODE = 1;
    public static final int LOGOUT_CODE = 0;
    public static final String LOGIN_URL= "http://192.168.113.200:8080/UserManager/onLine.do";
    public static final String LOGOUT_URL= "http://192.168.113.200:8080/UserManager/onLine.do";

    public static void OkHttpPost(int command,String url,String MACStr){
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("type", String.valueOf(command))
                .add("time",new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                .add("deviceName",android.os.Build.DEVICE)
                .add("deviceID",MACStr)
                .add("deviceType","0")
                .build();
        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, response.protocol() + " " +response.code() + " " + response.message());
                Headers headers = response.headers();
                for (int i = 0; i < headers.size(); i++) {
                    Log.d(TAG, headers.name(i) + ":" + headers.value(i));
                }
                Log.d(TAG, "onResponse: " + response.body().string());
            }
        });
    }

}
