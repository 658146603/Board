package xyz.qscftyjm.board

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ListView
import android.widget.Toast
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.HashMap
import java.util.Objects

import postutil.AsyncTaskUtil
import tools.BoardDBHelper
import tools.Msg
import tools.MsgDataOperator
import tools.MsgListAdapter
import tools.ParamToString
import tools.PublicUserInfo
import tools.StringCollector

class MainMsgFragment : Fragment(), View.OnClickListener, MsgReceiver.Message {
    private var lv_msg: ListView? = null
    private var adapter: MsgListAdapter? = null
    private var msgReceiver: MsgReceiver? = null
    private var msgData: ArrayList<Msg>? = null
    private var userInfoMap: MutableMap<String, PublicUserInfo>? = null
    private var database: SQLiteDatabase? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_main_msg, container, false)

        msgData = ArrayList()
        userInfoMap = HashMap()

        msgReceiver = MsgReceiver()
        database = BoardDBHelper.getMsgDBHelper(activity).writableDatabase
        val intentFilter = IntentFilter()
        intentFilter.addAction("xyz.qscftyjm.board.HAS_NEW_MSG")
        try {
            Objects.requireNonNull<Context>(context).applicationContext.registerReceiver(msgReceiver, intentFilter)
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

        msgReceiver!!.setMessage(this)

        lv_msg = view.findViewById(R.id.msg_list)
        MsgDataOperator.getMsgData(activity, msgData, userInfoMap)
        adapter = MsgListAdapter(msgData, userInfoMap, activity)
        lv_msg!!.adapter = adapter

        lv_msg!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            Logd("ItemClick: $position")
            val intent0 = Intent(activity, MsgDetailActivity::class.java)
            val bundle0 = Bundle()
            bundle0.putInt("msgid", msgData!![position].id)
            bundle0.putString("content", msgData!![position].content)
            bundle0.putString("nickname", msgData!![position].nickname)
            bundle0.putString("time", msgData!![position].time)
            bundle0.putInt("haspic", msgData!![position].hasPic)
            intent0.putExtras(bundle0)
            startActivity(intent0)
        }

        lv_msg!!.setOnCreateContextMenuListener { menu, v, menuInfo ->
            menu.setHeaderTitle("更多操作")
            menu.add(0, 0, 0, "查看详细内容")
            menu.add(0, 1, 0, "复制内容到剪切板")
            menu.add(0, 2, 0, "查看TA的所有留言(敬请期待)")
            menu.add(0, 3, 0, "评论这条留言")
            menu.add(0, 4, 0, "删除这条留言")
        }

        return view
    }

    override fun onContextItemSelected(menuItem: MenuItem?): Boolean {

        val info = menuItem!!.menuInfo as AdapterView.AdapterContextMenuInfo
        val index = info.id.toString()
        //Toast.makeText(getActivity(), "长按点击了第"+index+"条的第"+menuItem.getItemId()+"项", Toast.LENGTH_SHORT).show();
        when (menuItem.itemId) {
            0 -> {
                val intent0 = Intent(activity, MsgDetailActivity::class.java)
                val bundle0 = Bundle()
                bundle0.putInt("msgid", msgData!![info.position].id)
                bundle0.putString("content", msgData!![info.position].content)
                bundle0.putString("nickname", msgData!![info.position].nickname)
                bundle0.putString("time", msgData!![info.position].time)
                bundle0.putInt("haspic", msgData!![info.position].hasPic)
                intent0.putExtras(bundle0)
                startActivity(intent0)
            }

            1 -> {
                val manager = Objects.requireNonNull<FragmentActivity>(activity).getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val data = ClipData.newPlainText("content:", msgData!![info.position].content)
                manager.primaryClip = data
                Toast.makeText(activity, "留言内容已经复制到剪切板", Toast.LENGTH_SHORT).show()
            }

            2 ->
                // TODO 查看TA的所有留言
                makeToast("功能暂未开放")

            3 -> {
                // TODO 评论留言
                val fragment = CommentFragment()
                val bundle = Bundle()
                bundle.putInt("msgid", msgData!![info.position].id)
                fragment.arguments = bundle
                fragment.show(Objects.requireNonNull<FragmentActivity>(activity).getSupportFragmentManager(), "添加留言")
            }

            4 -> {
                // TODO 删除留言
                val cursor = database!!.query("userinfo", arrayOf("userid", "token"), null, null, null, null, "id desc", "0,1")

                val userid: String
                val token: String
                if (cursor.moveToFirst() && cursor.count > 0) {
                    userid = cursor.getString(0)
                    token = cursor.getString(1)
                    AsyncTaskUtil.AsyncNetUtils.post(StringCollector.msgServer, ParamToString.formDelMsg(userid, token, msgData!![info.position].id)) { response ->
                        val jsonObj: JSONObject
                        if (response != null) {
                            try {
                                jsonObj = JSONObject(response)
                                val code = jsonObj.optInt("code", -1)
                                if (code == 0) {
                                    makeToast("删除成功，下一次刷新后会消失")
                                } else if (code == -104) {
                                    Logd("delete failed, userid and token not match")
                                    makeToast("删除失败，请重新登录")
                                } else if (code == -105) {
                                    Logd("delete msg failed, data error")
                                    makeToast("删除失败")
                                } else if (code == -106) {
                                    Logd("delete msg failed, user not found")
                                    makeToast("删除失败，用户不存在")
                                } else if (code == -107) {
                                    Logd("delete msg failed, permission not allowed")
                                    makeToast("删除失败，权限不足")
                                } else {
                                    makeToast(jsonObj.optString("msg", "未知错误"))
                                    Logd(jsonObj.optString("msg", "未知错误"))
                                }
                            } catch (e: JSONException) {
                                makeToast("删除失败，网络错误")
                            }

                        } else {
                            makeToast("删除失败，网络错误")
                        }
                    }
                } else {
                    Toast.makeText(activity, "请登录账号！", Toast.LENGTH_LONG).show()
                    startActivity(Intent(activity, LoginActivity::class.java))
                }
                cursor.close()
            }

            else -> {
            }
        }//Logd("4");
        return super.onContextItemSelected(menuItem)

    }

    private fun makeToast(msg: String) {
        try {
            Toast.makeText(activity, msg, Toast.LENGTH_LONG).show()
        } catch (ignore: Exception) {
        }

    }


    override fun onClick(v: View) {

    }

    private fun Logd(msg: String) {
        Log.d("MMF", msg)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            Objects.requireNonNull<Context>(context).applicationContext.unregisterReceiver(msgReceiver)
            Log.d("MMF", "Broadcast closed successfully")
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            Log.d("MMF", "Broadcast closed failed")
        } catch (e: NullPointerException) {
            e.printStackTrace()
            Log.d("MMF", "Broadcast closed failed")
        }

    }

    override fun getMsg(str: String) {
        MsgDataOperator.getMsgData(activity, msgData, userInfoMap)
        Logd("get broadcast: $str")
        adapter!!.notifyDataSetChanged()
        lv_msg!!.invalidate()
    }

    companion object {

        internal val TAG = "Board"
    }
}
