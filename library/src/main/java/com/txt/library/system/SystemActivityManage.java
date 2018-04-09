package com.txt.library.system;

import android.app.Activity;
import android.util.Log;

import com.txt.library.base.SystemBase;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by DELL on 2017/10/15.
 */

public class SystemActivityManage extends SystemBase {

    public static final String TAG=SystemActivityManage.class.getSimpleName();
    @Override
    public void init() {

    }
    private static List<Activity> mActivitys = Collections
            .synchronizedList(new LinkedList<Activity>());

    //添加Activity
    public void pushActivity(Activity activity) {
        Log.d(TAG, "pushActivity: "+activity);
        mActivitys.add(activity);
    }
    //推出Activity
    public void popActivity(Activity activity) {
        Log.d(TAG, "popActivity: "+activity);
        if (mActivitys.contains(activity)){
            mActivitys.remove(activity);
        }
    }

    //清楚所以包含的Activity
    public void clear(){
        for (Activity activity:mActivitys){
            mActivitys.remove(activity);
            activity.finish();
        }
    }
    //结束类名Actvity
    public static void finishActivity(Class<?> cls) {
        Log.d(TAG, "finishActivity: ");
        if (mActivitys == null||mActivitys.isEmpty()) {
            return;
        }
        for (Activity activity : mActivitys) {
            if (activity.getClass().equals(cls)) {
                finishActivity(activity);
            }
        }
    }

    public static void finishActivity(Activity activity) {
        if (mActivitys == null||mActivitys.isEmpty()) {
            return;
        }
        if (activity != null) {
            mActivitys.remove(activity);
            activity.finish();
            activity = null;
        }
    }

    public boolean isContainActivity(Class<?> cls){
        if (mActivitys == null||mActivitys.isEmpty()) {
            return false;
        }
        for (Activity activity : mActivitys) {
            if (activity.getClass().equals(cls)) {
                return true;
            }
        }
        return  false;
    }
}
