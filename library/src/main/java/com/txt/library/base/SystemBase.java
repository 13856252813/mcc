package com.txt.library.base;

import android.content.Context;

/**
 * Created by pc on 2016/8/17.
 */

public abstract class SystemBase implements ISystem {
    public Context mContext;
    @Override
    public void createSystem(Context context) {
        mContext=context;
        init();
    }
    public abstract void init();

    @Override
    public void destorySystem() {

    }

    public <T extends SystemBase>T getSystem(Class<T> tClass){
        return SystemManager.getInstance().getSystem(tClass);
    }
}
