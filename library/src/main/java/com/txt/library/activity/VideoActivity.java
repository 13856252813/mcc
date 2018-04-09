package com.txt.library.activity;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyUploadProgressListener;
import com.google.gson.Gson;
import com.txt.library.PiccConfig;
import com.txt.library.R;
import com.txt.library.base.BaseActivity;
import com.txt.library.dialog.LoadingView;
import com.txt.library.dialog.ShowRemoteDialog;
import com.txt.library.https.HttpRequestClient;
import com.txt.library.model.PhoneCallState;
import com.txt.library.model.PhoneSendMsg;
import com.txt.library.model.ServiceRequest;
import com.txt.library.receive.PhoneStateChangeReceive;
import com.txt.library.system.SystemChat;
import com.txt.library.system.SystemCommon;
import com.txt.library.system.SystemHttpRequest;
import com.txt.library.system.SystemKandy;
import com.txt.library.system.SystemKandySetting;
import com.txt.library.utils.AudioModeManger;
import com.txt.library.utils.KExtUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


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
    private boolean mIsFront=false;


    private Handler mCountCheck=new Handler();
    private ShowRemoteDialog mRemoteDialog;
    private AudioModeManger mModelManager;
    private BroadcastReceiver mPhoneState;
    private HeadsetDetectReceiver mHeadSetReceiver;
    private int mIndex=0;

    private LoadingView mLoadingView;
    private ServiceRequest.UserInfoBean mCurrentBean;

//    //shuzu
//    private String departmentId="5a700c1fb5f488243d6e5048";
    private String departmentId="";
    private boolean mIsFlshightEnable=false;
//    private String departmentId="591031d9c842ab120d98b49c";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_video);
        userId=getIntent().getStringExtra("userId");
        departmentId=getIntent().getStringExtra("departmentId");

        audioManager= (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        mSp=getSharedPreferences("mcc_config",Context.MODE_PRIVATE);
        mRemoteDialog=new ShowRemoteDialog(VideoActivity.this);
        mModelManager=new AudioModeManger(this);
        mModelManager.register();

        Kandy.getAccess().registerNotificationListener(this);
        getSystem(SystemKandy.class).setKandyCallListener(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getSystem(SystemChat.class).setOnChatMsgReceiveListener(this);
        initView();

        initPhonekStateChang();
        sendServiceRequest();
        setResolution();
    }


    public void initView() {
        mCurrentBean= (ServiceRequest.UserInfoBean) getIntent().getSerializableExtra("userInfo");
        mLoadingView=new LoadingView(this);
        mLocalView = (KandyView) findViewById(R.id.localView);
        mLocalView.setDrawingCacheEnabled(true);
        mShowKandyView= (RelativeLayout) findViewById(R.id.showvideo);
        mShowRequestView= (LinearLayout) findViewById(R.id.showlist);
        mListStatue= (TextView) findViewById(R.id.liststate);
        mCancleButton= (Button) findViewById(R.id.canclesession);
        mCancleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoActivity.this);
                builder.setTitle("提示").setMessage("是否确认取消排队").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        cancleRequest();
                        releaseUser();
                        dialog.dismiss();

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }
        });


        mRemoteDialog.mHangupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hangup();
            }
        });
        mRemoteDialog.mAudioMute.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View v) {

                if (mIsFront){
//                    //前置状态下打开手电筒
                    getSystem(SystemCommon.class).lightSwitch(!mIsFlshightEnable);
                }else {
                    //后置状态下打开手电筒
                    KExtUtil.lightSwitch(!mIsFlshightEnable);
                }
                if (mIsFlshightEnable){
                    mRemoteDialog.mLightImg.setImageResource(R.mipmap.light_on);
                }else {
                    mRemoteDialog.mLightImg.setImageResource(R.mipmap.light_off);
                }
                mIsFlshightEnable=!mIsFlshightEnable;
            }
        });

        mRemoteDialog.mCamerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: mIsFront"+mIsFront);
                if (mIsFront){
                    mIsFront=false;
                }else {
                    mIsFront=true;
                }
                switchCamer(mIsFront,new KandyResponseListener() {
                    @Override
                    public void onRequestSucceded() {
                        Log.d(TAG, "onRequestSucceded: "+mIsFront);

                    }
                    @Override
                    public void onRequestFailed(int i, String s) {
                        Log.d(TAG, "onRequestFailed: mIsFront"+s);
                    }
                });
            }
        });
        mRemoteDialog.mTakeKandyVieewPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                screenShotpic();
            }
        });
    }


    public void screenShotpic(){
        mLoadingView.show("正在拍照");
        if (isHUAWER()){
            ViewGroup.LayoutParams params=mLocalView.getLayoutParams();
            params.width=1000;
            params.height=1000;
            mLocalView.setLayoutParams(params);
        }
        getSystem(SystemCommon.class).screenShotFromSurfaceView(mLocalView.getRenderer(), new SystemCommon.ScreenShotCallback()
        {
            @Override
            public void shotSuccess(final String imagePath) {
                VideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isHUAWER()){
                            if (mIndex<1){
                                mIndex++;
                                try {
                                    Thread.sleep(300);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                screenShotpic();
                                return;
                            }else {
                                mIndex=0;
                                mLoadingView.dismiss();
                                ViewGroup.LayoutParams params=mLocalView.getLayoutParams();
                                params.width= ViewGroup.LayoutParams.MATCH_PARENT;
                                params.height= ViewGroup.LayoutParams.MATCH_PARENT;
                                mLocalView.setLayoutParams(params);
                            }
                        }else {
                            mLoadingView.dismiss();
                            File file=new File(imagePath);
                            mLoadingView.show("正在上传");
                            getSystem(SystemChat.class).sendImage(Uri.fromFile(file), new KandyUploadProgressListener() {
                                @Override
                                public void onProgressUpdate(IKandyTransferProgress iKandyTransferProgress) {
                                    Log.d(TAG, "onProgressUpdate:");
                                }

                                @Override
                                public void onRequestSucceded() {
                                    Log.i(TAG, "uploadKandyFile():onRequestSucceded()");
                                    VideoActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mLoadingView.dismiss();
                                        }
                                    });
                                }

                                @Override
                                public void onRequestFailed(int err, String s) {
                                    VideoActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            mLoadingView.dismiss();
                                            Toast.makeText(VideoActivity.this, "图片上传出错", Toast.LENGTH_LONG).show();
                                        }
                                    });
                                    Log.e(TAG, "uploadKandyFile():onRequestFailed " + err + " error code: " + s);
                                }
                            });
                            mIndex=0;
                            ViewGroup.LayoutParams params=mLocalView.getLayoutParams();
                            params.width= ViewGroup.LayoutParams.MATCH_PARENT;
                            params.height= ViewGroup.LayoutParams.MATCH_PARENT;
                            mLocalView.setLayoutParams(params);
                        }
                    }
                });


            }

            @Override
            public void shotSuccess(Bitmap bitmap) {

            }

            @Override
            public void shotFail() {
                Log.d(TAG, "shotFail: ");
                VideoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mRemoteDialog.mTakeKandyVieewPicture.setClickable(true);
                        Toast.makeText(VideoActivity.this, "图片拍照出错", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    public boolean isHUAWER(){
        String model= android.os.Build.BRAND;
        getSystem(SystemCommon.class).mMoblieModle=model;
        return getSystem(SystemCommon.class).isFADMoblie();
    }

    /**
     * 设置视频
     */
    public void setResolution(){
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
    protected void onResume() {
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
//        getSystem(SystemCommon.class).moveToBack(VideoActivity.this);
        if (mModelManager != null) {
            mModelManager.setSpeakerInit();
            mModelManager.unregister();
            if (mRemoteDialog != null && mRemoteDialog.isShowing()) {
                mRemoteDialog.dismiss();
                mRemoteDialog = null;
            }
            isLoop = false;
            unregisterBroadReceive();
            unregiestHeadSetReceive();
            endSession();
            mCountCheck.removeCallbacks(mCountCheckRunnable);
            Kandy.getProvisioning().deactivate(new KandyResponseListener() {
                @Override
                public void onRequestSucceded() {
                    Log.d(TAG, "onRequestSucceded: ");
                }

                @Override
                public void onRequestFailed(int i, String s) {
                    Log.d(TAG, "onRequestFailed: code:" + i + " s:" + s);
                }
            });

        }
    }

    public void cancleRequest(){
        getSystem(SystemKandy.class).rejectIncomingCall();
    }

    public void hangup(){
        getSystem(SystemKandy.class).hangUp(new SystemKandy.CallRequestCallBack() {
            @Override
            public void onRequrestSuccess() {
                Log.d(TAG, "onRequrestSuccess: hangUp");
            }
            @Override
            public void onRequrestFailer() {
                VideoActivity.this.finish();
            }
        });
    }

    public void switchCamer(boolean isFront, KandyResponseListener listener){
        if (getSystem(SystemKandy.class).getmCurrentCall()==null){
            return;
        }
        if (isFront){
            getSystem(SystemKandy.class).getmCurrentCall().switchCamera(KandyCameraInfo.FACING_FRONT,getSystem(SystemKandySetting.class).mFrontSize,listener);
        }else {
            getSystem(SystemKandy.class).getmCurrentCall().switchCamera(KandyCameraInfo.FACING_BACK,getSystem(SystemKandySetting.class).mFrontSize,listener);
        }
    }

    /**
     * 发送serviceRequest
     */
    public void sendServiceRequest() {
        ServiceRequest request=new ServiceRequest();
        if(mCurrentBean==null){
            mCurrentBean=new ServiceRequest.UserInfoBean();
        }
        mCurrentBean.setCustomerId("nono");
        request.setCompanyId("txtechnology");
        request.setUserId(userId);
        request.setType("video");
        request.setDepartment(departmentId);
        request.setDeviceType("android");
        request.setChannelType("android");
        request.setUserInfo(mCurrentBean);
        Log.e("fl","------userbean:"+mCurrentBean.toString());
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

    //初始化KandyStatechange
    public void initPhonekStateChang(){
        mPhoneState=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action=intent.getAction();
                PhoneCallState callmsg=new PhoneCallState();
                PhoneSendMsg message=new PhoneSendMsg();
                if (action.equals(PhoneStateChangeReceive.CALL_STATE_RINGING)){
                    //开始来电
                    message.command= PiccConfig.PHONE_PSTN_COMMING;
                    message.text= PiccConfig.PHONE_PSTN_COMMING_TXT;
                }else if (action.equals(PhoneStateChangeReceive.CALL_STATE_IDLE)){
                    //来电结束
                    message.command= PiccConfig.PHONE_PSTN_END;
                    message.text=PiccConfig.PHONE_PSTN_END_TXT;
                }
                callmsg.message=message;
                JSONObject jsonObject=null;
                try {
                    jsonObject=new JSONObject(new Gson().toJson(callmsg));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                sendChatMsg("",jsonObject, new RequestCallBack() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onFail() {

                    }
                });


            }
        };
        IntentFilter filter=new IntentFilter();
        filter.addAction(PhoneStateChangeReceive.CALL_STATE_RINGING);
        filter.addAction(PhoneStateChangeReceive.CALL_STATE_IDLE);
        registerReceiver(mPhoneState,filter);
    }

    //发送kandy 文本以及additionData消息
    public void sendChatMsg(String txtMsg,JSONObject additionDataMsg,final RequestCallBack callBack){
        getSystem(SystemChat.class).sendChatWithMessage(getSystem(SystemKandy.class).mAgentCall, txtMsg,"control",additionDataMsg, false, new SystemChat.OnChatRequestCallBack() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "onSuccess: sendChatMsg");
                if (callBack!=null){
                    callBack.onSuccess();
                }
            }
            @Override
            public void onFail(int errCode, String errMsg) {
                Log.e(TAG, "onFail: sendChatMsg:errCode"+errCode+"errMsg"+errMsg);
                if (callBack!=null){
                    callBack.onFail();
                }
            }

            @Override
            public void onProgressUpdate(IKandyTransferProgress iKandyTransferProgress) {

            }
        });
    }

    public void unregisterBroadReceive(){
        if (mPhoneState!=null){
            unregisterReceiver(mPhoneState);
        }
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
        Log.i(TAG,"------onTalking:");
        isTalking=true;
        if (mShowRequestView.getVisibility()== View.VISIBLE){
            mShowRequestView.setVisibility(View.GONE);
            mShowKandyView.setVisibility(View.VISIBLE);
        }
        if (mModelManager!=null){
            mModelManager.setSpeakerPhoneOn(true);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        VideoActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRemoteDialog.show();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRemoteDialog.dismiss();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mRemoteDialog.show();
            }
        });
        if (mModelManager.isWiredHeadsetOn()){
            mModelManager.setSpeakerPhoneOn(false);
        }else {
            mModelManager.setSpeakerPhoneOn(true);
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


    //注册监听耳机插入和拔出的广播
    private void regeistHeadsetReceive() {
        mHeadSetReceiver=new HeadsetDetectReceiver();
        IntentFilter filter=new IntentFilter();
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(mHeadSetReceiver,filter);
    }

    public void unregiestHeadSetReceive(){
        if (mHeadSetReceiver!=null){
            unregisterReceiver(mHeadSetReceiver);
        }
    }

    public class HeadsetDetectReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_HEADSET_PLUG.equals(action)) {
                if (intent.hasExtra("state")) {
                    int state = intent.getIntExtra("state", 0);
                    if (isTalking){
                        if (state == 1) {
                            Log.d(TAG, "onReceive: 插入耳机");
                            if (mModelManager!=null){
                                mModelManager.setSpeakerPhoneOn(false);
                            }
                        } else if(state == 0){
                            Log.d(TAG, "onReceive: 拔出耳机");
                            mModelManager.setSpeakerPhoneOn(true);
                        }
                    }
                }
            }
        }
    }


    public void checkCoutReuestService(String requestId){
        getSystem(SystemHttpRequest.class).getReuestCount(requestId, new HttpRequestClient.RequestHttpCallBack() {
            @Override
            public void onSuccess(final String json) {
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
//        getSystem(SystemCommon.class).moveToBack(VideoActivity.this);
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
    public interface RequestCallBack {
        void onSuccess();
        void onFail();
    }

    public void endSession(){
        if (mUserAccessToken!=null&&!mUserAccessToken.equals("")){
            getSystem(SystemHttpRequest.class).endSession(mRequestId,mUserAccessToken, new HttpRequestClient.RequestHttpCallBack() {
                @Override
                public void onSuccess(String json) {
                    Log.i(TAG, "onSuccess: endSession"+json);
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
