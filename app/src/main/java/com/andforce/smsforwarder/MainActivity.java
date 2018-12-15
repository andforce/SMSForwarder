package com.andforce.smsforwarder;

import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.andforce.smsforwarder.test.TestSendPush;
import com.andforce.smsforwarder.utils.Manufacturer;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static String TAG = "SMSForwarder";
    private SMSContentObserver mSMSContentObserver;

    private boolean isWantReplace = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (defaultSmsReplaced()){
            // 短信应用已经被替换成 有消息，不需要监听数据库了
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                //检查有没有悬浮框的权限
                if (Settings.canDrawOverlays(MainActivity.this)) {
                    //如果有权限直接开启悬浮框，目的是保活
                    Toast.makeText(MainActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, ForwardService.class);
                    startService(intent);

                    List<String> failedPermissions = PermissionUtils.checkPermissions(this);
                    if (!failedPermissions.isEmpty()){
                        ActivityCompat.requestPermissions(this, PermissionUtils.sPermissions, 0x0001);
                    }
                } else {
                    //若没有权限，提示获取.
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                    Toast.makeText(MainActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
                    startActivityForResult(intent, 0x0002);
                }
            } else {
                //SDK在23以下，不用管.
                Intent intent = new Intent(MainActivity.this, ForwardService.class);
                startService(intent);
            }
        } else {
            if (!Manufacturer.isSmartisan()){
                // 锤子手机无法替换默认短信应用
                showDialog();
            } else {
                mSMSContentObserver = new SMSContentObserver(mHandler, getApplicationContext());

                getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mSMSContentObserver);
                //SDK在23以下，不用管.
                Intent intent = new Intent(MainActivity.this, ForwardService.class);
                startService(intent);
            }
        }

        Button button = findViewById(R.id.send_button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Toast.makeText(MainActivity.this, "模拟推送", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Start Push");
                TestSendPush.send(getApplicationContext());

            }
        });
    }

    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("读取新短信");
        builder.setMessage("为了防止\"有消息\"被杀掉，建议用此替换掉手机默认短信应用，如果选\"忽略\"，将使用数据库监听方式读取新短信");
        builder.setPositiveButton("替换", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isWantReplace = true;
                Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
                intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, getPackageName());
                startActivity(intent);
            }
        });
        builder.setNegativeButton("忽律", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSMSContentObserver = new SMSContentObserver(mHandler, getApplicationContext());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //检查有没有悬浮框的权限
                    if (Settings.canDrawOverlays(MainActivity.this)) {
                        //如果有权限直接开启悬浮框，目的是保活
                        Toast.makeText(MainActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ForwardService.class);
                        startService(intent);

                        List<String> failedPermissions = PermissionUtils.checkPermissions(getApplicationContext());
                        if (!failedPermissions.isEmpty()){
                            ActivityCompat.requestPermissions(MainActivity.this, PermissionUtils.sPermissions, 0x0001);
                        } else {
                            getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mSMSContentObserver);
                        }
                    } else {
                        //若没有权限，提示获取.
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        Toast.makeText(MainActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
                        startActivityForResult(intent, 0x0002);
                    }
                } else {
                    //SDK在23以下，不用管.
                    Intent intent = new Intent(MainActivity.this, ForwardService.class);
                    startService(intent);
                }
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (isWantReplace){
            // 点击替换，会再次弹出确认替换按钮，用户可能在这个时候，点击NO
            // 再次让用户确认一种方式

            if (!defaultSmsReplaced()){
                // 用户点了NO
                showDialog();
            } else {
                //用户点了 YES
                // 短信应用已经被替换成 有消息，不需要监听数据库了
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //检查有没有悬浮框的权限
                    if (Settings.canDrawOverlays(MainActivity.this)) {
                        //如果有权限直接开启悬浮框，目的是保活
                        Toast.makeText(MainActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(MainActivity.this, ForwardService.class);
                        startService(intent);

                        List<String> failedPermissions = PermissionUtils.checkPermissions(this);
                        if (!failedPermissions.isEmpty()){
                            ActivityCompat.requestPermissions(this, PermissionUtils.sPermissions, 0x0001);
                        }
                    } else {
                        //若没有权限，提示获取.
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                        Toast.makeText(MainActivity.this, "需要取得权限以使用悬浮窗", Toast.LENGTH_SHORT).show();
                        startActivityForResult(intent, 0x0002);
                    }
                } else {
                    //SDK在23以下，不用管.
                    Intent intent = new Intent(MainActivity.this, ForwardService.class);
                    startService(intent);
                }
            }
        }
        isWantReplace = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 0x0001:{
                List<String> failed = PermissionUtils.checkPermissions(this, permissions, grantResults);

                if (!failed.isEmpty()){
                    Toast.makeText(MainActivity.this, "权限不全", Toast.LENGTH_LONG).show();
                } else {
                    getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mSMSContentObserver);
                }
                break;
            }
            case 0x0002:{
                List<String> failedPermissions = PermissionUtils.checkPermissions(this);
                if (failedPermissions.isEmpty()){
                    // 所有的权限都获取到了
                    getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mSMSContentObserver);
                } else {
                    ActivityCompat.requestPermissions(this, PermissionUtils.sPermissions, 0x0001);
                }

                Toast.makeText(MainActivity.this, "已开启Toucher", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ForwardService.class);
                startService(intent);
                break;
            }
        }
    }

    public boolean defaultSmsReplaced(){
        return getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(this));
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d(TAG, "Sms received");
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if (mSMSContentObserver != null) {
            getContentResolver().unregisterContentObserver(mSMSContentObserver);
        }
    }

}
