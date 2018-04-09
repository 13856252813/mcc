package com.txt.library.system;

import android.util.Log;

import com.genband.mobile.impl.utilities.JsonParser;
import com.google.gson.Gson;
import com.txt.library.base.Constant;
import com.txt.library.base.SystemBase;
import com.txt.library.https.HttpRequestClient;
import com.txt.library.model.RequstMoudle;
import com.txt.library.model.ServiceRequest;

/**
 * Created by DELL on 2017/5/23.
 */

public class SystemHttpRequest extends SystemBase {
//    public String IP="192.168.1.65";
//    public String port="60223";
    public String IP="shuzu.ikandy.cn";
    public String port="60800";

    private  String TAG=SystemHttpRequest.class.getSimpleName();

    @Override
    public void init() {
    }

    public String getUri(){
        return "https://"+ Constant.IP+":"+ Constant.port;
    }


    /**
     * 获取kandy账号兵并且登录
     */
    public void getKandyAccount(HttpRequestClient.RequestHttpCallBack callBack){
        String api="/api/kandyUser";
        String Url=getUri()+api;
        HttpRequestClient.getIntance().get(Url, callBack);
    }

    public void postServiceRequests(ServiceRequest request, HttpRequestClient.RequestHttpCallBack callback){
        String api="/api/serviceRequests";
        StringBuilder builder=new StringBuilder(getUri()+api);
        if (request==null){
            Log.d(TAG, "postServiceRequests: request is null");
            return;
        }
        String json= JsonParser.toJSON(request);
        Log.d(TAG, "postServiceRequests: json"+json);
        HttpRequestClient.getIntance().post(builder.toString(),json,"",callback);
    }

    /**
     * 获取当前的排队数
     * @param requestId
     * @param callback
     */
    public void getReuestCount(String requestId, HttpRequestClient.RequestHttpCallBack callback){
        String api="/api/numberInWaitingQueue/";
        StringBuilder builder=new StringBuilder(getUri()+api+requestId);
        HttpRequestClient.getIntance().get(builder.toString(),callback);
    }


    //结束会话
    public void endSession(String requestId,String userAccessToken,HttpRequestClient.RequestHttpCallBack callback){
        String api="/api/serviceRequests/endSession";
        StringBuilder builder=new StringBuilder(getUri()+api);
        RequstMoudle moudle=new RequstMoudle();
        moudle.id=requestId;
        String json=new Gson().toJson(moudle);
        HttpRequestClient.getIntance().post(builder.toString(),json,userAccessToken,callback);
    }
}
