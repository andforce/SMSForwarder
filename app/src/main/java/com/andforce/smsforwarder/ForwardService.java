package com.andforce.smsforwarder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.andforce.smsforwarder.view.FloatView;

public class ForwardService extends Service {

    //Log用的TAG
    private static final String TAG = "MainService";

    //要引用的布局文件.
    FloatView mFloatView;
    @Override
    public void onCreate() {
        super.onCreate();
        //获取浮动窗口视图所在布局.
        mFloatView = (FloatView) (LayoutInflater.from(getApplication()).inflate(R.layout.toucherlayout, null).findViewById(R.id.imageButton1));
        mFloatView.show(500, 500);
        mFloatView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ForwardService.this, "Click", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(ForwardService.this, MainActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ForwardService.this.startActivity(i);
            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
