package com.tx.mcc.https;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by pc on 2017/3/21.
 */
public class HttpRequestClient {
    private static final String TAG = HttpRequestClient.class.getSimpleName();
    private OkHttpClient mOkhttpClient;
    private static HttpRequestClient mInstance;
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public HttpRequestClient() {
        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[]{xtm}, new SecureRandom());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        mOkhttpClient = new OkHttpClient().newBuilder()
                .readTimeout(5000, TimeUnit.MILLISECONDS)
                .sslSocketFactory(sslContext.getSocketFactory())
                .hostnameVerifier(DO_NOT_VERIFY)
                .build();
    }

    public static HttpRequestClient getIntance() {
        if (mInstance == null) {
            mInstance = new HttpRequestClient();
        }
        return mInstance;
    }
    //上传文件
    public void postFile(final String uri, final Map<String, Object> map,final File file,final RequestHttpCallBack callBack){
        MultipartBody.Builder requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
        if(file != null){
            // MediaType.parse() 里面是上传的文件类型。
            RequestBody body = RequestBody.create(MediaType.parse("file/*"), file);
            String filename = file.getName();
            // 参数分别为， 请求key ，文件名称 ， RequestBody
            requestBody.addFormDataPart("file", file.getName(), body);
        }
        if (map!=null){
            for (Map.Entry entry : map.entrySet()) {
                requestBody.addFormDataPart(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
            }
        }
        Request request = new Request.Builder().url(uri).post(requestBody.build()).build();
        Call call=mOkhttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: err" + e.getMessage() + "errCode" + e.hashCode());
                if (callBack != null) {
                    callBack.onFail(e.getMessage(), e.hashCode());
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()){

                    String result = response.body().string();
                    try {
                        JSONObject jsonObject= new JSONObject(result);
                        if (jsonObject.has("errCode")){
                           String errCode=jsonObject.getString("errCode");
                           if (errCode.equals("0")){
                               if (callBack != null) {
                                   callBack.onSuccess(new JSONObject(result).getString("result"));
                               }
                           }else {
                               if (callBack != null) {
                                   callBack.onFail("request Fail", -1);
                               }
                           }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        if (callBack != null) {
                            callBack.onFail("request Fail", -1);
                        }
                    }

                }else {
                    if (callBack != null) {
                        callBack.onFail("request Fail", -1);
                    }
                }

            }
        });




    }

    //get请求
    public void get(String url, final RequestHttpCallBack callBack) {
        Log.d(TAG, "get: url" + url);
        Request request = new Request.Builder().url(url).build();
        Call call=mOkhttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: err" + e.getMessage() + "errCode" + e.hashCode());
                if (callBack != null) {
                    callBack.onFail(e.getMessage(), e.hashCode());
                }
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d(TAG, "onResponse: result" + result);
                String errCode = null;
                try {
                    JSONObject jsonObject= new JSONObject(result);
                    if (jsonObject.has("errCode")){
                        errCode=jsonObject.getString("errCode");
                        if (errCode!=null){
                                if (errCode!=null&&errCode.equals("0")){
                                    if (callBack != null) {
                                        callBack.onSuccess(new JSONObject(result).getString("result"));
                                    }
                                }else {
                                    if (callBack != null) {
                                        callBack.onFail(new JSONObject(result).getString("errInfo"),Integer.parseInt(errCode));
                                    }
                                 }
                            }else {
                                if (callBack != null) {
                                    callBack.onFail("fail",-1);
                                }
                            }
                    }else {
                        callBack.onSuccess(result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void put(String url,String json,String aecsssToken,final RequestHttpCallBack callBack){
        Log.d(TAG, "put: url" + url);
        Log.d(TAG, "put: json"+json);
        RequestBody body = RequestBody.create(JSON, json);
        Request request=null;
        if (!aecsssToken.equals("")){
            request = new Request.Builder()
                    .url(url).header("access-token",aecsssToken).addHeader("token",aecsssToken)
                    .put(body)
                    .build();
        }else {
            request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .build();
        }
        Log.d(TAG, "post: request.toString()"+request.toString());
        Call call=mOkhttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callBack != null) {
                    callBack.onFail(e.getMessage(), e.hashCode());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d(TAG, "onResponse: result" + result);
                String errCode = null;
                try {

                    JSONObject jsonObject=new JSONObject(result);
                    errCode=jsonObject.getString("errCode");
                    if (errCode!=null&&errCode.equals("0")){
                        if (callBack != null) {
                           if (jsonObject.has("result")){
                                callBack.onSuccess(jsonObject.getString("result"));
                           }else {
                               callBack.onSuccess("");
                           }
                        }
                    }else {
                        if (callBack != null) {
                            callBack.onFail(new JSONObject(result).getString("errInfo"),Integer.parseInt(errCode));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    //post请求
    public void post(String url, String json,String aecsssToken ,final RequestHttpCallBack callBack) {
        Log.d(TAG, "post: url" + url);
        Log.d(TAG, "post: json"+json);
        RequestBody body = RequestBody.create(JSON, json);
        Request request=null;
        if (!aecsssToken.equals("")){
         request = new Request.Builder()
                .url(url).header("access-token",aecsssToken).addHeader("token",aecsssToken)
                .post(body)
                .build();
        }else {
            request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
        }
        Log.d(TAG, "post: request.toString()"+request.toString());
        Call call= mOkhttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callBack != null) {
                    callBack.onFail(e.getMessage(), e.hashCode());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d(TAG, "onResponse: result" + result);
                String errCode = null;
                try {
                    errCode=new JSONObject(result).getString("errCode");
                    if (errCode!=null&&errCode.equals("0")){
                        if (callBack != null) {
                            callBack.onSuccess(new JSONObject(result).getString("result"));
                        }
                    }else {
                        if (callBack != null) {
                            callBack.onFail(new JSONObject(result).getString("errInfo"),Integer.parseInt(errCode));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }



    public void delect(String url,String json,final RequestHttpCallBack callBack){
        RequestBody body = RequestBody.create(JSON, json);
        Request request= new Request.Builder()
                .url(url)
                .delete(body)
                .build();
        Call call=mOkhttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callBack != null) {
                    callBack.onFail(e.getMessage(), e.hashCode());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d(TAG, "onResponse: result" + result);
                if (callBack != null) {
                    callBack.onSuccess(result);
                }
            }
        });
    }

    public interface RequestHttpCallBack {
        public void onSuccess(String json);
        public void onFail(String err, int code);
    }
    X509TrustManager xtm = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            X509Certificate[] x509Certificates = new X509Certificate[0];
            return x509Certificates;
        }
    };

}
