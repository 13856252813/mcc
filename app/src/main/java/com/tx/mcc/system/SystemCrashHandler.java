package com.tx.mcc.system;

import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.txt.library.base.SystemBase;

/**
 * Created by DELL on 2017/5/5.
 */

public class SystemCrashHandler  extends SystemBase implements Thread.UncaughtExceptionHandler {
    private static final String TAG=SystemCrashHandler.class.getSimpleName();
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    @Override
    public void init() {
        mDefaultHandler=Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.d(TAG, "uncaughtException: ");
        if (mDefaultHandler!=null&&handlerException(throwable)){
            mDefaultHandler.uncaughtException(thread,throwable);
        }else {
                System.exit(0);
        }
    }

    protected boolean handlerException(Throwable ex) {
        Log.d(TAG,"handlerException");
        if (ex == null) {
            return false;
        } else {
            // 5.2 弹出窗口提示信息
            new Thread(new Runnable() {
                public void run() {

                    Looper.prepare();
                    Toast.makeText(mContext, "应用出现异常，正在退出...+", Toast.LENGTH_SHORT).show();
                    Looper.loop();
                }
            }).start();
            return true;
        }

    }
}
