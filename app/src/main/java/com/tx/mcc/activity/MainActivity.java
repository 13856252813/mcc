package com.tx.mcc.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.tx.mcc.R;
import com.txt.library.base.BaseActivity;
import com.txt.library.base.Constant;
import com.txt.library.model.ServiceRequest;
import com.txt.library.system.SystemLogin;

import java.util.HashMap;

import static com.tx.mcc.R.id.customer1;
import static com.tx.mcc.R.id.customer2;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private Button mBtnCustomer;
    private static final String TAG = MainActivity.class.getSimpleName();

    private EditText mEditName, mEditPhone, mEditEmail,mEditMerchant,mEditStore;
    private RadioButton mRadioButton1, mRadioButton2;
    private RadioGroup mRadioGroup;
    private TextView mTextDepartMentId;

    private String departmentId1 = "5a7be0cca41421541e49b76b";
    private String departmentId2 = "5a7be0eea41421541e49b76c";

    private String departmentIdName = "5a7be0cca41421541e49b76b";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBtnCustomer = (Button) findViewById(R.id.btn_customer);
        mBtnCustomer.setOnClickListener(this);
        mEditName = (EditText) findViewById(R.id.edit_name);
        mEditPhone = (EditText) findViewById(R.id.edit_phone);
        mEditEmail = (EditText) findViewById(R.id.edit_email);
        mTextDepartMentId = (TextView) findViewById(R.id.departmentId);
        mEditMerchant= (EditText) findViewById(R.id.merchantId);
        mEditStore= (EditText) findViewById(R.id.storeId);

        mRadioButton1 = (RadioButton) findViewById(customer1);
        mRadioButton2 = (RadioButton) findViewById(customer2);
        mRadioGroup = (RadioGroup) findViewById(R.id.radio_g);
        mTextDepartMentId.setText(departmentIdName);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case customer1:
                        if (mRadioButton1.isChecked()) {
                            departmentIdName = departmentId1;
                            mTextDepartMentId.setText(departmentIdName);
                        }
                        break;
                    case customer2:
                        if (mRadioButton2.isChecked()) {
                            departmentIdName = departmentId2;
                            mTextDepartMentId.setText(departmentIdName);
                        }
                        break;
                }
            }
        });

        showConfigDialog();

        HashMap<String,String> map=new HashMap<>();
        map.put("a","sdds");
    }


    @Override
    public void onClick(View v) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String name = mEditName.getText().toString().trim();
                String phone = mEditPhone.getText().toString().trim();
                String email = mEditEmail.getText().toString().trim();
                String merchantId=mEditMerchant.getText().toString().trim();
                String storeId=mEditStore.getText().toString().trim();

                SystemLogin systemLogin = new SystemLogin(MainActivity.this);
                systemLogin.setDepartmentId(departmentIdName);
                ServiceRequest.UserInfoBean userInfoBean = new ServiceRequest.UserInfoBean();
                userInfoBean.setName(name);
                userInfoBean.setEmail(email);
                userInfoBean.setPhone(phone);
                userInfoBean.setMerchantId(merchantId);
                userInfoBean.setStoreId(storeId);
                systemLogin.loginToKandy(userInfoBean, new SystemLogin.LoginStatusCallBack() {
                    @Override
                    public void loginSuccess() {

                    }

                    @Override
                    public void loginFail(String err, int code) {
                    }
                });
            }
        });
    }


    public void showConfigDialog() {
        View view = getLayoutInflater().inflate(R.layout.config_layout, null);
        final EditText editIp = (EditText) view.findViewById(R.id.edit_IP);
        final EditText editPort = (EditText) view.findViewById(R.id.edit_port);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("配置IP，端口号");
        builder.setView(view);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String ip = editIp.getText().toString().trim();
                String port = editPort.getText().toString().trim();
                if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
                    Toast.makeText(MainActivity.this, "ip和端口号不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Constant.IP = ip;
                Constant.port = port;
            }
        });
        builder.create().show();
    }


}
