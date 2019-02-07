package tools

import java.io.ByteArrayOutputStream
import java.io.FileInputStream

object BitmapIOUtil {

    fun bytesToHexString(bytes: ByteArray?): String? {
        val stringBuilder = StringBuilder()
        if (bytes == null || bytes.size <= 0) {
            return null
        }
        for (i in bytes.indices) {
            val v = bytes[i] and 0xFF
            val hv = Integer.toHexString(v)
            if (hv.length < 2) {
                stringBuilder.append(0)
            }
            stringBuilder.append(hv)
        }
        return stringBuilder.toString()
    }

    internal fun hexStringToByteArray(str: String): ByteArray {
        val len = str.length
        val data = ByteArray(len / 2)
        try {
            var i = 0
            while (i < len) {
                data[i / 2] = ((Character.digit(str[i], 16) shl 4) + Character.digit(str[i + 1], 16)).toByte()
                i += 2
            }
        } catch (ignored: Exception) {
        }

        return data
    }

    @Throws(Exception::class)
    fun ReadImage(imagepath: String): ByteArray {
        val fs = FileInputStream(imagepath)
        val outStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var len: Int
        while (-1 != (len = fs.read(buffer))) {
            outStream.write(buffer, 0, len)
        }
        outStream.close()
        fs.close()
        return outStream.toByteArray()
    }

}
