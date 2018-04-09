package com.txt.library.system;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import com.genband.mobile.core.WebRTC.view.SMSurfaceViewRenderer;
import com.txt.library.base.SystemBase;
import com.txt.library.base.SystemManager;
import com.txt.library.utils.FileUtils;

import org.webrtc.EglRenderer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by DELL on 2017/4/18.
 */
public class SystemCommon extends SystemBase {
    private final String TAG=SystemCommon.class.getSimpleName();
    private CameraManager manager;// 声明CameraManager对象
    public String mMoblieModle;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };

    public void verifyStoragePermissions(Activity activity) {
        try {
            //检测是否有写的权限
            int permission = ActivityCompat.checkSelfPermission(activity,
                    "android.permission.WRITE_EXTERNAL_STORAGE");
            if (permission != PackageManager.PERMISSION_GRANTED) {
                // 没有写的权限，去申请写的权限，会弹出对话框
                ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE,REQUEST_EXTERNAL_STORAGE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isFADMoblie(){

        if (mMoblieModle==null&&mMoblieModle.equals("")){
            return false;
        }else if (mMoblieModle.toUpperCase().equals("HONOR")||mMoblieModle.toUpperCase().equals("HUAWEI")){
            return true;
        }
        return false;
    }



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void init() {
        manager = (CameraManager)mContext.getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] camerList = manager.getCameraIdList();

            for (String str : camerList) {
                Log.d(TAG, "init: ");
            }
        } catch (CameraAccessException e) {
            Log.e("error", e.getMessage());
        }

    }

    public void lightSwitch(final boolean lightStatus) {
        if (lightStatus) { // 关闭手电筒
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", false);
                } catch (Exception e) {
                    Log.d(TAG, "lightSwitch: "+e);
                    e.printStackTrace();
                }
            } else {

            }
        } else { // 打开手电筒

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    manager.setTorchMode("0", true);
                } catch (Exception e) {
                    Log.d(TAG, "lightSwitch: "+e);
                    e.printStackTrace();
                }
            } else {

            }
        }
    }


    /**
     * 判断Android系统版本是否 >= M(API23)
     */
    private boolean isM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        } else {
            return false;
        }
    }




    /**
     * dip转pix
     * @param context
     * @param dp
     * @return
     */
    public static int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }



    public static Bitmap savePixels(int x, int y, int w, int h, GL10 gl)
    {
        int b[]=new int[w*(y+h)];
        int bt[]=new int[w*h];
        IntBuffer ib= IntBuffer.wrap(b);
        ib.position(0);
        gl.glReadPixels(x, 0, w, y+h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
        for(int i=0, k=0; i<h; i++, k++)
        {//remember, that OpenGL bitmap is incompatible with Android bitmap
            for(int j=0; j<w; j++)
            {
                int pix=b[i*w+j];
                int pb=(pix>>16)&0xff;
                int pr=(pix<<16)&0x00ff0000;
                int pix1=(pix&0xff00ff00) | pr | pb;
                bt[(h-k-1)*w+j]=pix1;
            }
        }
        Bitmap bp=Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
        return bp;
    }

    public void screenShot(int width,int heigh,String filepath,ShotScreentCallBack callback)
    {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        Log.d(TAG, "screenShot: egl"+egl.toString());
        GL10 gl = (GL10)egl.eglGetCurrentContext().getGL();
        Log.d(TAG, "screenShot: egl"+gl.toString());
        Bitmap bp = savePixels(0, 0, width, heigh, gl);
        savePic(bp,filepath,callback);
    }


    // 保存到sdcard
    public void savePic(Bitmap b, String strFileName,ShotScreentCallBack callback) {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(strFileName);
            if (null != fos) {
                b.compress(Bitmap.CompressFormat.PNG, 90, fos);
                b.recycle();
                fos.flush();
                fos.close();
                if (callback!=null){
                    callback.shotscrren(strFileName);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            if (callback!=null){
                callback.shotscrren(null);
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.shotscrren(null);
        }
    }

    public interface ShotScreentCallBack{
        public void shotscrren(String path);
    }


    //退出应用
    public void exitApp(){
        SystemManager.getInstance().destoryAllSysrtm();
        //android.os.Process.killProcess(android.os.Process.myPid());//获取PID
        System.exit(0);
    }


    public String imageToBase64(String path){
        if(TextUtils.isEmpty(path)){
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try{
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data,Base64.DEFAULT);
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if(null !=is){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
    }


    public String imgToBase64(String imageptah){
        Bitmap bitmap = BitmapFactory.decodeFile(imageptah);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] bytes = baos.toByteArray();
        byte[] encode = Base64.encode(bytes,Base64.DEFAULT);
        String result=new String(encode);
        return result;
    }

    public int getScreenWidth(Activity activity){
        DisplayMetrics metrics =new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int width = metrics.widthPixels;
        return width;
    }
    public int getScreenHigh(Activity activity){
        DisplayMetrics metrics =new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int heigh = metrics.heightPixels;
        return heigh;
    }


    //判断网络的连接状态
    public static boolean isNetworkAvailable(final Context context) {
        boolean hasWifoCon = false;
        boolean hasMobileCon = false;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfos = cm.getAllNetworkInfo();
        for (NetworkInfo net : netInfos) {
            String type = net.getTypeName();
            if (type.equalsIgnoreCase("WIFI")) {
                if (net.isConnected()) {
                    hasWifoCon = true;
                }
            }
            if (type.equalsIgnoreCase("MOBILE")) {
                if (net.isConnected()) {
                    hasMobileCon = true;
                }
            }
        }
        return hasWifoCon || hasMobileCon;
    }

    public boolean isOritation(Context context){
        int flag=0;
        try {
            int screenchange = Settings.System.getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
            flag=screenchange;
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return flag==0?false:true;
    }

    public void moveToBack(Context context){
        Intent intent=new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        context.startActivity(intent);
    }




    public String getClipData(Context context){
        String matchStr="flowId=";
        ClipboardManager manager= (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (!manager.hasPrimaryClip()){
            return "";
        }
        ClipData data=manager.getPrimaryClip();
        ClipData.Item item = data.getItemAt(0);
        String text=item.getText().toString();
        Log.d(TAG, "getClipData: "+text);
        String[] datas=text.split(matchStr);
        String dataText="";
        ClipData data1=ClipData.newPlainText("text",dataText);
        manager.setPrimaryClip(data1);
        if (datas==null||datas.length<2){
            return "";
        }
        return datas[1];
    }


    //截取kandy视频的图片
    public  void screenShotFromSurfaceView(final SMSurfaceViewRenderer smsSurfaceViewRenderer, final ScreenShotCallback callback){
        Log.d(TAG, "screenShotFromSurfaceView: ");
        if (smsSurfaceViewRenderer == null){
            if (callback!=null)
                Log.d(TAG, "screenShotFromSurfaceView: 1");
            callback.shotFail();
            return ;
        }

        try {
            Log.d(TAG, "screenShotFromSurfaceView: 2");
            Handler renderFrameHandler = null;
            Class surfaceViewRenderer =  smsSurfaceViewRenderer.getClass().getSuperclass();
            Field eglRendererF = surfaceViewRenderer.getDeclaredField("eglRenderer");
            eglRendererF.setAccessible(true);

            Object eglRendererO = eglRendererF.get(smsSurfaceViewRenderer);
            Class eglRendererClass = eglRendererO.getClass();
            if (eglRendererO instanceof EglRenderer) {

                EglRenderer eglRenderer = (EglRenderer)eglRendererO;
                Field renderFrameHandlerF = eglRendererClass.getDeclaredField("renderThreadHandler");
                renderFrameHandlerF.setAccessible(true);

                Object renderFrameHandlerO = renderFrameHandlerF.get(eglRenderer);
                if (renderFrameHandlerO instanceof Handler) {
                    renderFrameHandler = (Handler) renderFrameHandlerO;
                }
            }else {
                if (callback!=null)
                    callback.shotFail();
            }

            if (renderFrameHandler != null){
                renderFrameHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EGL10 egl = (EGL10) EGLContext.getEGL();

                        EGLDisplay display = egl.eglGetDisplay(egl.EGL_DEFAULT_DISPLAY);

                        EGLContext context = egl.eglGetCurrentContext();

                        int width = smsSurfaceViewRenderer.getWidth();
                        int height = smsSurfaceViewRenderer.getHeight();

                        Bitmap bm  = getBitmapFromGL(width,height, (GL10)context.getGL());
                        Matrix matrix = new Matrix();
                        matrix.setScale(0.5f, 0.5f);
                        Bitmap bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(),
                                bm.getHeight(), matrix, true);
                        String path=saveBitmap(bitmap);
                        bm.recycle();
                        if (path!=null&&!path.equals("")){
                            if (callback!=null)
                                callback.shotSuccess(path);
                            callback.shotSuccess(bitmap);
                        }else {
                            if (callback!=null)
                                callback.shotFail();
                        }
                    }
                });
            }else {
                if (callback!=null)
                    callback.shotFail();
            }
        }catch (NoSuchFieldException e) {
            e.printStackTrace();
            if (callback!=null)
                callback.shotFail();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            if (callback!=null)
                callback.shotFail();
        }
    }



    private static Bitmap getBitmapFromGL(int w, int h, GL10 gl) {
        int b[] = new int[w * (h)];
        int bt[] = new int[w * h];
        IntBuffer ib = IntBuffer.wrap(b);
        ib.position(0);

        gl.glFlush();
        gl.glReadPixels(0, 0, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, ib);
        for (int i = 0, k = 0; i < h; i++, k++) {
            for (int j = 0; j < w; j++) {
                int pix = b[i * w + j];
                int pb = (pix >> 16) & 0xff;
                int pr = (pix << 16) & 0xffff0000;
                int pix1 = (pix & 0xff00ff00) | pr | pb;
                bt[(h - k - 1) * w + j] = pix1;
            }
        }
        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.RGB_565);
    }

    private String saveBitmap(Bitmap bitmap) {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_SHARED.equals(state)) {
            // Sd card has connected to PC in MSC mode
        }
        String extPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File f = new File(extPath,System.currentTimeMillis()+"_shotscrren.jpg");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
            out.flush();
            out.close();

            FileInputStream inputStream=new FileInputStream(f);
            int size=inputStream.available()/1024;
            Log.d(TAG, "saveBitmap: size"+size);
            if(size>500){
                FileUtils.compressBitmap(f.getAbsolutePath(),500,f.getAbsolutePath());
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            f=null;
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            f=null;
        }
        if (f==null){
            return null;
        }
        return f.getAbsolutePath();
    }


    public interface ScreenShotCallback{
        public void shotSuccess(String imagePath);
        public void shotSuccess(Bitmap bitmap);
        public void shotFail();
    }
}
