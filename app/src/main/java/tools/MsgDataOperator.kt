package tools


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.util.Log

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList
import java.util.Objects

import postutil.AsyncTaskUtil

object MsgDataOperator {

    fun getMsgData(context: Context?, msgData: ArrayList<Msg>?, userInfoMap: MutableMap<String, PublicUserInfo>) {
        var msgData = msgData
        if (context == null) {
            return
        }
        val database = BoardDBHelper.getMsgDBHelper(context).writableDatabase
        var cursor: Cursor
        var userInfo: PublicUserInfo
        var lastId = 0
        if (msgData == null) {
            msgData = ArrayList()
        } else {
            if (msgData.size == 0) {
            } else {
                lastId = msgData[0].id
            }
        }

        cursor = database.query("publicinfo", arrayOf("userid", "nickname", "portrait"), null, null, null, null, null)

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

        cursor = database.query("msg", arrayOf("id", "userid", "time", "content", "haspic", "picture", "comment"), "id>?", arrayOf(lastId.toString()), null, null, "id", null)
        if (cursor.moveToFirst()) {
            do {
                val msg: Msg
                val id = cursor.getInt(0)
                val userid = cursor.getString(1)

                val time = cursor.getString(2)
                val content = cursor.getString(3)
                var haspic = cursor.getInt(4)
                val comment = cursor.getString(6)

                var b_pics: Array<Bitmap>? = null
                if (haspic > 0) {

                    try {
                        val t_pics = String(cursor.getBlob(5))
                        val arrPic = JSONArray(t_pics)
                        b_pics = arrayOfNulls(arrPic.length())
                        for (i in 0 until arrPic.length()) {
                            b_pics[i] = BitmapUtil.getHexBitmap(context, arrPic.getString(i))
                        }
                        haspic = b_pics.size
                    } catch (e: JSONException) {
                        b_pics = null
                        haspic = 0
                        e.printStackTrace()
                    } catch (e: NullPointerException) {
                        b_pics = null
                        haspic = 0
                        e.printStackTrace()
                    }

                }

                if (b_pics != null && b_pics.size > 0) {
                    msg = Msg(id, userid, null, time, content, null, haspic, b_pics)
                } else {
                    msg = Msg(id, userid, null, time, content, null)
                }
                msgData.add(0, msg)
            } while (cursor.moveToNext())
        }
        cursor.close()
        if (database.isOpen) {
            database.close()
        }
    }

    fun getUserInfo(context: Context, userids: ArrayList<String>, userInfoMap: MutableMap<String, PublicUserInfo>): Map<String, PublicUserInfo> {
        var userInfo: PublicUserInfo
        val database = BoardDBHelper.getMsgDBHelper(context).writableDatabase
        val cursor = database.query("publicinfo", arrayOf("userid", "nickname", "portrait"), null, null, null, null, null)
        if (cursor.moveToFirst()) {
            if (cursor.count > 0) {
                do {
                    userInfo = PublicUserInfo()
                    userInfo.userid = cursor.getString(0)
                    userInfo.nickname = cursor.getString(2)
                    userInfo.portrait = BitmapUtil.getHexBitmap(context, String(cursor.getBlob(2)))
                    if (!userInfoMap.containsKey(userInfo.userid)) {
                        userInfoMap[userInfo.userid!!] = userInfo
                    }

                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        for (i in userids.indices) {
            val needed = ArrayList<String>()

            if (!userInfoMap.containsKey(userids[i])) {
                needed.add(userids[i])
            }

            if (userids.size > 0) {

                val jsonArrNeeded = JSONArray(needed)
                AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formGetPublicInfo(jsonArrNeeded.toString())) { response ->
                    val jsonObj: JSONObject
                    if (response != null) {
                        Log.d("MDO", response)
                        try {
                            jsonObj = JSONObject(response)
                            if (jsonObj.optInt("code", -1) == 0) {
                                val users = jsonObj.optJSONArray("users")
                                if (users != null && users.length() > 0) {
                                    for (i in 0 until users.length()) {
                                        val userInfo = PublicUserInfo()
                                        if (users.optJSONObject(i) != null) {
                                            userInfo.userid = users.optJSONObject(i).optString("userid")
                                            userInfo.nickname = users.optJSONObject(i).optString("nickname")
                                            userInfo.portrait = BitmapUtil.getHexBitmap(context, users.optJSONObject(i).optString("portrait", "00000000"))
                                        } else {
                                            userInfo.userid = "UNDEFINED"
                                            userInfo.nickname = "UNDEFINED"
                                            userInfo.portrait = BitmapUtil.getHexBitmap(context, "00000000")
                                        }

                                        val values = ContentValues()
                                        values.put("userid", userInfo.userid)
                                        values.put("nickname", userInfo.nickname)
                                        values.put("portrait", BitmapIOUtil.bytesToHexString(BitmapUtil.Bitmap2Bytes(userInfo.portrait!!)))

                                        userInfoMap[userInfo.userid!!] = userInfo
                                        database.insertWithOnConflict("publicinfo", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                                    }
                                }
                            } else {
                                Log.d("Service", jsonObj.optString("msg", "未知错误"))
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }

                    }
                }
            }

        }

        return userInfoMap
    }

    fun getUserInfo(i: Int, context: Context, msgs: ArrayList<Msg>, userInfoMap: MutableMap<String, PublicUserInfo>): Map<String, PublicUserInfo> {
        val userids = ArrayList<String>()
        var userInfo: PublicUserInfo
        val database = BoardDBHelper.getMsgDBHelper(context).writableDatabase
        for (msg in msgs) {
            if (userInfoMap.containsKey(msg.userid) && Objects.requireNonNull<PublicUserInfo>(userInfoMap[msg.userid]).nickname != null && Objects.requireNonNull<PublicUserInfo>(userInfoMap[msg.userid]).portrait != null) {
                Log.d("MDO", "Have " + msg.userid!!)
            } else {
                val cursor = database.query("publicinfo", arrayOf("userid", "nickname", "portrait"), null, null, null, null, null)
                if (cursor.moveToFirst()) {
                    if (cursor.count > 0) {
                        do {
                            userInfo = PublicUserInfo()
                            userInfo.userid = cursor.getString(0)
                            userInfo.nickname = cursor.getString(2)
                            userInfo.portrait = BitmapUtil.getHexBitmap(context, String(cursor.getBlob(2)))
                            userInfoMap[userInfo.userid!!] = userInfo
                        } while (cursor.moveToNext())
                    } else {
                        if (!userids.contains(msg.userid)) {
                            userids.add(msg.userid)
                            Log.d("MDO", "Need " + msg.userid!!)
                        }
                    }
                } else {
                    if (!userids.contains(msg.userid)) {
                        userids.add(msg.userid)
                        Log.d("MDO", "Need " + msg.userid!!)
                    }
                }
                cursor.close()
            }
        }
        if (userids.size > 0) {
            val jsonArrNeeded = JSONArray(userids)
            AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formGetPublicInfo(jsonArrNeeded.toString())) { response ->
                val jsonObj: JSONObject
                if (response != null) {
                    Log.d("MDO", response)
                    try {
                        jsonObj = JSONObject(response)
                        if (jsonObj.optInt("code", -1) == 0) {
                            val users = jsonObj.optJSONArray("users")
                            if (users != null && users.length() > 0) {
                                for (i in 0 until users.length()) {
                                    val userInfo = PublicUserInfo()
                                    if (users.optJSONObject(i) != null) {
                                        userInfo.userid = users.optJSONObject(i).optString("userid")
                                        userInfo.nickname = users.optJSONObject(i).optString("nickname")
                                        userInfo.portrait = BitmapUtil.getHexBitmap(context, users.optJSONObject(i).optString("portrait", "00000000"))
                                    } else {
                                        userInfo.userid = "UNDEFINED"
                                        userInfo.nickname = "UNDEFINED"
                                        userInfo.portrait = BitmapUtil.getHexBitmap(context, "00000000")
                                    }

                                    val values = ContentValues()
                                    values.put("userid", userInfo.userid)
                                    values.put("nickname", userInfo.nickname)
                                    values.put("portrait", BitmapIOUtil.bytesToHexString(BitmapUtil.Bitmap2Bytes(userInfo.portrait!!)))

                                    userInfoMap[userInfo.userid!!] = userInfo
                                    database.insertWithOnConflict("publicinfo", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                                }
                            }
                        } else {
                            Log.d("MDO", jsonObj.optString("msg", "未知错误"))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
            }
        }
        return userInfoMap
    }

    internal fun getUserInfo(context: Context, userid: String, userInfoMap: MutableMap<String, PublicUserInfo>) {
        val userInfo = PublicUserInfo()
        val database = BoardDBHelper.getMsgDBHelper(context).writableDatabase
        val cursor = database.query("publicinfo", arrayOf("userid", "nickname", "portrait"), "userid=?", arrayOf(userid), null, null, null)

        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                userInfo.userid = userid
                userInfo.portrait = BitmapUtil.getHexBitmap(context, String(cursor.getBlob(2)))
                userInfo.nickname = cursor.getString(1)
            } while (cursor.moveToNext())
            userInfoMap[userid] = userInfo
            cursor.close()
        } else {

            AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formGetPublicInfo("['$userid']")) { response ->
                val jsonObj: JSONObject
                if (response != null) {
                    Log.d("MDO", response)
                    try {
                        jsonObj = JSONObject(response)
                        if (jsonObj.optInt("code", -1) == 0) {
                            val users = jsonObj.optJSONArray("users")
                            if (users != null && users.length() > 0) {
                                val userInfo = PublicUserInfo()
                                if (users.optJSONObject(0) != null) {
                                    userInfo.userid = users.optJSONObject(0).optString("userid")
                                    userInfo.nickname = users.optJSONObject(0).optString("nickname")
                                    userInfo.portrait = BitmapUtil.getHexBitmap(context, users.optJSONObject(0).optString("portrait", "00000000"))
                                } else {
                                    userInfo.userid = "UNDEFINED"
                                    userInfo.nickname = "UNDEFINED"
                                    userInfo.portrait = BitmapUtil.getHexBitmap(context, "00000000")
                                }

                                val values = ContentValues()
                                values.put("userid", userInfo.userid)
                                values.put("nickname", userInfo.nickname)
                                values.put("portrait", BitmapIOUtil.bytesToHexString(BitmapUtil.Bitmap2Bytes(userInfo.portrait!!)))

                                userInfoMap[userInfo.userid!!] = userInfo
                                database.insertWithOnConflict("publicinfo", null, values, SQLiteDatabase.CONFLICT_REPLACE)
                            }
                        } else {
                            Log.d("MDO", jsonObj.optString("msg", "未知错误"))
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                if (database.isOpen) {
                    database.close()
                }
            }
        }

    }

}
