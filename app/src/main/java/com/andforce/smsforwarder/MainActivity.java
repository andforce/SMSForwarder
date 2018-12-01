package com.andforce.smsforwarder;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "SMSForwarder";
    private SMSContentObserver mSMSContentObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.send_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "Start Push");
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
                                Log.d(TAG, "Push: " + result);
                                if (!TextUtils.isEmpty(result)) {
                                    if (result.contains("objectId")) {
                                        // success
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            }
        });

        mSMSContentObserver = new SMSContentObserver(mHandler, this.getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Checks if the specified context can draw on top of other apps. As of API
            // level 23, an app cannot draw on top of other apps unless it declares the
            // {@link android.Manifest.permission#SYSTEM_ALERT_WINDOW} permission in its
            // manifest, <em>and</em> the user specifically grants the app this
            // capability. To prompt the user to grant this approval, the app must send an
            // intent with the action
            // {@link android.provider.Settings#ACTION_MANAGE_OVERLAY_PERMISSION}, which
            // causes the system to display a permission management screen.
            if (Settings.canDrawOverlays(MainActivity.this)) {
                Toast.makeText(MainActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ForwardService.class);
                startService(intent);
                //finish();
            } else {
                //若没有权限，提示获取.
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                Toast.makeText(MainActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
                startActivity(intent);
            }

            List<String> failedPermissions = PermissionUtils.checkPermissions(this);
            if (failedPermissions.isEmpty()){
                // 所有的权限都获取到了
                getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mSMSContentObserver);
            } else {
                ActivityCompat.requestPermissions(this, PermissionUtils.sPermissions, 0x001);
            }
        } else {
            getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mSMSContentObserver);
            //SDK在23以下，不用管.
            Intent intent = new Intent(MainActivity.this, ForwardService.class);
            startService(intent);
            //finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 0x001){
            List<String> failed = PermissionUtils.checkPermissions(this, permissions, grantResults);

            if (!failed.isEmpty()){
                Toast.makeText(MainActivity.this, "权限不全", Toast.LENGTH_LONG).show();
            } else {
                getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mSMSContentObserver);
            }
        }
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "Sms received");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent defIntent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
        defIntent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, this.getPackageName());
        startActivity(defIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        getContentResolver().unregisterContentObserver(mSMSContentObserver);
    }

}
