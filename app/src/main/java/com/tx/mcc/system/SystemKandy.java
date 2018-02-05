package com.tx.mcc.system;

import android.os.Handler;
import android.util.Log;

import com.genband.kandy.api.IKandyGlobalSettings;
import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.access.KandyLoginResponseListener;
import com.genband.kandy.api.access.KandyLogoutResponseListener;
import com.genband.kandy.api.provisioning.IKandyValidationResponse;
import com.genband.kandy.api.provisioning.KandyValidationMethoud;
import com.genband.kandy.api.provisioning.KandyValidationResponseListener;
import com.genband.kandy.api.services.calls.IKandyCall;
import com.genband.kandy.api.services.calls.IKandyIncomingCall;
import com.genband.kandy.api.services.calls.IKandyOutgoingCall;
import com.genband.kandy.api.services.calls.KandyCallRTPStatisticsListener;
import com.genband.kandy.api.services.calls.KandyCallResponseListener;
import com.genband.kandy.api.services.calls.KandyCallServiceNotificationListener;
import com.genband.kandy.api.services.calls.KandyCallState;
import com.genband.kandy.api.services.calls.KandyOutgingVoipCallOptions;
import com.genband.kandy.api.services.calls.KandyOutgoingPSTNCallOptions;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyView;
import com.genband.kandy.api.services.calls.statistics.IKandyCallGeneralAudioRTPStatistics;
import com.genband.kandy.api.services.calls.statistics.IKandyCallGeneralVideoRTPStatistics;
import com.genband.kandy.api.services.calls.statistics.IKandyCallReceiveAudioRTPStatistics;
import com.genband.kandy.api.services.calls.statistics.IKandyCallReceiveVideoRTPStatistics;
import com.genband.kandy.api.services.calls.statistics.IKandyCallSendAudioRTPStatistics;
import com.genband.kandy.api.services.calls.statistics.IKandyCallSendVideoRTPStatistics;
import com.genband.kandy.api.services.calls.statistics.IKandyCallStatistics;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.genband.kandy.api.services.common.KandyMediaState;
import com.genband.kandy.api.services.common.KandyMissedCallMessage;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyWaitingVoiceMailMessage;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.genband.kandy.api.utils.KandyLog;
import com.tx.mcc.utils.KandyLogger;
import com.txt.library.base.SystemBase;

/**
 * Created by pc on 2016/8/18.
 */
public class SystemKandy extends SystemBase implements KandyCallServiceNotificationListener {
    private final static String TAG="SystemKandy";
    private String KEY_VERSION = "version";

    private String API_KEY="DAK9d52eeb8475d48f5aa7d05f796a2f697";
    private String API_SECRET="DAS86c13043ce3e42349e8ba0f213a107d8";

//    //shuzu
//    private String API_KEY="DAK26ae7cb662b24f509ec14e179067e343";
//    private String API_SECRET="DAS11894478cfc14b2483e5847ed5fbdb8e";
    private boolean isCurrentStateTalking=false;
    public String mAgentCall;
    public  boolean isLogin=false;
    private IKandyCall mCurrentCall=null;
    private KandyCallListener mlistener;
    private Handler mRevicePortHandler=new Handler();
    private boolean isStartRevice=true;
    private Handler mConnectHandler=new Handler();
    private Runnable mConnectRunnable=new Runnable() {
        @Override
        public void run() {
            mConnectHandler.postDelayed(mConnectRunnable,3000);
        }
    };

    @Override
    public void init() {
        Kandy.getKandyLog().setLogLevel(KandyLog.Level.VERBOSE);
        Kandy.initialize(mContext, API_KEY, API_SECRET);
        Kandy.getKandyLog().setLogger(new KandyLogger());
        IKandyGlobalSettings settings = Kandy.getGlobalSettings();
        settings.setKandyHostURL("https://api.kandycn.com");
        Kandy.getGlobalSettings().setWebRTCLogsEnabled(true);

        settings.setPowerSaverEnable(false);
        registerCallListener();
        mConnectHandler.postDelayed(mConnectRunnable,0);
    }

    public void setKandyCallListener(KandyCallListener listener){
        if (listener!=null)
            mlistener=listener;
    }
    /**
     * 账号，密码登录
     * @param domain
     * @param password
     * @param listener
     */
    public void userLogin(final String domain, final String password, final KandyLoginResponseListener listener){
        KandyRecord kandyRecord = null;
        try {
            kandyRecord = new KandyRecord(domain);
        } catch (KandyIllegalArgumentException e) {
            return;
        }
        Kandy.getAccess().login(kandyRecord, password, new KandyLoginResponseListener() {
            @Override
            public void onLoginSucceeded() {
                isLogin=true;
                if (listener!=null){
                    listener.onLoginSucceeded();
                }
            }

            @Override
            public void onRequestFailed(int i, String s) {
                if (listener!=null){
                    listener.onRequestFailed(i,s);
                }
            }
        });
    }
    /**
     * token登录
     * @param
     * @param listener
     */
    public void userLogin(String s, KandyLoginResponseListener listener){
        Kandy.getAccess().login(s,listener);
    }

    /**
     * 用户退出
     * @param listener
     */
    public void userLogout(KandyLogoutResponseListener listener){
        Kandy.getAccess().logout(listener);
    }

   // KandyView localView,KandyView roteView
    public void doCall(final String number , final boolean isVideo, boolean ispstn, final CallRequestCallBack callBack){
        boolean bIsPSTNCall = ispstn;
        if (bIsPSTNCall){
            mCurrentCall= Kandy.getServices().getCallService().createPSTNCall(null, number, KandyOutgoingPSTNCallOptions.NONE);
        }else {
            KandyRecord callee=null;
            try {
                callee=new KandyRecord(number);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
            }
            mCurrentCall= Kandy.getServices().getCallService().createVoipCall(null,callee,isVideo? KandyOutgingVoipCallOptions.START_CALL_WITH_VIDEO: KandyOutgingVoipCallOptions.AUDIO_ONLY_CALL);
        }
        if (mCurrentCall==null){
            if (callBack!=null){
                callBack.onRequrestFailer();
            }
            return;
        }
        mCurrentCall.getCallee().getUri();
        mCurrentCall.getCallee().getUserName();
        ((IKandyOutgoingCall) mCurrentCall).establish(new KandyCallResponseListener() {
            @Override
            public void onRequestSucceeded(IKandyCall call) {
                if (callBack!=null){
                    callBack.onRequrestSuccess();
                }
            }
            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                if (callBack!=null){
                    callBack.onRequrestFailer();
                }
            }
        });
    }

    public void stopCamer(KandyCallResponseListener listener){
        if (mCurrentCall!=null){
            mCurrentCall.stopVideoSharing(listener);
        }
    }

    public void doCall(final String number, KandyView localView, KandyView roteView, final boolean isVideo, boolean ispstn, final CallRequestCallBack callBack){
        Log.d(TAG,"number"+number);
        boolean bIsPSTNCall = ispstn;
        if (bIsPSTNCall){
            Log.i(TAG,"PSTN");
            mCurrentCall= Kandy.getServices().getCallService().createPSTNCall(null, number, KandyOutgoingPSTNCallOptions.NONE);
        }else {
            Log.i(TAG,"VOIP");
            KandyRecord callee=null;
            try {
                callee=new KandyRecord(number);
            } catch (KandyIllegalArgumentException e) {
                e.printStackTrace();
            }
            mCurrentCall= Kandy.getServices().getCallService().createVoipCall(null,callee,isVideo? KandyOutgingVoipCallOptions.START_CALL_WITH_VIDEO: KandyOutgingVoipCallOptions.AUDIO_ONLY_CALL);
        }
        if (mCurrentCall==null){
            if (callBack!=null){
                callBack.onRequrestFailer();
            }
            return;
        }
        mCurrentCall.setRemoteVideoView(roteView);
        mCurrentCall.setLocalVideoView(localView);
        mCurrentCall.getCallee().getUri();
        mCurrentCall.getCallee().getUserName();
        ((IKandyOutgoingCall) mCurrentCall).establish(new KandyCallResponseListener() {
            @Override
            public void onRequestSucceeded(IKandyCall call) {
                Log.i(TAG, "doCall:onRequestSucceeded: true");
                if (callBack!=null){
                    callBack.onRequrestSuccess();
                }
            }
            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                Log.i(TAG, "onRequestFailed: " + err + " Response code: " + responseCode);
                if (callBack!=null){
                    callBack.onRequrestFailer();
                }
            }
        });
    }

    //获取短信验证码
    public void getSmsCode(final String phoneNum, final KandyRequestCallBack callBack){
        String twoLetterISOCountryCode = "CN";
        String callerPhonePrefix = null;
        Kandy.getProvisioning().requestCode(KandyValidationMethoud.SMS, phoneNum, twoLetterISOCountryCode, callerPhonePrefix, new KandyResponseListener() {
            @Override
            public void onRequestSucceded() {
                if (callBack!=null){
                    callBack.onRequrestSuccess();
                }
            }

            @Override
            public void onRequestFailed(int i, String s) {
                Log.d(TAG,"ERR"+s+"errcode"+i);
                if (callBack!=null){
                    callBack.onRequrestFailer(i,s);
                }
            }
        });
    }

    //挂断处理
    public  void hangUp(final CallRequestCallBack callBack){
        if (mCurrentCall!=null){
            mCurrentCall.hangup(new KandyCallResponseListener() {
                @Override
                public void onRequestSucceeded(IKandyCall iKandyCall) {
                    Log.d(TAG,"hangup onRequestSucceeded");
                    mCurrentCall=null;
                    if (callBack!=null){
                        callBack.onRequrestSuccess();
                    }
                }

                @Override
                public void onRequestFailed(IKandyCall iKandyCall, int code, String err) {
                    Log.d(TAG,"hangup onRequestFailed:"+err+">>>code"+code);
                    mCurrentCall=null;
                    if (callBack!=null){
                        callBack.onRequrestFailer();
                    }
                }
            });
        }else {
            if (callBack!=null){
                callBack.onRequrestSuccess();
            }
        }
    }

    /**
     * 电话和短信验证注册
     * @param smsCode
     */
    public void phoneAndSmsCodeValidate(final String smsCode, final String phoneNumber, final KandyRequestCallBack callBack){
        if (smsCode.equals("")&&phoneNumber.equals("")){
            return;
        }
        String twoLetterISOCountryCode = "CN";
        Kandy.getProvisioning().validateAndProvision(phoneNumber, smsCode, twoLetterISOCountryCode, new KandyValidationResponseListener() {
            @Override
            public void onRequestSuccess(IKandyValidationResponse iKandyValidationResponse) {
                if (callBack!=null){
                    callBack.onRequrestSuccess();
                }
            }

            @Override
            public void onRequestFailed(int i, String s) {
                Log.d(TAG,"err："+s+"errCode"+i);
                if (callBack!=null){
                    callBack.onRequrestFailer(i,s);
                }
            }
        });
    }

    /**
     * 拒接电话
     */
    public void rejectIncomingCall() {
        if (mCurrentCall==null){
            return;
        }
        ((IKandyIncomingCall) mCurrentCall).reject(new KandyCallResponseListener() {
            @Override
            public void onRequestSucceeded(IKandyCall call) {
                mCurrentCall=null;
                Log.i(TAG, "mCurrentIncomingCall.reject succeeded");
            }
            @Override
            public void onRequestFailed(IKandyCall call, int responseCode, String err) {
                mCurrentCall=null;
                Log.i(TAG, "mCurrentIncomingCall.reject. Error: " + err + "\nResponse code: " + responseCode);
            }
        });
    }

    /**
     * 获取当前通话的Current
     * @return
     */
    public IKandyCall getmCurrentCall(){
        return mCurrentCall;
    }

    /**
     * 获得callee的名字
     * @return
     */
    public String getCalleeName(){
        if (mCurrentCall==null){
            return null;
        }
        return mCurrentCall.getCallee().getUserName();
    }

    /**
     * 获得callee的uri
     * @return
     */
    public String getCalleeUri(){
        if (mCurrentCall==null){
            return null;
        }
        return mCurrentCall.getCallee().getUri();
    }

    /**
     * 获得callee的Domain
     * @return
     */
    public String getCalleeDomain(){
        if (mCurrentCall==null){
            return null;
        }
        return mCurrentCall.getCallee().getDomain();
    }

    //接收通话
    public void accep(KandyCallResponseListener listener){
        if (mCurrentCall==null){
            return;
        }
        boolean flag=false;
        if (mCurrentCall.canReceiveVideo()){
            flag=true;
        }
        ((IKandyIncomingCall)mCurrentCall).accept(flag,listener);
    }

    @Override
    public void destorySystem() {
        super.destorySystem();
        Log.d(TAG, "destorySystem: ");
        if (mCurrentCall!=null){
            mCurrentCall=null;
        }
        unregisterCallListener();
        unregisterCallListener();
    }
    private KandyCallState mKandyState= KandyCallState.TERMINATED;
    @Override
    public void onIncomingCall(IKandyIncomingCall iKandyIncomingCall) {
       if (mCurrentCall!=null){
           isCurrentStateTalking=true;
            iKandyIncomingCall.reject(new KandyCallResponseListener() {
                @Override
                public void onRequestSucceeded(IKandyCall iKandyCall) {
                    Log.d(TAG, "onRequestSucceeded:onIncomingCall ");
                }

                @Override
                public void onRequestFailed(IKandyCall iKandyCall, int i, String s) {
                    Log.d(TAG, "onRequestSucceeded:onIncomingCall ");
                }
            });
            return;
        }
        mCurrentCall=iKandyIncomingCall;
        if (mlistener!=null) {
            if (mCurrentCall.canReceiveVideo()) {
                mAgentCall=mCurrentCall.getCallee().getUri();
                Log.d(TAG, "onIncomingCall: mAgentCall"+mAgentCall);
                mlistener.onIncomingCall(true);
            } else {
                mlistener.onIncomingCall(false);
            }
        }
    }
    @Override
    public void onMissedCall(KandyMissedCallMessage kandyMissedCallMessage) {
        Log.d(TAG,"onMissedCall");
    }

    @Override
    public void onWaitingVoiceMailCall(KandyWaitingVoiceMailMessage kandyWaitingVoiceMailMessage) {
        Log.d(TAG,"onWaitingVoiceMailCall");
    }

    @Override
    public void onCallStateChanged(KandyCallState kandyCallState, IKandyCall iKandyCall) {
        if (kandyCallState == KandyCallState.TERMINATED) {
            if (isCurrentStateTalking){
                isCurrentStateTalking=false;
                return;
            }
            if (iKandyCall != null) {
                if (mlistener!=null){
                    mlistener.onTenminaten(iKandyCall.getTerminationReason().getStatusCode());
                }
            }
            isStartRevice=false;
            mCurrentCall=null;
        } else if (kandyCallState == KandyCallState.TALKING) {
            mHandler=new Handler();
            mHandler.postDelayed(mGetRepotRunnable,0);
            if (mlistener!=null){
                mlistener.onTalking();
                isStartRevice=true;
            }
        }else if (kandyCallState == KandyCallState.RINGING){
            if (mlistener!=null){
                mlistener.isRing();
            }
        }
        mKandyState=kandyCallState;
    }

    public void doMute(KandyCallResponseListener listener) {
        if (mCurrentCall==null){
            return;
        }
        mCurrentCall.mute(listener);
    }
    public void doUnMute(KandyCallResponseListener listener) {
        if (mCurrentCall==null){
            return;
        }
        mCurrentCall.unmute(listener);
    }

    private void registerCallListener() {
        Log.d(TAG, "registerCallListener()");
        Kandy.getServices().getCallService().registerNotificationListener(this);
    }

    private void unregisterCallListener() {
        Log.d(TAG, "unregisterCallListener()");
        Kandy.getServices().getCallService().unregisterNotificationListener(this);
    }
    @Override
    public void onVideoStateChanged(IKandyCall iKandyCall, boolean b, boolean b1) {
        Log.d(TAG,"onVideoStateChanged"+b+b1);
    }

    @Override
    public void onMediaStateChanged(IKandyCall iKandyCall, KandyMediaState kandyMediaState) {

    }

    @Override
    public void onGSMCallIncoming(IKandyCall iKandyCall, String s) {
        Log.d(TAG,"onGSMCallIncoming");
    }

    @Override
    public void onGSMCallConnected(IKandyCall iKandyCall, String s) {
        Log.d(TAG,"onGSMCallConnected");
    }

    @Override
    public void onGSMCallDisconnected(IKandyCall iKandyCall, String s) {
        Log.d(TAG,"onGSMCallDisconnected");
    }
    interface KandyRequestCallBack{
        void onRequrestSuccess();
        void onRequrestFailer(int code, String err);
    }

    public interface CallRequestCallBack{
        void onRequrestSuccess();
        void onRequrestFailer();
    }

    public void switchCamer(boolean isFront, KandyResponseListener listener){
        Log.d(TAG, "switchCamer: switchCamer");
        if (mCurrentCall==null){
            return;
        }
        if (isFront){
            mCurrentCall.switchCamera(KandyCameraInfo.FACING_FRONT,getSystem(SystemKandySetting.class).mFrontSize,listener);
        }else {
            mCurrentCall.switchCamera(KandyCameraInfo.FACING_BACK,getSystem(SystemKandySetting.class).mFrontSize,listener);
        }
    }

    public void startCamer(){
        if (mCurrentCall!=null){
            mCurrentCall.startVideoSharing(new KandyCallResponseListener() {
                @Override
                public void onRequestSucceeded(IKandyCall iKandyCall) {
                    Log.d(TAG, "onRequestSucceeded: ");
                }

                @Override
                public void onRequestFailed(IKandyCall iKandyCall, int i, String s) {
                    Log.d(TAG, "onRequestFailed: i"+i+"s"+s);
                }
            });
        }
    }

    public interface KandyCallListener{
        void onTalking();
        void onTenminaten(int code);
        void onIncomingCall(boolean isVideo);
        void isRing();
    }

    private Handler mHandler;
    private Runnable mGetRepotRunnable=new Runnable() {
        @Override
        public void run() {
            getReport(mCurrentCall);
            mHandler.postDelayed(mGetRepotRunnable,5*1000);
        }
    };

    public void deactivate(final onCallDeactivateCallBack callBack){
        Kandy.getProvisioning().deactivate(new KandyResponseListener() {
            @Override
            public void onRequestSucceded() {
                if (callBack!=null)
                     callBack.onSuccess();
            }
            @Override
            public void onRequestFailed(int i, String s) {
               if (callBack!=null)
                    callBack.onFail(s,i);
            }
        });
    }

    public interface onCallDeactivateCallBack{
        public void onSuccess();
        public void onFail(String err, int errcode);
    }


    private boolean IsStartSaveInfo=true;
    public void getReport(IKandyCall call) {
        if (call != null&&IsStartSaveInfo) {
            call.getRTPStatistics(new KandyCallRTPStatisticsListener() {
                @Override
                public void onKandyCallRTPStatisticsReceived(IKandyCallStatistics iKandyCallStatistics) {
                    iKandyCallStatistics.getRawData();
                    IKandyCallGeneralAudioRTPStatistics gAudio=iKandyCallStatistics.getKandyCallGeneralAudioRTPStatistcs();
                    IKandyCallGeneralVideoRTPStatistics gVideo=iKandyCallStatistics.getKandyCallGeneralVideoRTPStatistics();
                    IKandyCallReceiveAudioRTPStatistics raudio = iKandyCallStatistics.getKandyCallReceiveAudioRTPStatistics();
                    IKandyCallReceiveVideoRTPStatistics rvideo = iKandyCallStatistics.getKandyCallReceiveVideoRTPStatistics();
                    IKandyCallSendAudioRTPStatistics saudio = iKandyCallStatistics.getKandyCallSendAudioRTPStatistics();
                    IKandyCallSendVideoRTPStatistics svideo = iKandyCallStatistics.getKandyCallSendVideoRTPStatistics();
                /*    Log.d(TAG,"IKandyCallGeneralAudioRTPStatistics"+gAudio.toJson());
                    Log.d(TAG,"IKandyCallGeneralVideoRTPStatistics"+gVideo.toJson());
                    Log.d(TAG,"IKandyCallReceiveAudioRTPStatistics"+raudio.toJson());
                    Log.d(TAG,"IKandyCallReceiveVideoRTPStatistics"+rvideo.toJson());
                    Log.d(TAG,"IKandyCallSendAudioRTPStatistics"+saudio.toJson());
                    Log.d(TAG,"IKandyCallSendVideoRTPStatistics"+svideo.toJson());
                    Log.d(TAG,"getRawData"+iKandyCallStatistics.getRawData());*/
                    Log.d(TAG, "rvideo: getBytesReceived:"+rvideo.getBytesReceived());

                }
            });
        }
    }

    public interface onReceiveVideoCallBack{
        public void onVideoRevice(int videoByte);
    }





}
