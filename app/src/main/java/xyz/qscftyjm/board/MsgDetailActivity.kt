package xyz.qscftyjm.board

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import org.json.JSONArray
import org.json.JSONException

import java.util.ArrayList

import tools.BitmapUtil
import tools.BoardDBHelper

class MsgDetailActivity : AppCompatActivity(), View.OnClickListener {
    var comment: String? = null
    private var database: SQLiteDatabase? = null
    private var msgid: String? = null
    private var userid: String? = null
    private var nickname: String? = null
    private var time: String? = null
    private var content: String? = null
    private var haspic = 0
    private var tv_time: TextView? = null
    private var tv_nickname: TextView? = null
    private var tv_content: TextView? = null
    private var cursor: Cursor? = null
    private val pics = arrayOf<ImageView>(null, null, null)
    private var portrait: ImageView? = null
    private var big_pic: ImageView? = null
    private var pictures: ArrayList<Bitmap>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        database = BoardDBHelper.getMsgDBHelper(this).writableDatabase
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_msg_detail)
        val bundle = this.intent.extras
        pics[0] = findViewById(R.id.msg_detail_pic_0)
        pics[1] = findViewById(R.id.msg_detail_pic_1)
        pics[2] = findViewById(R.id.msg_detail_pic_2)
        big_pic = findViewById(R.id.msg_detail_big_pic)
        portrait = findViewById(R.id.msg_detail_head_portrait)
        tv_nickname = findViewById(R.id.msg_detail_nickname)
        tv_time = findViewById(R.id.msg_detail_time)
        tv_content = findViewById(R.id.msg_detail_content)

        for (i in 0..2) {
            pics[i].setOnClickListener(this)
        }

        pictures = ArrayList()
        if (bundle != null && bundle.containsKey("msgid")) {
            msgid = bundle.getInt("msgid").toString()
            cursor = database!!.query("msg", arrayOf("id", "userid", "time", "content", "haspic", "picture", "comment"), "id=?", arrayOf<String>(msgid), null, null, null, null)
            if (cursor!!.moveToFirst() && cursor!!.count > 0) {
                userid = cursor!!.getString(1)
                time = cursor!!.getString(2)
                content = cursor!!.getString(3)
                tv_time!!.text = time
                tv_content!!.text = content
                haspic = cursor!!.getInt(4)
                if (haspic > 0) {
                    try {
                        val jsonArray = JSONArray(String(cursor!!.getBlob(5)))
                        haspic = jsonArray.length()
                        Log.d("MDA", "pic num: $haspic")
                        for (i in 0 until haspic) {
                            pictures!!.add(BitmapUtil.getHexBitmap(this@MsgDetailActivity, jsonArray.optString(i)))
                            pics[i].visibility = View.VISIBLE
                            pics[i].setImageBitmap(pictures!![i])
                        }
                    } catch (ignored: JSONException) {
                    }

                }
            }
            cursor!!.close()
            cursor = database!!.query("publicinfo", arrayOf("userid", "nickname", "portrait"), "userid=?", arrayOf<String>(userid), null, null, null, null)
            if (cursor!!.moveToFirst() && cursor!!.count > 0) {
                nickname = cursor!!.getString(1)
                tv_nickname!!.text = nickname
                portrait!!.setImageBitmap(BitmapUtil.getHexBitmap(this@MsgDetailActivity, String(cursor!!.getBlob(2))))
            }
        } else {
            finish()
        }
        database!!.close()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.msg_detail_pic_0 -> {
                if (big_pic!!.visibility != View.VISIBLE) {
                    big_pic!!.visibility = View.VISIBLE
                }
                if (haspic < 1 && pics[0].drawable == null) {
                    break
                }
                big_pic!!.setImageDrawable(pics[0].drawable)
            }
            R.id.msg_detail_pic_1 -> {
                if (big_pic!!.visibility != View.VISIBLE) {
                    big_pic!!.visibility = View.VISIBLE
                }
                if (haspic < 2 && pics[1].drawable == null) {
                    break
                }
                big_pic!!.setImageDrawable(pics[1].drawable)
            }
            R.id.msg_detail_pic_2 -> {
                if (big_pic!!.visibility != View.VISIBLE) {
                    big_pic!!.visibility = View.VISIBLE
                }
                if (haspic < 3 && pics[2].drawable == null) {
                    break
                }
                big_pic!!.setImageDrawable(pics[2].drawable)
            }
            else -> Log.d("MDA", "NOT DEFINED CLICK")
        }
    }
}
