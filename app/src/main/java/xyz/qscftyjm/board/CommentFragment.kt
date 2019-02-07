package xyz.qscftyjm.board

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

import org.json.JSONException
import org.json.JSONObject

import java.util.Objects

import postutil.AsyncTaskUtil
import tools.BoardDBHelper
import tools.ParamToString
import tools.StringCollector


class CommentFragment : DialogFragment() {

    private var mContext: Context? = null
    private var et_comment_content: EditText? = null
    private var bt_submit: Button? = null
    private var database: SQLiteDatabase? = null
    internal var msgid = -1

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        Objects.requireNonNull<FragmentActivity>(activity).getWindowManager().getDefaultDisplay().getMetrics(dm)
        dialog.window!!.setLayout(dm.widthPixels, Objects.requireNonNull<Window>(dialog.window).getAttributes().height)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_comment, container, false)
        et_comment_content = view.findViewById(R.id.comment_msg_content)
        bt_submit = view.findViewById(R.id.comment_msg_submit)
        database = BoardDBHelper.getMsgDBHelper(mContext).writableDatabase
        val bundle = this.arguments
        if (bundle != null && bundle.containsKey("msgid")) {
            msgid = bundle.getInt("msgid")
        } else {
            dismiss()
        }

        bt_submit!!.setOnClickListener(View.OnClickListener {
            if (msgid < 0) {
                return@OnClickListener
            }
            val comment = et_comment_content!!.text.toString()
            if (comment == "" || comment.length < 1) {
                Toast.makeText(mContext, "评论内容不能为空", Toast.LENGTH_SHORT).show()
                return@OnClickListener
            } else {
                val cursor = database!!.query("userinfo", arrayOf("userid", "token"), null, null, null, null, "id desc", "0,1")
                if (cursor.moveToFirst() && cursor.count > 0) {
                    val userid = cursor.getString(0)
                    val token = cursor.getString(1)
                    AsyncTaskUtil.AsyncNetUtils.post(StringCollector.msgServer, ParamToString.formAddComment(msgid.toString(), userid, token, comment)) { response ->
                        //Toast.makeText(mContext,"comment : "+response,Toast.LENGTH_SHORT).show();
                        val jsonObj: JSONObject
                        val code: Int
                        if (response != null) {
                            try {
                                jsonObj = JSONObject(response)
                                code = jsonObj.optInt("code", -1)
                                Logd(jsonObj.optString("msg", "unknown error"))
                                if (code == 0) {
                                    makeToast("评论成功")
                                    dismiss()
                                } else if (code != -1) {
                                    if (code == -101) {
                                        val intent = Intent(mContext, LoginActivity::class.java)
                                        val bundle = Bundle()
                                        bundle.putString("userid", userid)
                                        intent.putExtras(bundle)
                                        startActivity(intent)
                                        dismiss()
                                    } else if (code == -99) {
                                        makeToast("评论发生错误")
                                    } else if (code == -102 || code == -103) {
                                        makeToast("该留言不存在，可能已被删除")
                                    }
                                } else {
                                    Logd("sever error")
                                }
                            } catch (e: JSONException) {
                                Logd("服务器返回数据错误")
                                makeToast("服务器返回数据错误")
                            }

                        }
                    }
                } else {
                    //TODO 登录
                }
            }
        })
        return view
    }

    private fun makeToast(info: String?) {
        if (mContext == null || info == null || info.length == 0) {
            return
        }
        Toast.makeText(mContext, info, Toast.LENGTH_SHORT).show()
    }

    private fun Logd(log: String?) {
        if (log == null) {
            return
        }
        Log.d("CF", log)
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

}
