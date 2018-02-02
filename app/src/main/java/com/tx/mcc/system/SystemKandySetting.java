package com.tx.mcc.system;

import android.hardware.Camera;
import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyCallSettings;
import com.genband.kandy.api.services.common.KandyCameraInfo;
import com.tx.mcc.utils.CameraControllerHelper;
import com.txt.library.base.SystemBase;

import java.util.List;

/**
 * Created by pc on 2016/11/18.
 */

public class SystemKandySetting extends SystemBase {
    private KandyCallSettings mCallSetting;
    private final static String TAG=SystemKandySetting.class.getSimpleName();
    public final static int NORMAL=720;
    public final static int HD=1280;
    private final static int  CAMERA_BACK=0;
    private final static int  CAMERA_FOUNT=1;
    private int CURRENT_ENABLE_CAMEA=1;
    public Camera.Size mFrontSize;
    @Override
    public void init() {
        mCallSetting= Kandy.getServices().getCallService().getSettings();
        initDefaultEnableCamer();
       // initDefaultSelution();
        mFrontSize=getCamerSize(true);
        Log.d(TAG, "init: mFrontSize"+mFrontSize);
    }
    private void initDefaultEnableCamer(){
        Camera.CameraInfo info = new Camera.CameraInfo();
        KandyCameraInfo kandyCameraInfo = KandyCameraInfo.FACING_BACK;
        setKandyCamera(kandyCameraInfo);
    }

    //设置Kandy可用的Camera
    private void setKandyCamera(KandyCameraInfo info){
        if(Kandy.getServices().getCallService().getSettings().getCameraMode() != info){
                Kandy.getServices().getCallService().getSettings().setCameraMode(info);
        }
    }

    //获取当前的摄像头是前置还是后置
    public int getCurrentCamera(){
        return CURRENT_ENABLE_CAMEA;
    }
    //设置分辨率
    public void setResolution(boolean front,final BaseCallBack callBack){
        Camera.Size size=getCamerSize(front);
        mFrontSize=size;
        if (size==null){
            callBack.onFail();
            return;
        }
        Log.d(TAG,"mCallSetting.getDefaultVideoResolution():width:"+size.width+"higth:"+size.height);
        CameraControllerHelper
                .createCustomSize(
                        size.width,
                        size.height,
                        new CameraControllerHelper.CameraControllerSingleSizeResponseListener() {
                            @Override
                            public void onSuccess(Camera.Size size) {
                                Log.d(TAG,"setResolution onSuccess:"+"width"+size.width+"heigth"+size.height);
                                mCallSetting.setDefaultVideoResolution(size);
                                callBack.onSuccess();
                            }

                            @Override
                            public void onFailed(String error) {
                                callBack.onFail();
                            }
                        });

    }
    //获取设定的Camera的Size
    private Camera.Size getCamerSize(boolean isFront){
        Camera camera= null;
        Camera.Size needSize=null;
        try{
            if (isFront){
                camera= Camera.open(FindFrontCamera());
            }else {
                camera= Camera.open(FindBackCamera());
            }
            Camera.Parameters parms = camera.getParameters();
            List<Camera.Size> sizes=parms.getSupportedPreviewSizes();

            for (Camera.Size size:sizes){
                //这边是获取Camera的分辨率列表 选择自己需要的分辨率选项
                Log.d(TAG," width"+size.width+"height"+size.height);
                if (size.width==1920&&size.height==1080){
                    needSize=size;
                }
            }
            if (needSize==null){
                needSize=sizes.get(0);
            }
            camera.release();
        }
        catch (RuntimeException e){
            e.printStackTrace();
        }
        return needSize;
    }
    @Override
    public void destorySystem() {
        super.destorySystem();
        mCallSetting=null;
    }

    public interface BaseCallBack{
        public void onFail();
        public void onSuccess();
    }

    //获得前置摄像头的ID
    private int FindFrontCamera(){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_FRONT ) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }

    //获得后置摄像头ID
    private int FindBackCamera(){
        int cameraCount = 0;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras(); // get cameras number

        for ( int camIdx = 0; camIdx < cameraCount;camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo ); // get camerainfo
            if ( cameraInfo.facing ==Camera.CameraInfo.CAMERA_FACING_BACK ) {
                // 代表摄像头的方位，目前有定义值两个分别为CAMERA_FACING_FRONT前置和CAMERA_FACING_BACK后置
                return camIdx;
            }
        }
        return -1;
    }

}
