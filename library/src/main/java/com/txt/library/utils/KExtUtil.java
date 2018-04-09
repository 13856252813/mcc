package com.txt.library.utils;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.genband.mobile.core.WebRTC.WebRTCCallManager;
import com.genband.mobile.core.WebRTC.view.SMSurfaceViewRenderer;

import org.webrtc.Camera1Session;
import org.webrtc.Camera2Session;
import org.webrtc.EglRenderer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;


public class KExtUtil {
    static String tag = "KExtUtil";

    public static Boolean screenShotFromSurfaceView(final SMSurfaceViewRenderer smsSurfaceViewRenderer){

        if (smsSurfaceViewRenderer == null){
            return false;
        }

        try {
            Handler  renderFrameHandler = null;
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
            }

            if (renderFrameHandler != null){
                renderFrameHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        EGL10 egl = (EGL10) EGLContext.getEGL();
                        Log.d(tag, "egl:" + egl.toString());

                        EGLDisplay display = egl.eglGetDisplay(egl.EGL_DEFAULT_DISPLAY);
                        Log.d(tag, "display:" + display.toString());

                        EGLContext context = egl.eglGetCurrentContext();
                        Log.d(tag, "context:" + context.toString());

                        int width = smsSurfaceViewRenderer.getWidth();
                        int height = smsSurfaceViewRenderer.getHeight();

                        Log.d(tag, "getGL == " + context.getGL().toString());

                        Bitmap bm  = getBitmapFromGL(width,height, (GL10)context.getGL());

                        saveBitmap(bm);
                    }
                });
            }
        }catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static Boolean screenShotFromGLSurfaceView(final GLSurfaceView glSurfaceView){

        if (glSurfaceView == null){
            return false;
        }

        glSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                EGL10 egl = (EGL10) EGLContext.getEGL();
                Log.d(tag, "egl:" + egl.toString());

                EGLDisplay display = egl.eglGetDisplay(egl.EGL_DEFAULT_DISPLAY);
                Log.d(tag, "display:" + display.toString());

                EGLContext context = egl.eglGetCurrentContext();
                Log.d(tag, "context:" + context.toString());

                int width = glSurfaceView.getWidth();
                int height = glSurfaceView.getHeight();

                Log.d(tag, "getGL == " + context.getGL().toString());

                Bitmap bm  = getBitmapFromGL(width,height, (GL10)context.getGL());

                saveBitmap(bm);
            }
        });
        return true;
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
        return Bitmap.createBitmap(bt, w, h, Bitmap.Config.ARGB_8888);
    }


    private static void saveBitmap(Bitmap bitmap) {
        Log.d(tag, "saveBitmap");

        String state = Environment.getExternalStorageState();
        Log.d(tag, "state == " + state);
        if (Environment.MEDIA_SHARED.equals(state)) {
            // Sd card has connected to PC in MSC mode
        }

        String extPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
        File f = new File(extPath, "scmm.png");
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.d(tag, "saveBitmap success");
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private static void readGLESInfo() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        Log.d(tag, "egl:" + egl.toString());

        EGLSurface surface = egl.eglGetCurrentSurface(egl.EGL_DRAW);
        Log.d(tag, "surface:" + surface.toString());

        EGLDisplay display = egl.eglGetDisplay(egl.EGL_DEFAULT_DISPLAY);
        Log.d(tag, "display:" + display.toString());

        EGLContext context = egl.eglGetCurrentContext();
        Log.d(tag, "context:" + context.toString());

        String version = egl.eglQueryString(display, EGL10.EGL_VERSION);
        String extension = egl.eglQueryString(display, EGL10.EGL_EXTENSIONS);
        Log.d(tag, "GL version = " + version + "  extension = " + extension);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);
        Log.d(tag, "configurationsList = " + configurationsList[0]);

        for (int i = 0; i < totalConfigurations[0]; i++) {
            Log.d(tag, "GL i = " + i + "  configurationsList = " + configurationsList[i]);
        }

        int widthArray[] = new int[1];
        int heightArray[] = new int[1];
        for (int i = 0; i < totalConfigurations[0]; i++) {

            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, widthArray);
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_HEIGHT, heightArray);

            Log.d(tag, "GL i = " + i + "  width = " + widthArray[0] + " height = " + heightArray[0]);
        }

//        final int widthArray[] = new int[1];
//        egl.eglQuerySurface(display, surface, EGL10.EGL_WIDTH, widthArray);
//
//        final int heightArray[] = new int[1];
//        egl.eglQuerySurface(display, surface, EGL10.EGL_HEIGHT, heightArray);

    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static Boolean lightSwitch(Boolean isopent)
    {
        WebRTCCallManager webRTCCallManager = WebRTCCallManager.getInstance();
        if (webRTCCallManager == null){
            return false;
        }

        try {
            Class webRTCCallManagerC = webRTCCallManager.getClass();
            Field videoCapturerF = webRTCCallManagerC.getDeclaredField("videoCapturer");
            videoCapturerF.setAccessible(true);
            Object videoCapturerO = videoCapturerF.get(webRTCCallManager);
            Class videoCapturerSuperC = videoCapturerO.getClass().getSuperclass();

            Field currentSessionF = videoCapturerSuperC.getDeclaredField("currentSession");
            currentSessionF.setAccessible(true);

            Object currentSession0 = currentSessionF.get(videoCapturerO);
            Class currentSessionC = currentSession0.getClass();
            if (currentSession0 instanceof Camera1Session) {
                Camera1Session camera1Session = (Camera1Session) currentSession0;
                Field cameraF = currentSessionC.getDeclaredField("camera");
                cameraF.setAccessible(true);

                Object cameraO  = cameraF.get(camera1Session);
                if (cameraO != null  && (cameraO instanceof Camera)) {
                    Camera camera = (Camera) cameraO;
                    Parameters parameters = camera.getParameters();

                    if (isopent) {
                        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);// 关闭
                        camera.setParameters(parameters);
                    } else {
                        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);// 开启
                        camera.setParameters(parameters);
                    }
                    return true;
                }
            }else if (currentSession0 instanceof Camera2Session) {
                Camera2Session camera2Session = (Camera2Session) currentSession0;
                Field cameraManagerF = currentSessionC.getDeclaredField("cameraManager");
                cameraManagerF.setAccessible(true);

                Object cameraManagerO = cameraManagerF.get(camera2Session);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (cameraManagerO != null && (cameraManagerO instanceof CameraManager)) {
                        CameraManager cameraManager = (CameraManager) cameraManagerO;
                        if (!isopent) { // 关闭手电筒
                            cameraManager.setTorchMode("0", false);
                        } else {
                            cameraManager.setTorchMode("0", true);
                        }
                        return true;
                    }
                }
                return false;
            }
        }catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (CameraAccessException e){
            e.printStackTrace();
        }
        //videoCapturer
        return true;
    }
}



