package com.starnet.lcdserver;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WebPostAndGet {
    private managerOnlineClient mManagerOnlineClient;
    //检查显示是否要修改
    private static final String GETADURL = "http://192.168.113.200:8080/UserManager/postContext.do";
    private static final String POSTURL = "http://192.168.113.200:8080/UserManager/getContext.do";
    private static final String TAG = "LCDServerHttpGet";
    public WebPostAndGet(managerOnlineClient managerOnlineClient){
        mManagerOnlineClient = managerOnlineClient;
    }

    /**
     * 定时POST请求
     * 通过web的返回的显示内容判断是否需修改
     * 是的话修改显示
     * */
    public void OkHttpGetTheAd(String MACStr){
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("deviceID",MACStr)
                .build();
        final Request request = new Request.Builder()
                .url(GETADURL)
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.i(TAG, "onResponse: 收到回复");
                String str = response.body().string();
                try {
                    JSONArray jsonArray = new JSONArray(str);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    String textToChange = jsonObject.getString("context");
                    //文本与显示不同则修改显示
                    if(!textToChange.equals(mManagerOnlineClient.getLCDText())){
                        mManagerOnlineClient.changeLCDText(textToChange);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }


    /**
     * 一修改就通知web服务器
     * */
    public void OkHttpPost(String MACStr){
        OkHttpClient okHttpClient = new OkHttpClient();
        RequestBody requestBody = null;
        try {
            requestBody = new FormBody.Builder()
                    .add("time",new SimpleDateFormat("yyyy-MM-dd").format(new Date()))
                    .add("deviceName",android.os.Build.DEVICE)
                    .add("deviceID",MACStr)
                    .add("context", URLEncoder.encode(mManagerOnlineClient.getLCDText(), "utf-8"))
                    .build();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder()
                .url(POSTURL)
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
            }
        });
    }
}