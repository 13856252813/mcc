package com.tx.mcc.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.tx.mcc.R;
import com.tx.mcc.https.HttpRequestClient;
import com.tx.mcc.system.SystemHttpRequest;
import com.tx.mcc.system.SystemKandy;
import com.tx.mcc.system.SystemKandySetting;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button mBtnCustomer;
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnCustomer = (Button) findViewById(R.id.btn_customer);
        mBtnCustomer.setOnClickListener(this);

        Log.e("fl","-----bool:"+(ContextCompat.checkSelfPermission(MainActivity.this,
                android.Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED ));
        getSystem(SystemKandySetting.class).setResolution(false, new SystemKandySetting.BaseCallBack() {
            @Override
            public void onFail() {
                Log.d(TAG, "onFail: setResolution");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: setResolution");
            }
        });
    }


    @Override
    public void onClick(View v) {
        getSystem(SystemHttpRequest.class).getKandyAccount(new HttpRequestClient.RequestHttpCallBack() {
            @Override
            public void onSuccess(String json) {
                Log.e("fl", "----getKandyAccount:" + json + "-------thead:");
                try {
                    JSONObject object = new JSONObject(json);
                    String userName = object.optString("username");
                    String password = object.optString("password");
                    userLogin(userName, password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFail(String err, int code) {
            }
        });
    }


    /**
     * 登录kandy
     */
    public void userLogin(final String userId, String password) {
        Log.d(TAG, "userLogin: ");
        getSystem(SystemKandy.class).userLogin(userId, password, new KandyLoginResponseListener() {
            @Override
            public void onLoginSucceeded() {
                Log.e("fl", "onLoginSucceeded: userLogin");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Intent intent=new Intent(MainActivity.this, VideoActivity.class);
                        intent.putExtra("userId",userId);
                        startActivity(intent);
                        finish();
                    }
                });
            }

            @Override
            public void onRequestFailed(int i, String s) {
                Log.d(TAG, "onLoginSucceeded: onRequestFailed" + i + s);
                showServiceRequestFailDialog("加载失败，请退出重新加载");
            }
        });
    }

    private void showServiceRequestFailDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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


}
