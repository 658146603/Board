package xyz.qscftyjm.board;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
import tools.TimeUtil;

public class LoginActivity extends AppCompatActivity {

    static String TAG ="Board";

    private Button login_btn,register_btn,forget_password_btn;
    private EditText userid_et,password_et;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Bundle bundle=this.getIntent().getExtras();

        setContentView(R.layout.activity_login);
        login_btn=findViewById(R.id.login_btn);
        register_btn=findViewById(R.id.register_btn);
        forget_password_btn=findViewById(R.id.forget_password_btn);
        userid_et=findViewById(R.id.login_account);
        password_et=findViewById(R.id.login_password);

        if(bundle!=null&&bundle.containsKey("userid")) {
            userid_et.setText(bundle.getString("userid"));
        }

        login_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String input_account=userid_et.getText().toString();
                String input_password=password_et.getText().toString();
                if(!input_account.equals("")&&!input_password.equals("")) {

                    //input_password= MD5Util.getMd5(input_password);
                    //Toast.makeText(LoginActivity.this, "Account : "+input_account+" Password : "+input_password, Toast.LENGTH_SHORT).show();
                    final String account =input_account;
                    final String password=input_password;


                    AsynTaskUtil.AsynNetUtils.post(StringCollector.getUserServer(), ParamToString.formLogin(account, password), new AsynTaskUtil.AsynNetUtils.Callback() {

                        @Override
                        public void onResponse(String response) {

                            //Toast.makeText(LoginActivity.this, result, Toast.LENGTH_SHORT).show();
                            JSONObject jsonObj;
                            if(response!=null) {
                                String result=response;
                                Log.d(TAG,result);
                                try {
                                    jsonObj=new JSONObject(response);
                                    int code=jsonObj.optInt("code",-1);
                                    //Toast.makeText(LoginActivity.this, String.valueOf(status), Toast.LENGTH_SHORT).show();
                                    if(code==0) {
                                        Toast.makeText(LoginActivity.this, "欢迎 "+jsonObj.optString("nickname")+" ！正在跳转到主界面......", Toast.LENGTH_SHORT).show();
                                        BoardDBHelper sqLiteHelper=BoardDBHelper.getMsgDBHelper(LoginActivity.this);
                                        SQLiteDatabase database=sqLiteHelper.getWritableDatabase();

                                        ContentValues values = new ContentValues();
                                        values.put("userid",account);
                                        values.put("nickname",jsonObj.optString("nickname","null"));
                                        values.put("token",jsonObj.optString("token","null"));
                                        values.put("email",jsonObj.optString("email","youremail@server.com"));
                                        values.put("checktime", TimeUtil.getTime());
                                        values.put("portrait",jsonObj.optString("portrait","00000000").getBytes());
                                        values.put("priority",jsonObj.optInt("priority",-1));
                                        database.insert("userinfo",null,values);
                                        Log.d(TAG, "添加账号数据 "+account);

                                        Intent intent=new Intent(LoginActivity.this, MainActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }else if(code<0) {
                                        Toast.makeText(LoginActivity.this,jsonObj.optString("msg","未知错误"),Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }else {
                                Toast.makeText(LoginActivity.this, "网络或服务器错误", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                }else {
                    Toast.makeText(LoginActivity.this, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show();
                }
            }
        });

        register_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //Toast.makeText(LoginActivity.this, "注册账号-正在跳转...", Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        forget_password_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(LoginActivity.this, "忘记密码 该功能将在后续推出，敬请期待", Toast.LENGTH_SHORT).show();

            }
        });

    }
}
