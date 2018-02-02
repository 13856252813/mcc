package com.txt.library.base;

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by pc on 2016/8/17.
 */

public class SystemManager implements ISystemManager {
    private Context mContext;
    private static SystemManager mInstance;
    private HashMap<String, SystemBase> mPools;

    private final static String TAG=SystemManager.class.getSimpleName();
    public static SystemManager getInstance() {
        if (mInstance == null) {
            synchronized (SystemManager.class){
                mInstance=new SystemManager();
            }
        }
        return mInstance;
    }
    public SystemManager(){
        mPools=new HashMap<>();
    }
    @Override
    public void initSystem(Context context) {
        mContext = context;
    }

    /**
     * 应用退出销毁各个系统
     */
    @Override
    public void destoryAllSysrtm() {
        if (mPools!=null){
            Log.d(TAG,"destoryAllSysrtm");
            Iterator<Map.Entry<String,SystemBase>> items=mPools.entrySet().iterator();

            while(items.hasNext()){
                Log.d(TAG,"items"+items);
                Map.Entry<String,SystemBase> next=items.next();
                Log.d(TAG,"items"+next.getValue());
                next.getValue().destorySystem();
            }
            mPools.clear();
            mPools=null;
        }
    }
    /**
     * 初始化各个模块
     * @param className
     * @param <T>
     * @return
     */
    public <T extends SystemBase> T getSystem(Class<T> className) {
        if (className == null) {
            return null;
        }
        Log.d(TAG, "getSystem: mPools"+mPools.size());
        T instance = (T) mPools.get(className.getName());
        Log.d(TAG, "getSystem: instance"+instance);
        if (instance==null){
            Log.d(TAG, "getSystem: getSystem");
            try {
                instance=className.newInstance();
                className.getMethod("createSystem",Context.class).invoke(instance,mContext);
                mPools.put(className.getName(),instance);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
}
