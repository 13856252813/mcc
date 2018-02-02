package com.tx.mcc.system;

import android.net.Uri;
import android.util.Log;

import com.genband.kandy.api.Kandy;
import com.genband.kandy.api.services.calls.KandyRecord;
import com.genband.kandy.api.services.calls.KandyRecordType;
import com.genband.kandy.api.services.chats.IKandyMediaItem;
import com.genband.kandy.api.services.chats.IKandyMessage;
import com.genband.kandy.api.services.chats.IKandyTransferProgress;
import com.genband.kandy.api.services.chats.KandyChatMessage;
import com.genband.kandy.api.services.chats.KandyChatServiceNotificationListener;
import com.genband.kandy.api.services.chats.KandyDeliveryAck;
import com.genband.kandy.api.services.chats.KandyMessageBuilder;
import com.genband.kandy.api.services.chats.KandyReadMessage;
import com.genband.kandy.api.services.common.KandyResponseListener;
import com.genband.kandy.api.services.common.KandyUploadProgressListener;
import com.genband.kandy.api.services.events.KandyTypingIndicationItem;
import com.genband.kandy.api.utils.KandyIllegalArgumentException;
import com.tx.mcc.model.ChatSend;
import com.txt.library.base.SystemBase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import static com.genband.kandy.api.services.chats.KandyMessageBuilder.createText;

/**
 * Created by DELL on 2017/8/22.
 */
public class SystemChat extends SystemBase implements KandyChatServiceNotificationListener {
    public final static int MSG_TYPE_TXT=1;
    public final static int MSG_TYPE_IMG=2;
    public final static int MSG_TYPE_VIDEO=3;
    public final static int MSG_TYPE_AUDIO=4;
    public final static int MSG_TYPE_GPS=5;
    public final static int MSG_TYPE_CONTACT=6;
    public final static int MSG_TYPE_FILE=7;
    public String mCurrentUser;
    private onChatReceiveListener mListener;
    private static final String TAG=SystemChat.class.getSimpleName();

    @Override
    public void init() {
        registerChatsNotifications();
    }

    private void registerChatsNotifications() {
        Log.d(TAG, "registerChatsNotifications");
        Kandy.getServices().getChatService().registerNotificationListener(this);
    }


    private KandyRecord getRecipient(String destination, boolean isGroupChatMode) {
        KandyRecord recipient = null;
        try {
            if(isGroupChatMode) {
                recipient = new KandyRecord(destination, KandyRecordType.GROUP);
            } else {
                recipient = new KandyRecord(destination);
            }
        } catch (KandyIllegalArgumentException ex) {
            return null;
        }

        return recipient;
    }

    private IKandyMediaItem getMediaItem(int type, ChatSend send) {
        IKandyMediaItem mediaItem=null;
        Log.d(TAG, "getMediaItem: Uri"+send.getMediaPath());
        switch (type){
            case MSG_TYPE_IMG:
                try {
                    mediaItem= KandyMessageBuilder.createImage("",send.getMediaPath());
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_VIDEO:
                try {
                    mediaItem= KandyMessageBuilder.createVideo("",send.getMediaPath());
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;

            case MSG_TYPE_AUDIO:
                try {
                    mediaItem= KandyMessageBuilder.createAudio("",send.getMediaPath());
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_GPS:
                mediaItem= KandyMessageBuilder.createLocation("", send.getLocation());
                break;
            case MSG_TYPE_CONTACT:
                try {
                    Log.d(TAG, "getMediaItem: "+send.getMediaPath());
                    mediaItem= KandyMessageBuilder.createContact("",send.getMediaPath());
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
            case MSG_TYPE_FILE:
                try {
                    mediaItem= KandyMessageBuilder.createFile("",send.getMediaPath());
                } catch (KandyIllegalArgumentException e) {
                    e.printStackTrace();
                }
                break;
        }
        return mediaItem;
    }


    public void sendChatWithMessage(String chatid,String txtMsg,String type,JSONObject addDataMsg,final boolean isGroupChat, final OnChatRequestCallBack callBack){
        if (chatid==null){
            if (callBack!=null){
                callBack.onFail(-1,"chatid is null");
            }
            return;
        }
            Log.d(TAG, "sendChatWithMessage: chatid"+chatid);
            KandyRecord recipient = getRecipient(chatid,isGroupChat);
            KandyChatMessage message =null;
            IKandyMediaItem mediaItem=createText(txtMsg);
            JSONObject jsonObject=new JSONObject();
            try {
                jsonObject.put("type",type);
                jsonObject.put("subType",addDataMsg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        Log.d(TAG, "sendChatWithMessage: additionData"+jsonObject);
            mediaItem.setAdditionalData(jsonObject);
            message = new KandyChatMessage(recipient,mediaItem);

        if (1==MSG_TYPE_TXT){
            Kandy.getServices().getChatService().sendChat(message, new KandyResponseListener() {
                @Override
                public void onRequestSucceded() {
                    if (callBack!=null){
                        callBack.onSuccess();
                    }
                }
                @Override
                public void onRequestFailed(int i, String s) {
                    if (callBack!=null){
                        callBack.onFail(i,s);
                    }
                }
            });
        }else {
            Kandy.getServices().getChatService().sendChat(message, new KandyUploadProgressListener() {
                @Override
                public void onProgressUpdate(IKandyTransferProgress iKandyTransferProgress) {
                    if (callBack!=null){
                        callBack.onProgressUpdate(iKandyTransferProgress);
                    }
                }
                @Override
                public void onRequestSucceded() {
                    if (callBack!=null){
                        callBack.onSuccess();
                    }
                }
                @Override
                public void onRequestFailed(int i, String s) {
                    if (callBack!=null){
                        callBack.onFail(i,s);
                    }
                }
            });
        }
    }

    public void setOnChatMsgReceiveListener(onChatReceiveListener listener){
                mListener=listener;
    }
    @Override
    public void onChatReceived(IKandyMessage iKandyMessage, KandyRecordType kandyRecordType) {
        Log.d(TAG,"onChatReceived"+iKandyMessage+">>KandyRecordType"+kandyRecordType);
        Log.d(TAG,"iKandyMessage"+iKandyMessage.getMessageType());
        Log.d(TAG,"iKandyMessage"+mCurrentUser);
        Log.d(TAG,"iKandyMessage"+iKandyMessage.getSender().getUri());
        if (mCurrentUser.equals(iKandyMessage.getSender().getUri())){
            return;
        }
        Log.d(TAG,"iKandyMessage"+2);
        onChatImcomingReceived(iKandyMessage,kandyRecordType);
    }

    public void onChatImcomingReceived(IKandyMessage iKandyMessage, KandyRecordType kandyRecordType){
        Log.d(TAG, ": getRecipient getUri ");
        KandyChatMessage chatMessage=(KandyChatMessage)iKandyMessage;
        //message.uuid=String.valueOf(iKandyMessage.getUUID());
        Log.d(TAG, ": getRecipient getUri "+iKandyMessage.getRecipient().getUri());
        //message.isIncoming=1;
        //message.transferState= ChatStateonChatImcomingReceived.TRANSFER_STATE_INIT;
        if (kandyRecordType== KandyRecordType.GROUP){
           // message.chatId=iKandyMessage.getRecipient().getUri();
            //message.sourceUri=chatMessage.getSender().getUri();
            //android.util.Log.d(TAG, "sourceUri: "+message.sourceUri);
        }else {
            //message.chatId=chatMessage.getSender().getUri();
        }
        IKandyMediaItem kandyMediaItem = iKandyMessage.getMediaItem();
        if (kandyMediaItem.getAdditionalData()==null){
            return;
        }
        String data=kandyMediaItem.getAdditionalData().toString();
        if (data!=null&&mListener!=null){
            mListener.onChatReceive(data);
        }
        return;
    }

    @Override
    public void onChatDelivered(List<KandyDeliveryAck> list) {
    }


    @Override
    public void onChatRead(List<KandyReadMessage> list) {
    }

    @Override
    public void onChatMediaAutoDownloadProgress(IKandyMessage iKandyMessage, IKandyTransferProgress iKandyTransferProgress) {

    }

    @Override
    public void onChatMediaAutoDownloadFailed(IKandyMessage iKandyMessage, int i, String s) {

    }

    @Override
    public void onChatMediaAutoDownloadSucceded(IKandyMessage iKandyMessage, Uri uri) {

    }

    @Override
    public void onUserTypingIndicationReceived(KandyTypingIndicationItem kandyTypingIndicationItem, KandyRecord kandyRecord, KandyRecord kandyRecord1) {

    }

    public interface OnChatRequestCallBack{
        void onSuccess();
        void onFail(int errCode, String errMsg);
        void onProgressUpdate(IKandyTransferProgress iKandyTransferProgress);
    }

    public interface  onChatReceiveListener{
         void onChatReceive(String chatMsg);
    }
}
