package com.txt.library.system;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.txt.library.activity.VideoActivity;
import com.txt.library.base.SystemBase;
import com.txt.library.https.HttpRequestClient;
import com.txt.library.model.ServiceRequest;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.ContentValues.TAG;

/**
 * Created by pc on 2018/2/5.
 */

public class SystemLogin extends SystemBase {

    private Activity activity;
    private String DepartmentId="";

    public SystemLogin(Activity activity){
        this.activity=activity;
    }

    public void loginToKandy(final ServiceRequest.UserInfoBean bean,final LoginStatusCallBack callBack) {
        getSystem(SystemHttpRequest.class).getKandyAccount(new HttpRequestClient.RequestHttpCallBack() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject object = new JSONObject(json);
                    String userName = object.optString("username");
                    String password = object.optString("password");
                    userLogin(bean,userName, password,callBack);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onFail(String err, int code) {
                callBack.loginFail(err,code);
            }
        });
    }

    /**
     * 登录kandy
     */
    public void userLogin(final ServiceRequest.UserInfoBean bean,final String userId, String password, final LoginStatusCallBack callBack) {
        Log.d(TAG, "userLogin: ");
        getSystem(SystemKandy.class).userLogin(userId, password, new KandyLoginResponseListener() {
            @Override
            public void onLoginSucceeded() {
                callBack.loginSuccess();
                Log.d(TAG, "onLoginSucceeded: userLogin");
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(activity, VideoActivity.class);
                        intent.putExtra("userInfo",bean);
                        intent.putExtra("userId", userId);
                        intent.putExtra("departmentId",DepartmentId);
                        activity.startActivity(intent);
//                        activity.finish();
                    }
                });
            }

            @Override
            public void onRequestFailed(int i, String s) {
                Log.d(TAG, "onLoginSucceeded: onRequestFailed" + i + s);
                callBack.loginFail(s,i);
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showServiceRequestFailDialog(activity, "加载失败，请退出重新加载");
                    }
                });
            }
        });
    }

    private void showServiceRequestFailDialog(Activity activity, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("提示").setMessage(message).setNegativeButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).setPositiveButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).create().show();
    }


    @Override
    public void init() {

    }

    public interface LoginStatusCallBack{
        void loginSuccess();
        void loginFail(String err, int code);
    }
    
    public void setDepartmentId(String departmentId){
        DepartmentId=departmentId;
    }
}
