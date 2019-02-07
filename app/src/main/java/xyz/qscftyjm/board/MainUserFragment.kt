package xyz.qscftyjm.board


import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import tools.BitmapUtil
import tools.BoardDBHelper


class MainUserFragment : Fragment(), View.OnClickListener {

    private var priority: Int = 0
    private val id: Int = 0
    private var userid = ""
    private var nickname = ""
    private var email = ""
    private val bitmap_portrait: Bitmap? = null
    private var database: SQLiteDatabase? = null
    private var view: View? = null
    private var bt_login_info: Button? = null
    private var bt_user_info: Button? = null
    private var tv_nickname: TextView? = null
    private var tv_userid: TextView? = null
    private var img_head_portrait: ImageView? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_main_user, container, false)

        database = BoardDBHelper.getMsgDBHelper(activity).writableDatabase

        bt_login_info = view!!.findViewById(R.id.user_bt_login_info)
        bt_user_info = view!!.findViewById(R.id.user_bt_more)
        tv_nickname = view!!.findViewById(R.id.user_nickname)
        tv_userid = view!!.findViewById(R.id.user_userid)
        img_head_portrait = view!!.findViewById(R.id.user_img_portrait)
        bt_login_info!!.setOnClickListener(this)
        bt_user_info!!.setOnClickListener(this)

        setUserInfo(tv_userid, tv_nickname, img_head_portrait, database!!)

        Log.d(TAG, "userid $userid nickname $nickname email $email priority $priority")
        Log.d(TAG, tv_userid!!.text.toString())

        return view
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.user_bt_login_info -> startActivity(Intent(this.activity, LoginActivity::class.java))
            R.id.user_bt_more -> {
                Log.d(TAG, "MORE")
                val intent = Intent(activity, MoreInfoActivity::class.java)
                startActivity(intent)
            }
            else -> Log.d(TAG, "Button Not Defined")
        }

    }

    override fun onResume() {
        super.onResume()
        setUserInfo(tv_userid, tv_nickname, img_head_portrait, database!!)
    }

    private fun setUserInfo(tv_userid: TextView?, tv_nickname: TextView?, img_portrait: ImageView?, db: SQLiteDatabase) {
        val cursor = db.query("userinfo", arrayOf("userid", "nickname", "portrait", "email", "priority", "token"), null, null, null, null, "id desc", "0,1")
        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                this.userid = cursor.getString(0)
                tv_userid!!.text = "ID : " + this.userid
                this.nickname = cursor.getString(1)
                tv_nickname!!.text = "Hi, " + this.nickname

                img_portrait!!.setImageBitmap(BitmapUtil.getHexBitmap(activity, String(cursor.getBlob(2))))

                this.email = cursor.getString(3)
                this.priority = cursor.getInt(4)

            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    companion object {

        private val TAG = "Board"
    }

}
