package com.tx.mcc.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyConnectServiceNotificationListener;
import com.genband.kandy.api.access.KandyConnectionState;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.access.KandyRegistrationState;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.tx.mcc.R;
import com.tx.mcc.dialog.ShowRemoteDialog;
import com.tx.mcc.https.HttpRequestClient;
import com.tx.mcc.model.ServiceRequest;
import com.tx.mcc.system.SystemChat;
import com.tx.mcc.system.SystemCommon;
import com.tx.mcc.system.SystemHttpRequest;
import com.tx.mcc.system.SystemKandy;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by pc on 2018/1/30.
 */

public class VideoActivity extends BaseActivity implements SystemKandy.KandyCallListener, SystemChat.onChatReceiveListener,
        KandyConnectServiceNotificationListener {

    private static final String TAG=VideoActivity.class.getSimpleName();
    private KandyView mLocalView;
    private LinearLayout mShowRequestView;
    private RelativeLayout mShowKandyView;
    private TextView mListStatue;
    private Button mCancleButton;
    private String mRequestId;

    private String userId;
    private boolean isTalking;
    private String mUserAccessToken;
    private AudioManager audioManager;
    private SharedPreferences mSp;
    private boolean isLoop=true;

    private Handler mCountCheck=new Handler();
    private ShowRemoteDialog mRemoteDialog;


    private String departmentId="5a700c1fb5f488243d6e5048";
//    private String departmentId="591031d9c842ab120d98b49c";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        userId=getIntent().getStringExtra("userId");

        audioManager= (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        mSp=getSharedPreferences("mcc_config",Context.MODE_PRIVATE);
        mRemoteDialog=new ShowRemoteDialog(VideoActivity.this);
        Kandy.getAccess().registerNotificationListener(this);
        getSystem(SystemKandy.class).setKandyCallListener(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSystem(SystemChat.class).setOnChatMsgReceiveListener(this);
        initView();
        sendServiceRequest();
        getSystem(SystemCommon.class).verifyStoragePermissions(this);

    }


    public void initView() {
        mLocalView = (KandyView) findViewById(R.id.localView);
        mLocalView.setDrawingCacheEnabled(true);
        mShowKandyView= (RelativeLayout) findViewById(R.id.showvideo);
        mShowRequestView= (LinearLayout) findViewById(R.id.showlist);
        mListStatue= (TextView) findViewById(R.id.liststate);
        mCancleButton= (Button) findViewById(R.id.canclesession);
    }


    @Override
    protected void onResume() {
        Log.i(TAG,"------onResume:");
        super.onResume();
        if (isTalking){
            getSystem(SystemKandy.class).stopCamer(new KandyCallResponseListener() {
                @Override
                public void onRequestSucceeded(IKandyCall iKandyCall) {
                    getSystem(SystemKandy.class).startCamer();
                }

                @Override
                public void onRequestFailed(IKandyCall iKandyCall, int i, String s) {
                    Log.d(TAG, "onRequestFailed: "+s);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        endSession();
        Kandy.getProvisioning().deactivate(new KandyResponseListener() {
            @Override
            public void onRequestSucceded() {
                Log.d(TAG, "onRequestSucceded: ");
            }

            @Override
            public void onRequestFailed(int i, String s) {
                Log.d(TAG, "onRequestFailed: code:"+i+" s:"+s);
            }
        });

    }

    /**
     * 发送serviceRequest
     */
    public void sendServiceRequest() {
        getSystem(SystemKandy.class).deactivate(new SystemKandy.onCallDeactivateCallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: deactivate");
            }

            @Override
            public void onFail(String err, int errcode) {
                Log.d(TAG, "onFail: deactivate"+err+">>errcode"+errcode);
            }
        });
        ServiceRequest request=new ServiceRequest();
        ServiceRequest.UserInfoBean bean=new ServiceRequest.UserInfoBean();
        bean.setCustomerId("nono");
        request.setCompanyId("txtechnology");
        request.setUserId(userId);
        request.setType("video");
        request.setDepartment(departmentId);
        request.setDeviceType("android");
        request.setChannelType("android");
        request.setUserInfo(bean);
        getSystem(SystemHttpRequest.class).postServiceRequests(request, new HttpRequestClient.RequestHttpCallBack() {
            @Override
            public void onSuccess(String json) {
                try {
                    JSONObject jsonData=new JSONObject(json);
                    if(jsonData!=null){
                        mUserAccessToken=jsonData.optString("userAccessToken");
                        mRequestId=jsonData.optString("_id");
                        String userId=jsonData.optString("userId");
                        getSystem(SystemChat.class).mCurrentUser=userId;
                        startCheckStatus();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFail(String err, int code) {

            }
        });

    }

    @Override
    public void onRegistrationStateChanged(KandyRegistrationState kandyRegistrationState) {

    }

    @Override
    public void onConnectionStateChanged(KandyConnectionState kandyConnectionState) {

    }

    @Override
    public void onInvalidUser(String s) {

    }

    @Override
    public void onSessionExpired(String s) {

    }

    @Override
    public void onSDKNotSupported(String s) {

    }

    @Override
    public void onCertificateError(String s) {

    }

    @Override
    public void onServerConfigurationReceived(JSONObject jsonObject) {

    }

    @Override
    public void onChatReceive(String chatMsg) {
        Log.i(TAG,"------onChatReceive:");

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onTalking() {
        Log.e("fl","-----ontalking");
        isTalking=true;
        if (mShowRequestView.getVisibility()== View.VISIBLE){
            mShowRequestView.setVisibility(View.GONE);
            mShowKandyView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onTenminaten(final int code) {
        Log.e(TAG, "onTenminaten: onTenminaten");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tenminationMsg = getTermaintedMsg(code);
                Toast.makeText(VideoActivity.this, tenminationMsg, Toast.LENGTH_SHORT).show();

            }
        });
        releaseUser();

    }

    @Override
    public void onIncomingCall(boolean isVideo) {
        Log.e("fl","----onIncomingCall");
        getSystem(SystemKandy.class).getmCurrentCall().setLocalVideoView(mLocalView);
        getSystem(SystemKandy.class).getmCurrentCall().setRemoteVideoView(mRemoteDialog.getRemoteKandyView());
        getSystem(SystemKandy.class).accep(new KandyCallResponseListener() {
            @Override
            public void onRequestSucceeded(IKandyCall iKandyCall) {

            }

            @Override
            public void onRequestFailed(IKandyCall iKandyCall, int i, String s) {

            }
        });

    }

    @Override
    public void isRing() {

    }


    public void checkCoutReuestService(String requestId){
        getSystem(SystemHttpRequest.class).getReuestCount(requestId, new HttpRequestClient.RequestHttpCallBack() {
            @Override
            public void onSuccess(final String json) {
                Log.i(TAG, "checkCoutReuestService  onSuccess: "+json);
                if (json.equals("Not Found")){
                    VideoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoActivity.this,"客服已经结束会话",Toast.LENGTH_LONG).show();
                            VideoActivity.this.finish();
                        }
                    });
                    return;
                }

            }

            @Override
            public void onFail(String err, int code) {
                Log.d(TAG, "checkCoutReuestService  onFail: "+err);
                isLoop=false;
                VideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                    }
                });
            }
        });
    }


    private Runnable mCountCheckRunnable=new Runnable() {
        @Override
        public void run() {
            if (isLoop){
                checkCoutReuestService(mRequestId);
                mCountCheck.postDelayed(mCountCheckRunnable,1000);
            }
        }
    };

    public void startCheckStatus(){
        isLoop=true;
        mCountCheck.postDelayed(mCountCheckRunnable,0);
    }

    //释放用户
    public void releaseUser(){
        getSystem(SystemCommon.class).moveToBack(VideoActivity.this);
        getSystem(SystemKandy.class).deactivate(new SystemKandy.onCallDeactivateCallBack() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "onSuccess: deactivate");
            }

            @Override
            public void onFail(String err, int errcode) {
                Log.d(TAG, "onFail: deactivate"+err+">>errcode"+errcode);
            }
        });
        getSystem(SystemKandy.class).userLogout(new KandyLogoutResponseListener() {
            @Override
            public void onLogoutSucceeded() {
                Log.d(TAG, "onLogoutSucceeded: ");
                VideoActivity.this.finish();
            }
            @Override
            public void onRequestFailed(int i, String s) {
                VideoActivity.this.finish();
            }
        });
    }
    public String getTermaintedMsg(int respondcode) {
        String msg = null;
        switch (respondcode) {
            case 603:
                msg = "对方无法连接";
                break;
            case 9900:
                msg = "对方已挂断";
                break;
            case 9901:
                msg = "用户已挂断";
                break;
            case 487:
                msg = "无法接听";
                break;
            case 480:
                msg = "对方不在线";
                break;
            case 402:
                msg = "运营商暂不支持该功能";
                break;
            case 0:
                msg = "对方已挂断";
                break;
            case 9902:
                msg = "通讯已挂断";
                break;
            case 9906:
                msg = "通讯已挂断";
                break;
            default:
                msg = "通讯已挂断";
                break;
        }
        return msg;
    }

    public void endSession(){
        if (mUserAccessToken!=null&&!mUserAccessToken.equals("")){
            getSystem(SystemHttpRequest.class).endSession(mRequestId,mUserAccessToken, new HttpRequestClient.RequestHttpCallBack() {
                @Override
                public void onSuccess(String json) {
                    Log.d(TAG, "onSuccess: endSession"+json);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(VideoActivity.this,"会话结束！",Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override
                public void onFail(String err, int code) {
                    Log.d(TAG, "onFail: err"+err+"code"+code);
                }
            });
        }else {
            VideoActivity.this.finish();
        }
    }
}
