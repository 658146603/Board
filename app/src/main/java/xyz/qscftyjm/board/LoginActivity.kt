package xyz.qscftyjm.board

import android.content.ContentValues
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import org.json.JSONException
import org.json.JSONObject

import postutil.AsyncTaskUtil
import tools.BoardDBHelper
import tools.ParamToString
import tools.StringCollector
import tools.TimeUtil

class LoginActivity : AppCompatActivity() {

    private var login_btn: Button? = null
    private var register_btn: Button? = null
    private var forget_password_btn: Button? = null
    private var userid_et: EditText? = null
    private var password_et: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        login_btn = findViewById(R.id.login_btn)
        register_btn = findViewById(R.id.register_btn)
        forget_password_btn = findViewById(R.id.forget_password_btn)
        userid_et = findViewById(R.id.login_account)
        password_et = findViewById(R.id.login_password)
        val bundle = this.intent.extras
        if (bundle != null && bundle.containsKey("userid")) {
            userid_et!!.setText(bundle.getString("userid"))
            Toast.makeText(this@LoginActivity, "请重新登录您的账号", Toast.LENGTH_LONG).show()
        }

        login_btn!!.setOnClickListener {
            val input_account = userid_et!!.text.toString()
            val input_password = password_et!!.text.toString()
            if (input_account != "" && input_password != "") {

                val account = input_account


                AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formLogin(account, input_password)) { response ->
                    val jsonObj: JSONObject
                    if (response != null) {
                        Log.d(TAG, response)
                        try {
                            jsonObj = JSONObject(response)
                            val code = jsonObj.optInt("code", -1)
                            if (code == 0) {
                                Toast.makeText(this@LoginActivity, "欢迎 " + jsonObj.optString("nickname") + " ！正在跳转到主界面......", Toast.LENGTH_SHORT).show()
                                val sqLiteHelper = BoardDBHelper.getMsgDBHelper(this@LoginActivity)
                                val database = sqLiteHelper.writableDatabase

                                val values = ContentValues()
                                values.put("userid", account)
                                values.put("nickname", jsonObj.optString("nickname", "null"))
                                values.put("token", jsonObj.optString("token", "null"))
                                values.put("email", jsonObj.optString("email", "youremail@server.com"))
                                values.put("checktime", TimeUtil.time)
                                values.put("portrait", jsonObj.optString("portrait", "00000000").toByteArray())
                                values.put("priority", jsonObj.optInt("priority", -1))
                                database.insert("userinfo", null, values)
                                Log.d(TAG, "添加账号数据 $account")

                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                startActivity(intent)

                                finish()
                            } else if (code < 0) {
                                Toast.makeText(this@LoginActivity, jsonObj.optString("msg", "未知错误"), Toast.LENGTH_LONG).show()
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    } else {
                        Toast.makeText(this@LoginActivity, "网络或服务器错误", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this@LoginActivity, "用户名或密码不能为空！", Toast.LENGTH_SHORT).show()
            }
        }

        register_btn!!.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        forget_password_btn!!.setOnClickListener { Toast.makeText(this@LoginActivity, "忘记密码 该功能将在后续推出，敬请期待", Toast.LENGTH_SHORT).show() }

    }

    companion object {

        private val TAG = "Board"
    }
}
