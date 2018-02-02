/*******************************************************************************
 * Copyright 2015 锟� GENBAND US LLC, All Rights Reserved
 *
 * This software embodies materials and concepts which are
 * proprietary to GENBAND and/or its licensors and is made
 * available to you for use solely in association with GENBAND
 * products or services which must be obtained under a separate
 * agreement between you and GENBAND or an authorized GENBAND
 * distributor or reseller.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER
 * AND/OR ITS LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * THE WARRANTY AND LIMITATION OF LIABILITY CONTAINED IN THIS
 * AGREEMENT ARE FUNDAMENTAL PARTS OF THE BASIS OF GENBAND锟絊 BARGAIN
 * HEREUNDER, AND YOU ACKNOWLEDGE THAT GENBAND WOULD NOT BE ABLE TO
 * PROVIDE THE PRODUCT TO YOU ABSENT SUCH LIMITATIONS.  IN THOSE
 * STATES AND JURISDICTIONS THAT DO NOT ALLOW CERTAIN LIMITATIONS OF
 * LIABILITY, GENBAND锟絊 LIABILITY SHALL BE LIMITED TO THE GREATEST
 * EXTENT PERMITTED UNDER APPLICABLE LAW.
 *
 * Restricted Rights legend:
 * Use, duplication, or disclosure by the U.S. Government is
 * subject to restrictions set forth in subdivision (c)(1) of
 * FAR 52.227-19 or in subdivision (c)(1)(ii) of DFAR 252.227-7013.
 *******************************************************************************/
package com.tx.mcc.utils;

import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;

import com.genband.kandy.api.services.common.KandyCameraInfo;

import java.util.List;

/**
 * <p>Helper class to interact with {@link Camera} APIs</p>
 * <ul>
 * <li><strong>Camera Permission</strong> - Your application must request permission to use a device
 * camera.
 * <pre class="prettyprint"><span class="tag">&lt;uses-permission</span><span class="pln"> </span><span class="atn">android:name</span><span class="pun">=</span><span class="atv">"android.permission.CAMERA"</span><span class="pln"> </span><span class="tag">/&gt;</span></pre>
 * <p class="note"><strong>Note:</strong> If you are using the camera <a href="http://developer.android.com/guide/topics/media/camera.html#intents">via an
 * intent</a>, your application does not need to request this permission.</p>
 * </li>
 * <li><strong>Camera Features</strong> - Your application must also declare use of camera features,
 * for example:
 * <pre class="prettyprint"><span class="tag">&lt;uses-feature</span><span class="pln"> </span><span class="atn">android:name</span><span class="pun">=</span><span class="atv">"android.hardware.camera"</span><span class="pln"> </span><span class="tag">/&gt;</span></pre>
 * <p>For a list of camera features, see the manifest
 * <a href="http://developer.android.com/guide/topics/manifest/uses-feature-element.html#hw-features">Features
 * Reference</a>.</p>
 * <p>Adding camera features to your manifest causes Google Play to prevent your application from
 * being installed to devices that do not include a camera or do not support the camera features you
 * specify. For more information about using feature-based filtering with Google Play, see <a href="http://developer.android.com/guide/topics/manifest/uses-feature-element.html#market-feature-filtering">Google
 * Play and Feature-Based Filtering</a>.</p>
 * <p>If your application <em>can use</em> a camera or camera feature for proper operation, but does
 * not <em>require</em> it, you should specify this in the manifest by including the <code>android:required</code> attribute, and setting it to <code>false</code>:</p>
 * <pre class="prettyprint"><span class="tag">&lt;uses-feature</span><span class="pln"> </span><span class="atn">android:name</span><span class="pun">=</span><span class="atv">"android.hardware.camera"</span><span class="pln"> </span><span class="atn">android:required</span><span class="pun">=</span><span class="atv">"false"</span><span class="pln"> </span><span class="tag">/&gt;</span></pre>
 * <p/>
 * </ul>
 */
public class CameraControllerHelper {

    public interface CameraControllerResponseListener
    {
        public void onSuccess(List<Camera.Size> sizes);
        public void onFailed(String error);
    }

    public interface CameraControllerSingleSizeResponseListener
    {
        public void onSuccess(Camera.Size size);
        public void onFailed(String error);
    }

    /**
     * Gets the supported video frame sizes
     * @param cameraInfo
     * @param listener
     */
    public static void getSupportedVideoSizes(final KandyCameraInfo cameraInfo, final CameraControllerResponseListener listener){
        Log.d("wdy","CameraControllerHelper:getSupportedVideoSizes: " + "cameraInfo: " + cameraInfo + " listener: " + listener + "");
        new AsyncTask<Void, Void, List<Camera.Size>>() {

            @Override
            protected List<Camera.Size> doInBackground(Void... voids) {
                List<Camera.Size> sizes = null;
                Camera camera = null;
                int cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                try {
                    if (cameraInfo != null)
                    {
                        switch (cameraInfo)
                        {
                            case FACING_FRONT:
                                cameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                                break;
                            default:
                                cameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

                        }
                    }
                    camera = Camera.open(cameraId);
                    Camera.Parameters parameters = camera.getParameters();
                    sizes = parameters.getSupportedVideoSizes();
                } catch (RuntimeException e) {
                    if (listener != null)
                    {
                        Log.e("wdy","CameraControllerHelper:doInBackground: " + "voids: " + voids + "" + e.getLocalizedMessage());
                        listener.onFailed(e.getLocalizedMessage());
                    }
                } finally {
                    if (camera != null)
                    {
                        camera.release();
                    }
                }
                return sizes;
            }

            @Override
            protected void onPostExecute(List<Camera.Size> sizes) {
                if (listener != null && sizes != null)
                {
                    listener.onSuccess(sizes);
                }
            }
        }.execute();

    }

    /**
     * <p>
     * Create a {@link Camera.Size} Object using the provided height and width.
     * </p>
     * <p/>
     *
     * @param height
     * @param width
     */
    public static void createCustomSize(final int width, final int height, final CameraControllerSingleSizeResponseListener listener)
    {
        new AsyncTask<Void, Void, Camera.Size>() {

            @Override
            protected Camera.Size doInBackground(Void... voids) {
                Camera.Size size = null;
                Camera camera = null;
                try {
                    camera = Camera.open(0);
                    size = camera.new Size(width, height);
                } catch (RuntimeException e) {
                    if (listener != null)
                    {
                        listener.onFailed(e.getLocalizedMessage());
                    }
                } finally {
                    if (camera != null)
                    {
                        camera.release();
                    }
                }
                return size;
            }

            @Override
            protected void onPostExecute(Camera.Size size) {
                if (listener != null && size != null)
                {
                    listener.onSuccess(size);
                }
            }
        }.execute();
    }

    /**
     *
     * @param size
     * @param sizes
     * @return {@code true} if sizes contains size. otherwise {@code false}
     */
    public static boolean contains(Camera.Size size, List<Camera.Size> sizes)
    {
        if (size == null || sizes == null || sizes.isEmpty())
        {
            return false;
        }

        for (Camera.Size s: sizes)
        {
            if (s.width==size.width && s.height==size.height)
            {
                return true;
            }
        }

        return false;
    }


}
