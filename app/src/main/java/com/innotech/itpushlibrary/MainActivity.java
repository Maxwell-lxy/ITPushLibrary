package com.innotech.itpushlibrary;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.innotech.innotechpush.InnotechPushMethod;
import com.innotech.innotechpush.callback.RequestCallback;
import com.meituan.robust.PatchExecutor;
import com.meituan.robust.patch.annotaion.Add;
import com.meituan.robust.patch.annotaion.Modify;

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_SDCARD_READ = 1;
    EditText edAlias;
    public TextView txtGuid, txtNotiCustom;
    TextView txtSetAliasResult;
    private MyHandler myHandler = new MyHandler();

    @Modify
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        InnotechPushMethod.launcher(this);

        TestPushReciver.handler = myHandler;

        //热更
        if (isGrantSDCardReadPermission()) {
            runRobust();
        } else {
            requestPermission();
        }
        Toast.makeText(this, getStringupdate(), Toast.LENGTH_LONG).show();
        Log.e("jimmy","这是热更上去的内容");
    }

    private void initViews() {
        Button btnSetAlias = findViewById(R.id.btnAlias);
        txtGuid = findViewById(R.id.txtGuid);
        txtNotiCustom = findViewById(R.id.txtNotiCustom);
        edAlias = findViewById(R.id.edAlias);
        txtSetAliasResult = findViewById(R.id.txtSetAliasResult);

        btnSetAlias.setOnClickListener(v -> {
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

    private boolean isGrantSDCardReadPermission() {
        return PermissionUtils.isGrantSDCardReadPermission(this);
    }

    private void requestPermission() {
        PermissionUtils.requestSDCardReadPermission(this, REQUEST_CODE_SDCARD_READ);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SDCARD_READ:
                handlePermissionResult();
                break;

            default:
                break;
        }
    }

    private void handlePermissionResult() {
        if (isGrantSDCardReadPermission()) {
            runRobust();
        } else {
            Toast.makeText(this, "failure because without sd card read permission", Toast.LENGTH_SHORT).show();
        }

    }

    private void runRobust() {
        new PatchExecutor(getApplicationContext(), new PatchManipulateImp(), new RobustCallBackSample()).start();
    }

    //增加方法
    @Add
    public String getStringupdate() {
        return "Robust";
    }
}
