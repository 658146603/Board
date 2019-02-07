package xyz.qscftyjm.board

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
import tools.AlertDialogUtil
import tools.ParamToString
import tools.StringCollector

class RegisterActivity : AppCompatActivity() {

    private var set_nickname: EditText? = null
    private var set_password: EditText? = null
    private var confirm_password: EditText? = null
    private var submit_request: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        set_nickname = findViewById(R.id.register_nickname)
        set_password = findViewById(R.id.register_password)
        confirm_password = findViewById(R.id.register_confirm_password)
        submit_request = findViewById(R.id.register_submit)


        submit_request!!.setOnClickListener(View.OnClickListener {
            val nickname = set_nickname!!.text.toString()
            val password = set_password!!.text.toString()
            val confirm = confirm_password!!.text.toString()
            if (nickname != "" && password != "" && confirm != "") {
                if (password == confirm) {
                    if (nickname.length < 2 || nickname.length > 15) {
                        Toast.makeText(this@RegisterActivity, "用户名长度应该在2~15位，请重新修改用户名长度", Toast.LENGTH_SHORT).show()
                        return@OnClickListener
                    }
                    if (password.length < 6 || password.length > 18) {
                        Toast.makeText(this@RegisterActivity, "密码长度应该在6~18位，请重新修改密码强度", Toast.LENGTH_SHORT).show()
                        return@OnClickListener
                    }

                    Toast.makeText(this@RegisterActivity, password, Toast.LENGTH_SHORT).show()
                    AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formRegister(nickname, password)) { response ->
                        Log.d(TAG, response)
                        val jsonObj: JSONObject
                        if (response != null) {
                            try {
                                jsonObj = JSONObject(response)
                                val code = jsonObj.optInt("code", -1)
                                if (code == 0) {

                                    Toast.makeText(this@RegisterActivity, "注册成功，即将跳转登录界面", Toast.LENGTH_SHORT).show()
                                    AlertDialogUtil.makeRegisterResultDialog(this@RegisterActivity, jsonObj.optString("userid", "null"), jsonObj.optString("nickname", "null"))
                                    //finish();

                                } else if (code < 0) {
                                    Toast.makeText(this@RegisterActivity, jsonObj.optString("msg", "未知错误"), Toast.LENGTH_LONG).show()
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                        } else {
                            Toast.makeText(this@RegisterActivity, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show()
                        }
                    }

                } else {
                    Toast.makeText(this@RegisterActivity, "两次密码不一致，请重新输入", Toast.LENGTH_SHORT).show()
                    set_password!!.setText("")
                    confirm_password!!.setText("")
                }
            } else {
                Toast.makeText(this@RegisterActivity, "用户名或密码不能为空", Toast.LENGTH_SHORT).show()
            }
        })

    }

    companion object {

        private val TAG = "Board"
    }
}
