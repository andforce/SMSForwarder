package com.andforce.smsforwarder;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

public class ForwardService extends Service {

    //Log用的TAG
    private static final String TAG = "MainService";

    //要引用的布局文件.
    View toucherLayout;
    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;

    ImageButton imageButton1;

    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;

    float x = 0;
    float y = 0;
    @Override
    public void onCreate() {
        super.onCreate();
        createToucher();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createToucher() {
        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) getApplication().getSystemService(Context.WINDOW_SERVICE);
        //设置type.系统提示型窗口，一般都在应用程序窗口之上.
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        //设置效果为背景透明.
        params.format = PixelFormat.RGBA_8888;
        //设置flags.不可聚焦及不可使用按钮对悬浮窗进行操控.
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        //设置窗口初始停靠位置.
        params.gravity = Gravity.START | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        //设置悬浮窗口长宽数据.
        //注意，这里的width和height均使用px而非dp.这里我偷了个懒
        //如果你想完全对应布局设置，需要先获取到机器的dpi
        //px与dp的换算为px = dp * (dpi / 160).
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        LayoutInflater inflater = LayoutInflater.from(getApplication());
        //获取浮动窗口视图所在布局.
        toucherLayout = inflater.inflate(R.layout.toucherlayout, null);
        //添加toucherlayout
        windowManager.addView(toucherLayout, params);

        Log.i(TAG, "toucherlayout-->left:" + toucherLayout.getLeft());
        Log.i(TAG, "toucherlayout-->right:" + toucherLayout.getRight());
        Log.i(TAG, "toucherlayout-->top:" + toucherLayout.getTop());
        Log.i(TAG, "toucherlayout-->bottom:" + toucherLayout.getBottom());

        //主动计算出当前View的宽高信息.
        toucherLayout.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        //用于检测状态栏高度.
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        Log.i(TAG, "状态栏高度为:" + statusBarHeight);

        //浮动窗口按钮.
        imageButton1 = (ImageButton) toucherLayout.findViewById(R.id.imageButton1);

        //其他代码...
        imageButton1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN :{
                        x = event.getRawX();
                        y = event.getRawY();
                        Log.d("FLOAT_TOUCH", "DOWN X:" +x + " Y:" + y);
                        break;
                    }
                    case MotionEvent.ACTION_MOVE :{
                        float moveX = event.getRawX();
                        float moveY = event.getRawY();

                        Log.d("FLOAT_TOUCH", "MOVE X:" + moveX + " Y:" + moveY);
                        if (Math.abs(x - moveX) < 10 && Math.abs(y - moveY) < 10){
                            break;
                        }
                        params.x = (int) event.getRawX() - toucherLayout.getWidth() / 2;
                        //这就是状态栏偏移量用的地方
                        params.y = (int) event.getRawY() - toucherLayout.getHeight() / 2 - statusBarHeight;
                        windowManager.updateViewLayout(toucherLayout, params);
                        x = moveX;
                        y = moveY;

                        break;
                    }

                    case MotionEvent.ACTION_UP :{
                        Log.d("FLOAT_TOUCH", "UP");
                        x = 0;
                        y = 0;
                        break;
                    }

                }

                return false;
            }
        });


    }

}
