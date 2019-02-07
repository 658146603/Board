package postutil

import android.accounts.NetworkErrorException

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL

object NetUtils {
    fun post(url: String, content: String): String? {
        var conn: HttpURLConnection? = null
        try {
            // 创建一个URL对象
            val mURL = URL(url)
            // 调用URL的openConnection()方法,获取HttpURLConnection对象
            conn = mURL.openConnection() as HttpURLConnection

            conn.requestMethod = "POST"// 设置请求方法为post
            conn.readTimeout = 10000// 设置读取超时为10秒
            conn.connectTimeout = 10000// 设置连接网络超时为10秒
            conn.doOutput = true// 设置此方法,允许向服务器输出内容

            // post请求的参数
            val data = content
            // 获得一个输出流,向服务器写数据,默认情况下,系统不允许向服务器输出内容
            val out = conn.outputStream// 获得一个输出流,向服务器写数据
            out.write(data.toByteArray())
            out.flush()
            out.close()

            val responseCode = conn.responseCode// 调用此方法就不必再使用conn.connect()方法
            if (responseCode == 200) {

                val `is` = conn.inputStream
                return getStringFromInputStream(`is`)
            } else {
                throw NetworkErrorException("response status is $responseCode")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            conn?.disconnect()
        }

        return null
    }

    operator fun get(url: String): String? {
        var conn: HttpURLConnection? = null
        try {
            // 利用string url构建URL对象
            val mURL = URL(url)
            conn = mURL.openConnection() as HttpURLConnection

            conn.requestMethod = "GET"
            conn.readTimeout = 10000
            conn.connectTimeout = 10000

            val responseCode = conn.responseCode
            if (responseCode == 200) {

                val `is` = conn.inputStream
                return getStringFromInputStream(`is`)
            } else {
                throw NetworkErrorException("response status is $responseCode")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

            conn?.disconnect()
        }

        return null
    }

    @Throws(IOException::class)
    private fun getStringFromInputStream(is: InputStream): String {
        val os = ByteArrayOutputStream()
        // 模板代码 必须熟练
        val buffer = ByteArray(1024)
        var len = -1
        while ((len = is.read(buffer)) != -1) os.write(buffer, 0, len)
        is.close()
        val state = os.toString()// 把流中的数据转换成字符串,采用的编码是utf-8(模拟器默认编码)
        os.close()
        return state
    }
}
