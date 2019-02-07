package tools

import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.sql.Blob
import java.sql.SQLException

internal object StringUtil {

    @Throws(SQLException::class, IOException::class)
    fun BlobToString(blob: Blob): String {

        val reString: String
        val `is` = blob.binaryStream
        val bais = `is` as ByteArrayInputStream
        val byte_data = ByteArray(bais.available()) //bais.available()返回此输入流的字节数
        bais.read(byte_data, 0, byte_data.size) //将输入流中的内容读到指定的数组
        reString = String(byte_data, StandardCharsets.UTF_8) //再转为String，并使用指定的编码方式
        `is`.close()

        return reString
    }


    fun toURLEncoded(paramString: String?): String? {
        var str = paramString
        if (paramString == null || paramString == "") {
            return ""
        }
        try {
            str = URLEncoder.encode(str, "UTF-8")
            return str
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
        }

        return ""
    }
}
