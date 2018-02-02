package com.tx.mcc.app;

import com.tx.mcc.system.SystemChat;
import com.tx.mcc.system.SystemCrashHandler;
import com.tx.mcc.system.SystemKandy;
import com.tx.mcc.system.SystemKandySetting;
import com.tx.mcc.system.SystemLogHelper;
import com.txt.library.base.SystemManager;
import com.txt.library.baseApplication.CommonApplication;

/**
 * Created by pc on 2018/1/30.
 */

public class MyApplication extends CommonApplication {

    public static MyApplication mInstance;


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
        init();
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
