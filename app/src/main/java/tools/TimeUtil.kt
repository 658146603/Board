package tools

import android.util.Log

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date

object TimeUtil {

    val time: String
        get() {
            val time: String
            val date = Date()
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            time = sdf.format(date)
            println(time)

            return time
        }

    fun checkIsOverTime(lastchecketime: String): Boolean {
        var flag = true

        try {
            val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val lastdate = sdf.parse(lastchecketime)
            val nowdate = Date()
            val delta = nowdate.time - lastdate.time
            Log.d("Calendar", "delta time : $delta")
            if (delta >= 0 && delta <= 1000 * 3600 * 24 * 7) {
                //一个星期不进行登录，判断用户登录过期
                flag = false
            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return flag
    }


}