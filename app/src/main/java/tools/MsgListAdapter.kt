package tools

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

import java.util.ArrayList
import java.util.Objects

import xyz.qscftyjm.board.R

class MsgListAdapter(private val msgList: ArrayList<Msg>, private val userInfoMap: MutableMap<String, PublicUserInfo>, private val context: Context) : BaseAdapter() {
    private var viewHolder: ViewHolder? = null
    private val database: SQLiteDatabase

    init {
        var userInfo: PublicUserInfo
        database = BoardDBHelper.getMsgDBHelper(context).writableDatabase
        val cursor = database.query("publicinfo", arrayOf("userid", "nickname", "portrait"), null, null, null, null, null)

        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                userInfo = PublicUserInfo()
                userInfo.userid = cursor.getString(0)
                userInfo.portrait = BitmapUtil.getHexBitmap(context, String(cursor.getBlob(2)))
                userInfo.nickname = cursor.getString(1)
            } while (cursor.moveToNext())
            userInfoMap[userInfo.userid!!] = userInfo
            cursor.close()
        }
    }

    override fun getCount(): Int {
        return msgList.size
    }

    override fun getItem(position: Int): Any {
        return msgList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView

        if (convertView == null) {

            convertView = LayoutInflater.from(context).inflate(R.layout.msg_card, null)
            viewHolder = ViewHolder(convertView!!)
            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        if (userInfoMap.containsKey(msgList[position].userid)) {
            viewHolder!!.portrait.setImageBitmap(Objects.requireNonNull<PublicUserInfo>(userInfoMap[msgList[position].userid]).portrait)
            viewHolder!!.nickname.text = Objects.requireNonNull<PublicUserInfo>(userInfoMap[msgList[position].userid]).nickname
        } else {
            MsgDataOperator.getUserInfo(context, msgList[position].userid, userInfoMap)
        }

        if (userInfoMap.containsKey(msgList[position].userid)) {
            viewHolder!!.portrait.setImageBitmap(Objects.requireNonNull<PublicUserInfo>(userInfoMap[msgList[position].userid]).portrait)
            viewHolder!!.nickname.text = Objects.requireNonNull<PublicUserInfo>(userInfoMap[msgList[position].userid]).nickname
        }

        viewHolder!!.time.text = msgList[position].time

        viewHolder!!.content.text = msgList[position].content
        if (msgList[position].hasPic > 0) {
            viewHolder!!.picture.visibility = View.VISIBLE
            viewHolder!!.picture.setImageBitmap(msgList[position].picture!![0])
        } else {
            viewHolder!!.picture.visibility = View.GONE
        }


        return convertView
    }

    internal inner class ViewHolder(view: View) {
        var portrait: ImageView
        var picture: ImageView
        var time: TextView
        var nickname: TextView
        var content: TextView

        init {
            portrait = view.findViewById(R.id.msg_head_portrait)
            picture = view.findViewById(R.id.msg_picture)
            time = view.findViewById(R.id.msg_time)
            nickname = view.findViewById(R.id.msg_nickname)
            content = view.findViewById(R.id.msg_content)
        }
    }

}
