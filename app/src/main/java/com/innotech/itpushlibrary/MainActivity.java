package com.innotech.itpushlibrary;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.innotech.innotechpush.utils.UserInfoSPUtils;
import com.innotech.innotechpush.utils.Utils;
import com.tencent.bugly.crashreport.CrashReport;

public class MainActivity extends Activity {
    EditText edAlias;
    public TextView txtGuid, txtNotiCustom;
    TextView txtSetAliasResult;
    private MyHandler myHandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        InnotechPushMethod.launcher(this);

        TestPushReciver.handler = myHandler;
    }

    private void initViews() {
        Button btnSetAlias = (Button) findViewById(R.id.btnAlias);
        txtGuid = (TextView) findViewById(R.id.txtGuid);
        txtNotiCustom = (TextView) findViewById(R.id.txtNotiCustom);
        String guid = UserInfoSPUtils.getString(this, UserInfoSPUtils.KEY_GUID, "null");
        txtGuid.setText(guid);
        edAlias = (EditText) findViewById(R.id.edAlias);
        txtSetAliasResult = (TextView) findViewById(R.id.txtSetAliasResult);
        String deviceInfo = "IMEI:" + Utils.getIMEI(this) + " AndroidID:" + Utils.getAndroidId(this) + " SerialNumber:" + Utils.getSerialNumber();

        btnSetAlias.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alias = getNewAliasName();
                if (alias != null) {
                    txtSetAliasResult.setText("");
                    String aliasStr = edAlias.getText().toString();
                    InnotechPushMethod.setAlias(MainActivity.this, aliasStr, new RequestCallback() {
                        @Override
                        public void onSuccess(String msg) {
                            Log.i("Innotech_Push", ">>>>>>>>>>>>setAlias onSuccess msg:" + msg);
                            updateUI("Set Alias success!");
                        }

                        @Override
                        public void onFail(String msg) {
                            Log.i("Innotech_Push", ">>>>>>>>>>>setAlias onFail msg:" + msg);
                            updateUI("Set Alias Fail!");
                        }
                    });
                } else {
                    Toast.makeText(MainActivity.this, "别名不能够为空", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void updateUI(String con) {
        Message msg = new Message();
        msg.what = 1;
        Bundle b = new Bundle();// 存放数据
        b.putString("con", con);
        msg.setData(b);
        if (myHandler != null) {
            myHandler.sendMessage(msg);
        }
    }

    private String getNewAliasName() {
        if (edAlias.getText() != null) {
            String alias = edAlias.getText().toString();
            return alias;
        }
        return null;
    }

    class MyHandler extends Handler {
        public MyHandler() {
        }

        // 子类必须重写此方法，接受数据
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            // 此处可以更新UI
            Bundle b = msg.getData();
            switch (msg.what) {
                case 1:
                    String con = b.getString("con");
                    txtSetAliasResult.setText(con);
                    break;
                case 2:
                    String guid = b.getString("guid");
                    txtGuid.setText(guid);
                    break;
                case 3:
                    String custom = b.getString("custom");
                    txtNotiCustom.setText(custom);
                    break;
                default:
                    break;
            }

        }
    }
}
