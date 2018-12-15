package com.andforce.smsforwarder.view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;

public class FloatView extends android.support.v7.widget.AppCompatImageButton {

    private float x = 0;
    private float y = 0;

    //布局参数.
    WindowManager.LayoutParams params;
    //实例化的WindowManager.
    WindowManager windowManager;

    //状态栏高度.（接下来会用到）
    int statusBarHeight = -1;

    public FloatView(Context context) {
        super(context);
    }

    public FloatView(Context context,  AttributeSet attrs) {
        super(context, attrs);

        //赋值WindowManager&LayoutParam.
        params = new WindowManager.LayoutParams();
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
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
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    public void show(int x, int y){
        params.x = x;
        params.y = y;
        windowManager.addView(this, params);
    }

    public FloatView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean mNeedClick = false;
    Handler mHandler = new Handler(Looper.myLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0x0001:{
                    mNeedClick = false;
                    Log.d("FLOAT_TOUCH", "handleMessage");
                }
            }
        }
    };

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN :{
                x = event.getRawX();
                y = event.getRawY();
                Log.d("FLOAT_TOUCH", "DOWN X:" +x + " Y:" + y);
                mNeedClick = true;
                 mHandler.removeMessages(0x0001);
                 mHandler.sendEmptyMessageDelayed(0x0001, 500);
                return true;
            }
            case MotionEvent.ACTION_MOVE :{
                float moveX = event.getRawX();
                float moveY = event.getRawY();

                Log.d("FLOAT_TOUCH", "MOVE X:" + moveX + " Y:" + moveY);
                if (Math.abs(x - moveX) < 10 && Math.abs(y - moveY) < 10){
                    break;
                }
                mNeedClick = false;

                params.x = (int) event.getRawX() - this.getWidth() / 2;
                //这就是状态栏偏移量用的地方
                params.y = (int) event.getRawY() - this.getHeight() / 2 - statusBarHeight;
                windowManager.updateViewLayout(this, params);
                x = moveX;
                y = moveY;

                return true;
            }

            case MotionEvent.ACTION_UP :{
                Log.d("FLOAT_TOUCH", "UP");
                x = 0;
                y = 0;
                if (mNeedClick) {
                    this.performClick();
                }
                return true;
            }

        }
        return super.onTouchEvent(event);
    }
}
