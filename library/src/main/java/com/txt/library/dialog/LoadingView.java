package com.txt.library.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.txt.library.R;

/**
 * Created by pc on 2018/2/6.
 */

public class LoadingView extends Dialog {


    private TextView mTextTip;
    private View view;

    public LoadingView(@NonNull Context context) {
        super(context, R.style.remote_dialog_style);
        view = LayoutInflater.from(context).inflate(R.layout.loging_view, null);
        mTextTip = (TextView) view.findViewById(R.id.view_tip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(view);
    }

    public void show(String tip) {
        mTextTip.setText(tip);
        show();
    }
}
