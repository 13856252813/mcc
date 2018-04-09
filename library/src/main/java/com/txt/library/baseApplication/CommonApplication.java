package com.txt.library.baseApplication;

import android.app.Application;

import com.txt.library.base.SystemBase;
import com.txt.library.base.SystemManager;
import com.txt.library.system.SystemChat;
import com.txt.library.system.SystemCrashHandler;
import com.txt.library.system.SystemKandy;
import com.txt.library.system.SystemKandySetting;
import com.txt.library.system.SystemLogHelper;

/**
 * Created by DELL on 2017/5/23.
 */

public class CommonApplication extends Application {


    public static CommonApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        mInstance=this;
    }

    public <T extends SystemBase>T getSystem(Class<T> tClass){
        return SystemManager.getInstance().getSystem(tClass);
    }

    public void init(){
        SystemManager.getInstance().initSystem(this);
        getSystem(SystemKandy.class);//初始化Kandy
        getSystem(SystemCrashHandler.class);
        getSystem(SystemChat.class);//初始化消息控制类
        getSystem(SystemKandySetting.class);//初始化KandySetting
        getSystem(SystemLogHelper.class).start();
    }
}
