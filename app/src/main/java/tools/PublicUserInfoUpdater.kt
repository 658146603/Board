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

import java.io.ByteArrayOutputStream
import java.util.ArrayList
import java.util.HashMap

import postutil.NetUtils

object PublicUserInfoUpdater {
    private var database: SQLiteDatabase? = null

    fun CheckPublicUserInfoUpdate(context: Context) {
        object : Thread() {
            override fun run() {
                super.run()
                database = BoardDBHelper.getMsgDBHelper(context).writableDatabase
                var userid: String
                var nickname: String
                var portrait: String
                val userInfoMd5Array = ArrayList<Map<String, String>>()
                var userInfoMd5: MutableMap<String, String>
                val cursor = database!!.query("publicinfo", arrayOf("userid", "nickname", "portrait"), null, null, null, null, null)
                if (cursor.moveToFirst() && cursor.count > 0) {
                    do {
                        userInfoMd5 = HashMap()
                        userid = cursor.getString(0)
                        nickname = cursor.getString(1)
                        portrait = String(cursor.getBlob(2))
                        val bitmap = BitmapUtil.getHexBitmap(context, portrait)
                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        userInfoMd5["userid"] = userid
                        userInfoMd5["md5"] = getUserMd5(userid, nickname, portrait)!!//BitmapIOUtil.bytesToHexString(baos.toByteArray())
                        userInfoMd5Array.add(userInfoMd5)
                    } while (cursor.moveToNext())
                    Log.d("PUIU", "size: " + userInfoMd5Array.size)
                    val response = NetUtils.post(StringCollector.userServer, ParamToString.formUpdatePublicUserInfo(userInfoMd5Array))
                    if (response != null) {
                        try {
                            val jsonObject = JSONObject(response)
                            if (jsonObject.optInt("code", -1) == 0) {
                                var _userid: String
                                var _nickname: String
                                var _portrait: String
                                val jsonArray = jsonObject.optJSONArray("data")
                                Log.d("PUIU", "Has Update: " + jsonArray.length())
                                if (jsonArray.length() > 0) {
                                    for (i in 0 until jsonArray.length()) {
                                        _userid = jsonArray.optJSONObject(i).optString("userid")
                                        _nickname = jsonArray.optJSONObject(i).optString("nickname")
                                        _portrait = jsonArray.optJSONObject(i).optString("portrait")
                                        if (_userid != "" && _nickname != "" && _portrait != "") {
                                            val values = ContentValues()
                                            values.put("nickname", _nickname)
                                            values.put("portrait", _portrait)
                                            database!!.update("publicinfo", values, "userid=?", arrayOf(_userid))
                                        }
                                    }
                                }
                            }
                        } catch (ignored: JSONException) {
                        }

                    }
                }
                cursor.close()
            }
        }.start()


    }

    private fun getUserMd5(userid: String?, nickname: String?, portrait: String?): String? {
        return if (userid == null || nickname == null || portrait == null) {
            null
        } else {
            MD5Util.getMd5(userid + nickname + portrait)
        }
    }

}

