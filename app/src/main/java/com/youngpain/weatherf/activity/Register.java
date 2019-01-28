package com.youngpain.weatherf.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.youngpain.weatherf.R;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Register extends AppCompatActivity {
    //控件
    private EditText nameEdit;
    private EditText pwdEdit;
    private Button regButton;
    private final int ERROR = 0;
    private final int REGISTER_FAIL = 1;
    private final int REGISTER_SUCCESS = 2;
    private final int USER_EXIST = 3;
    //保存用户信息
    private String username;
    private String password;

    // 构建Web端注册请求URL
    private final String url = "http://192.168.0.101:8080/user/register";
    //使用OkHttpClient进行网络请求
    private OkHttpClient httpClient = new OkHttpClient();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR:
                    //Toast弹出错误
                    Toast.makeText(Register.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case USER_EXIST:
                    //Toast弹出用户已存在
                    Toast.makeText(Register.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_FAIL:
                    //Toast弹出注册失败
                    Toast.makeText(Register.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    break;
                case REGISTER_SUCCESS:
                    //Toast弹出注册成功
                    Toast.makeText(Register.this, (String) msg.obj, Toast.LENGTH_SHORT).show();
                    //注册成功跳转到个人信息页面
                    startActivity(new Intent(Register.this, ShowWeather.class));
                    finish();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //去除标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.register);
        //初始化
        init();
    }

    private void init() {
        //获取控件
        nameEdit = findViewById(R.id.reg_et_user);
        pwdEdit = findViewById(R.id.reg_et_password);
        regButton = findViewById(R.id.bt_reg);
        //绑定监听事件
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取用户名和密码
                username = nameEdit.getText().toString().trim();
                password = pwdEdit.getText().toString().trim();
                if ("".equals(username) || "".equals(password)) {
                    Toast.makeText(Register.this, "用户名或密码不能为空"
                            , Toast.LENGTH_SHORT).show();
                } else {
                    //调用register()方法请求Web端
                    register(username, password);
                }
            }
        });
    }

    /**
     * 发送注册请求到Web端
     *
     * @param username
     * @param password
     */
    private void register(String username, String password) {
        //创建请求表单
        RequestBody body = new FormBody.Builder()
                .add("username", username)//添加用户名
                .add("password", password)//添加密码
                .build();
        //创建请求
        final Request request = new Request.Builder().url(url).post(body).build();
        //在子线程中获取服务器响应
        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = httpClient.newCall(request).execute();
                    //请求成功
                    if (response.isSuccessful()) {
                        String result = response.body().string();

                        Log.i("服务器返回的结果:", result);

                        Message message = Message.obtain();
                        if ("USER_EXIST".equals(result)) {
                            message.what = REGISTER_FAIL;//设置标志
                            message.obj = "用户已存在";//设置内容
                        }
                        if ("REG_SUCCESS".equals(result)) {
                            message.what = REGISTER_SUCCESS;//设置标志
                            message.obj = "注册成功";//设置内容
                        }
                        if ("REG_FAIL".equals(result)) {
                            message.what = REGISTER_FAIL;//设置标志
                            message.obj = "注册失败";//设置内容
                        }
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    Log.i("Register.java", "服务器异常:" + e.toString());

                    Message message = Message.obtain();
                    message.what = ERROR;
                    message.obj = e.toString();
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
