package com.tx.mcc.dialog;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.genband.kandy.api.services.calls.KandyView;
import com.tx.mcc.R;

/**
 * Created by DELL on 2017/8/25.
 */
public class ShowRemoteDialog extends Dialog {
    private final static String TAG=ShowRemoteDialog.class.getSimpleName();
    public KandyView mRemoteView;
    public RelativeLayout mRootKandyView;
    public View mRootview;
    public ShowRemoteDialog(@NonNull Context context) {
        super(context, R.style.remote_dialog_style);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        mRootview= LayoutInflater.from(context).inflate(R.layout.dialog_kandyview,null);
        setContentView(mRootview);
        initView();
        //设置对话框属性
        Window dialogWindow=getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        WindowManager.LayoutParams lp=dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.TOP|Gravity.RIGHT);
        lp.width=WindowManager.LayoutParams.MATCH_PARENT;
        lp.height=WindowManager.LayoutParams.MATCH_PARENT;
        dialogWindow.setAttributes(lp);
    }

    private void initView() {
        mRemoteView= (KandyView)mRootview.findViewById(R.id.remotedialogview);
        mRootKandyView= (RelativeLayout)mRootview.findViewById(R.id.dialog_root);
    }

    public void hideKandyView(){
        mRootKandyView.setVisibility(View.GONE);
    }

    public void showKandyView(){
        mRootKandyView.setVisibility(View.VISIBLE);
    }

    public KandyView getRemoteKandyView(){
        return mRemoteView;
    }
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
