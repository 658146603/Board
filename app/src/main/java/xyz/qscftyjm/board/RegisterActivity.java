package xyz.qscftyjm.board;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import postutil.AsynTaskUtil;
import tools.ParamToString;
import tools.StringCollector;

public class RegisterActivity extends AppCompatActivity {

    static String TAG = "Board";

    private EditText set_nickname;
    private EditText set_password;
    private EditText confirm_password;
    private Button submit_request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        set_nickname=findViewById(R.id.register_nickname);
        set_password=findViewById(R.id.register_password);
        confirm_password=findViewById(R.id.register_confirm_password);
        submit_request=findViewById(R.id.register_submit);


        submit_request.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                String nickname=set_nickname.getText().toString();
                String password=set_password.getText().toString();
                String confirm=confirm_password.getText().toString();
                if(!nickname.equals("")&&!password.equals("")&&!confirm.equals("")) {
                    if(password.equals(confirm)) {
                        if(nickname.length()<2||nickname.length()>15) {
                            Toast.makeText(RegisterActivity.this, "用户名长度应该在2~15位，请重新修改用户名长度", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if(password.length()<6||password.length()>18) {
                            Toast.makeText(RegisterActivity.this, "密码长度应该在6~18位，请重新修改密码强度", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Toast.makeText(RegisterActivity.this, password, Toast.LENGTH_SHORT).show();
                        AsynTaskUtil.AsynNetUtils.post(StringCollector.getUserServer(), ParamToString.formRegister(nickname, password), new AsynTaskUtil.AsynNetUtils.Callback() {

                            @Override
                            public void onResponse(String response) {
                                String result=response;
                                Log.d(TAG,response);
                                JSONObject jsonObj;
                                if(response!=null) {
                                    try {
                                        jsonObj=new JSONObject(result);
                                        int code=jsonObj.optInt("code", -1);
                                        if(code==0) {

                                            Toast.makeText(RegisterActivity.this, "注册成功，即将跳转登录界面", Toast.LENGTH_SHORT).show();
                                            AlertDialogUtil.makeRegisterResultDialog(RegisterActivity.this, jsonObj.optString("userid", "null"), jsonObj.optString("nickname", "null"));
                                            //finish();

                                        } else if(code<0) {
                                            Toast.makeText(RegisterActivity.this,jsonObj.optString("msg","未知错误"),Toast.LENGTH_LONG).show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Toast.makeText(RegisterActivity.this, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }else {
                        Toast.makeText(RegisterActivity.this, "两次密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
                        set_password.setText("");
                        confirm_password.setText("");
                    }
                }else {
                    Toast.makeText(RegisterActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
                }

            }
        });

    }
}
