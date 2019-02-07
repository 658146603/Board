package xyz.qscftyjm.board

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Handler
import android.os.IBinder
import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import postutil.AsyncTaskUtil
import tools.BoardDBHelper
import tools.ParamToString
import tools.StringCollector

class MsgSyncService : Service() {
    private val handler = Handler()
    private var runnable: Runnable? = null
    private var database: SQLiteDatabase? = null
    private var thread: Thread? = null
    private var flag = 0

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mChannel: NotificationChannel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = NotificationChannel("Msg001", "GetMsg", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(mChannel)
            val notification = Notification.Builder(applicationContext, "Msg001").build()
            startForeground(1, notification)
        }

        database = BoardDBHelper.getMsgDBHelper(this).writableDatabase
        runnable = object : Runnable {

            override fun run() {
                Log.v("MSS", "flag: $flag")
                if (flag == 0) {
                    return
                }
                var lastId = 0
                handler.postDelayed(this, 1000)
                var cursor = database!!.query("msg", arrayOf("id"), null, null, null, null, "id desc", "0,1")
                if (cursor.moveToFirst()) {
                    lastId = cursor.getInt(0)
                    Log.v("Service", "lsatId: $lastId")
                }
                cursor.close()

                val userid: String
                val token: String

                cursor = database!!.query("userinfo", arrayOf("userid", "token"), null, null, null, null, "id desc", "0,1")
                if (cursor.moveToFirst()) {
                    userid = cursor.getString(0)
                    token = cursor.getString(1)
                } else {
                    return
                }
                cursor.close()
                AsyncTaskUtil.AsyncNetUtils.post(StringCollector.msgServer, ParamToString.formGetMsg(userid, token, lastId.toString()), AsyncTaskUtil.AsyncNetUtils.Callback { response ->
                    if (response != null) {
                        try {
                            val jsonObj = JSONObject(response)
                            if (jsonObj.optInt("code", -1) == 0) {
                                val delArr = jsonObj.optJSONArray("delete")
                                if (delArr != null && delArr.length() > 0) {
                                    for (i in 0 until delArr.length()) {
                                        val num = database!!.delete("msg", "id=?", arrayOf(delArr.optInt(i, -1).toString()))
                                    }
                                }
                                val msgArr = jsonObj.optJSONArray("msgs")
                                if (jsonObj.optInt("msgct", 0) == 0) {
                                    Log.v("Service Msg", "No new msg")
                                    return@Callback
                                }
                                val msgCt = msgArr.length()

                                if (msgCt > 0) {
                                    for (i in 0 until msgCt) {
                                        val newObj = msgArr.getJSONObject(i)
                                        val values = ContentValues()
                                        if (newObj.optInt("id", -1) == -1) {
                                            continue
                                        }
                                        values.put("id", newObj.optInt("id", -1))
                                        values.put("userid", newObj.optString("userid", "error"))
                                        values.put("time", newObj.optString("time", "error"))
                                        values.put("content", newObj.optString("content", "error"))
                                        if (newObj.optInt("hasPics", 0) > 0 && newObj.optJSONArray("pics") != null) {
                                            values.put("haspic", newObj.optInt("hasPics", 0))
                                            val jsonArray = newObj.optJSONArray("pics")
                                            values.put("picture", jsonArray.toString())
                                        } else {
                                            values.put("haspic", 0)
                                        }
                                        values.put("comment", newObj.optString("comment", "{'comment':null}"))
                                        database!!.insertWithOnConflict("msg", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                                    }
                                    val intent = Intent()
                                    intent.action = "xyz.qscftyjm.board.HAS_NEW_MSG"
                                    intent.putExtra("msg", msgArr.length().toString())
                                    sendBroadcast(intent)
                                } else {
                                    Log.d("Service Msg", "No new msg")
                                }

                            } else {
                                Log.d("Service", jsonObj.optString("msg", "未知错误"))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }

                    }
                })
            }
        }


    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        flag = 1
        if (thread == null || !thread!!.isAlive) {
            thread = Thread(runnable)
            thread!!.run()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        flag = 0
        Log.d("MSS", "Service Stop")
    }

}
