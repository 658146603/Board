package tools

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import java.util.HashMap

object UserUtil {
    fun getUserInfo(context: Context): Map<String, Any> {
        val userInfo = HashMap<String, Any>()
        val db = BoardDBHelper.getMsgDBHelper(context).writableDatabase
        val cursor = db.query("userinfo", arrayOf("userid", "nickname", "portrait", "email", "priority", "token"), null, null, null, null, "id desc", "0,1")
        val token: String
        val count: Int
        if (cursor.moveToFirst()) {
            count = cursor.count
            if (count > 0) {

                do {
                    userInfo["userid"] = cursor.getString(0)
                    userInfo["nickname"] = cursor.getString(1)

                    userInfo["portrait"] = BitmapUtil.getHexBitmap(context, String(cursor.getBlob(2)))

                    userInfo["email"] = cursor.getString(3)
                } while (cursor.moveToNext())

                cursor.close()

            }
        }

        return userInfo
    }
}
