package com.andforce.smsforwarder.test;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TestSendPush {

    public static void send(final Context context){
        final String messageJson = "{\"data\":{\"aps\":{\"alert\":{\"body\":\"888888\",\"title\":\"+8615313726078\"}," +
                "\"badge\":\"Increment\",\"sms\":{\"address\":\"+8615313726078\",\"body\":\"888888\",\"date\":\"1543672750000\",\"id\":\"1064\",\"person\":\"\",\"protocol\":\"0\",\"read\":\"0\",\"simId\":\"-1\",\"type\":\"1\"},\"sound\":\"default\"}},\"prod\":\"dev\",\"where\":{\"deviceType\":\"ios\"}}";
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();

                MediaType mediaType = MediaType.parse("application/json");
                RequestBody body = RequestBody.create(mediaType, messageJson);
                Request request = new Request.Builder()
                        .url("https://6g0ounut.push.lncld.net/1.1/push")
                        .post(body)
                        .addHeader("content-type", "application/json")
                        .addHeader("x-lc-id", "6G0ouNUTlUdRYE2ARgwAhiJM-gzGzoHsz")
                        .addHeader("x-lc-key", "nGciHu1gAOH8A93ja2i9OOVY,wL3ha2OcwKHbBMYTULnhp9bY")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    if (response.body() != null) {
                        String result = response.body().string();
                        if (!TextUtils.isEmpty(result)) {
                            Looper.prepare();
                            if (result.contains("objectId")) {
                                // success
                                Toast.makeText(context, "发送成功", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "发送失败", Toast.LENGTH_SHORT).show();
                            }
                            Looper.loop();
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
