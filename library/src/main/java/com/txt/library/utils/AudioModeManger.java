package com.txt.library.utils;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

/**
 * Created by DELL on 2017/8/9.
 */

public class AudioModeManger {
    private AudioManager audioManager;
    private onSpeakerListener mOnSpeakerListener;
    private Context mContext;
    public AudioModeManger(Context context){
        mContext=context;
    }

    public interface onSpeakerListener{
        void onSpeakerChanged(boolean isSpeakerOn);
    }

    public void setOnSpeakerListener(onSpeakerListener listener){
        if (listener != null){
            mOnSpeakerListener = listener;
        }
    }



    /**
     * 注册距离传感器监听
     */
    public void register(){
        audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
    }

    public void unregister(){


    }


    public void setSpeakerInit() {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_NORMAL);
            //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamVolume(AudioManager.STREAM_MUSIC), AudioManager.FX_KEY_CLICK);
            if (mSpeakIconChangelistener!=null)
                mSpeakIconChangelistener.onSpeakIconChange(true);

    }

    public void setSpeakerPhoneOn(boolean on) {
        if (on) {
            audioManager.setSpeakerphoneOn(true);
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
            if (mSpeakIconChangelistener!=null)
                mSpeakIconChangelistener.onSpeakIconChange(true);

        } else {
            audioManager.setSpeakerphoneOn(false);
            //5.0以上
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
                //设置音量，解决有些机型切换后没声音或者声音突然变大的问题
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);

            } else {
                audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                        audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), AudioManager.FX_KEY_CLICK);
            }

            if (mSpeakIconChangelistener!=null)
                mSpeakIconChangelistener.onSpeakIconChange(false);
        }
    }

    private OnSpeakInconChange mSpeakIconChangelistener;
    public void setOnSpeakIconChange(OnSpeakInconChange listener){
        mSpeakIconChangelistener=listener;
    }


    public interface OnSpeakInconChange{
        public void onSpeakIconChange(boolean isSpeakerOn);
    }


    public boolean isWiredHeadsetOn(){

        return audioManager.isWiredHeadsetOn();
    }




}
