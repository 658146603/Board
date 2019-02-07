package xyz.qscftyjm.board

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import tools.BitmapUtil
import tools.BoardDBHelper

class MoreInfoActivity : AppCompatActivity() {

    private var tv_userid: TextView? = null
    private var tv_nickname: TextView? = null
    private var tv_email: TextView? = null
    private var img_portrait: ImageView? = null
    private var bt_change_info: Button? = null
    private var bt_change_password: Button? = null
    private var userid: String? = null
    private var nickname: String? = null
    private var email: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_more_info)
        tv_userid = findViewById(R.id.user_info_userid)
        tv_nickname = findViewById(R.id.user_info_nickname)
        tv_email = findViewById(R.id.user_info_email)
        img_portrait = findViewById(R.id.user_info_portrait)
        bt_change_info = findViewById(R.id.user_info_change_info)
        bt_change_password = findViewById(R.id.user_info_change_password)

        val count = setUserInfo(tv_userid, tv_nickname, tv_email, img_portrait)


        bt_change_password!!.setOnClickListener {
            Log.d(TAG, "Change Info")
            // TODO 弹出窗口更改信息
            if (count > 0) {
                val fragment = ChangePasswordFragment()
                fragment.show(supportFragmentManager, "changepassword")
            } else {
                Toast.makeText(this@MoreInfoActivity, "请登录账号", Toast.LENGTH_LONG).show()
            }
        }

        bt_change_info!!.setOnClickListener {
            Log.d(TAG, "Change Info")
            // TODO 弹出窗口更改信息
            if (count > 0) {
                val fragment = ChangeUserInfoFragment()
                fragment.show(fragmentManager, "changeinfo")
            } else {
                Toast.makeText(this@MoreInfoActivity, "请登录账号", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setUserInfo(tv_userid: TextView?, tv_nickname: TextView?, tv_email: TextView?, img_portrait: ImageView?): Int {
        val db = BoardDBHelper.getMsgDBHelper(this@MoreInfoActivity).writableDatabase
        val cursor = db.query("userinfo", arrayOf("userid", "nickname", "portrait", "email", "priority", "token"), null, null, null, null, "id desc", "0,1")
        var token: String
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.count
            if (count > 0) {
                do {
                    this.userid = cursor.getString(0)
                    tv_userid!!.text = this.userid
                    this.nickname = cursor.getString(1)
                    tv_nickname!!.text = nickname

                    img_portrait!!.setImageBitmap(BitmapUtil.getHexBitmap(this@MoreInfoActivity, String(cursor.getBlob(2))))

                    this.email = cursor.getString(3)
                    tv_email!!.text = this.email
                    token = cursor.getString(5)
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        return count
    }

    public override fun onResume() {
        super.onResume()
        setUserInfo(tv_userid, tv_nickname, tv_email, img_portrait)
    }

    companion object {

        private val TAG = "Board"
    }

}
