package xyz.qscftyjm.board

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import org.json.JSONException
import org.json.JSONObject

import java.util.Objects

import postutil.AsyncTaskUtil
import tools.MD5Util
import tools.ParamToString
import tools.StringCollector

class ChangePasswordFragment : DialogFragment() {
    private var userid: String? = null
    private var oldpassword: String? = null
    private var newpassword: String? = null
    private var ed_userid: EditText? = null
    private var ed_old_password: EditText? = null
    private var ed_new_password: EditText? = null
    private var ed_confirm_password: EditText? = null
    private var bt_sunmit: Button? = null

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        Objects.requireNonNull<FragmentActivity>(activity).getWindowManager().getDefaultDisplay().getMetrics(dm)
        dialog.window!!.setLayout(dm.widthPixels, Objects.requireNonNull(dialog.window).attributes.height)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.change_password_layout, container)
        ed_userid = view.findViewById(R.id.change_password_userid)
        ed_old_password = view.findViewById(R.id.change_password_old_password)
        ed_new_password = view.findViewById(R.id.change_password_new_password)
        ed_confirm_password = view.findViewById(R.id.change_password_confirm_password)
        bt_sunmit = view.findViewById(R.id.change_password_submit)

        bt_sunmit!!.setOnClickListener(View.OnClickListener {
            userid = ed_userid!!.text.toString()
            oldpassword = ed_old_password!!.text.toString()
            newpassword = ed_new_password!!.text.toString()
            if (userid == "" || oldpassword == "" || newpassword == "") {
                Toast.makeText(activity, "用户ID或密码不能为空", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (newpassword != ed_confirm_password!!.text.toString()) {
                Toast.makeText(activity, "新密码与确认密码不一致", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            if (newpassword!!.length < 6 || newpassword!!.length > 18 || userid == "") {
                Toast.makeText(activity, "密码长度应该在6~18位", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            }
            AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formChangePassword(userid, MD5Util.getMd5(oldpassword!!), MD5Util.getMd5(newpassword!!)), AsyncTaskUtil.AsyncNetUtils.Callback { response ->
                Log.d("CPF", response)
                val jsonObj: JSONObject
                if (response != null) {
                    try {
                        jsonObj = JSONObject(response)
                        val code = jsonObj.optInt("code", -1)
                        if (code == 0) {
                            val intent = Intent(activity, LoginActivity::class.java)
                            val bundle = Bundle()
                            bundle.putString("userid", userid)
                            intent.putExtras(bundle)
                            startActivity(intent)
                            dismiss()
                        } else if (code == 107) {
                            try {
                                Toast.makeText(activity, "账号密码验证失败", Toast.LENGTH_SHORT).show()
                            } catch (ignored: Exception) {
                                Log.d("CPF", "java,lang.Exception")
                            }

                        } else {
                            return@Callback
                        }
                    } catch (ignored: JSONException) {
                    }

                }
            })
        })

        return view
    }
}
