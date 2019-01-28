package com.youngpain.weatherf.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
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

public class Login extends AppCompatActivity {

    //控件
    private EditText nameEdit;
    private EditText pwdEdit;
    private Button loginButton;
    private Button notLoginButton;
    private Button registerButton;
    //定义状态码
    private final int ERROR = 0;
    private final int LOGIN_FAIL = 1;
    private final int LOGIN_SUCCESS = 2;
    //保存用户信息
    private String username;
    private String password;

    //构建Web端登录请求URL
    private final String url = "http://192.168.0.101:8080/user/login";

    //使用OkHttpClient进行网络请求
    private OkHttpClient httpClient = new OkHttpClient();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ERROR:
                    //获取Message中的内容
                    String message0 = (String) msg.obj;
                    //Toast弹出错误
                    Toast.makeText(Login.this, message0, Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_FAIL:
                    //获取Message中的内容
                    String message1 = (String) msg.obj;
                    //Toast弹出登录失败
                    Toast.makeText(Login.this, message1, Toast.LENGTH_SHORT).show();
                    break;
                case LOGIN_SUCCESS:
                    //获取Message中的内容
                    String message2 = (String) msg.obj;
                    //Toast弹出登录成功
                    Toast.makeText(Login.this, message2, Toast.LENGTH_SHORT).show();
                    //登录成功跳转到个人信息页面
                    startActivity(new Intent(Login.this, ShowWeather.class));
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
        setContentView(R.layout.login);
        //初始化
        init();
    }

    private void init() {
        //获取控件
        nameEdit = findViewById(R.id.login_et_user);
        pwdEdit = findViewById(R.id.login_et_password);
        loginButton = findViewById(R.id.bt_login);
        //绑定监听事件
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //获取用户名和密码
                username = nameEdit.getText().toString().trim();
                password = pwdEdit.getText().toString().trim();
                if ("".equals(username) || "".equals(password)) {
                    Toast.makeText(Login.this, "用户名或密码不能为空"
                            , Toast.LENGTH_SHORT).show();
                } else {
                    //调用login()方法请求Web端
                    login(username, password);
                }
            }
        });
        notLoginButton = findViewById(R.id.not_login);
        notLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, ShowWeather.class));
            }
        });
        registerButton = findViewById(R.id.register_user);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
    }

    /**
     * 发送登录请求到Web端
     *
     * @param username
     * @param password
     */
    private void login(String username, String password) {
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
                        if ("LOGIN_SUCCESS".equals(result)) {
                            message.what = LOGIN_SUCCESS;//设置标志
                            message.obj = "登录成功";//设置内容
                        }
                        if ("LOGIN_FAIL".equals(result)) {
                            message.what = LOGIN_FAIL;//设置标志
                            message.obj = "用户名或密码错误";//设置内容
                        }
                        handler.sendMessage(message);
                    } else {
                        Toast.makeText(Login.this, "服务器无响应", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.i("Login.java", "服务器异常:" + e.toString());

                    Message message = Message.obtain();
                    message.what = ERROR;
                    message.obj = e.toString();
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
