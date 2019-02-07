package tools

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

object MD5Util {

    fun getMd5(input: String): String? {
        try {
            //拿到一个MD5转换器（如果想要SHA1加密参数换成"SHA1"）
            val messageDigest = MessageDigest.getInstance("MD5")
            //输入的字符串转换成字节数组
            val inputByteArray = input.toByteArray()
            //inputByteArray是输入字符串转换得到的字节数组
            messageDigest.update(inputByteArray)
            //转换并返回结果，也是字节数组，包含16个元素
            val resultByteArray = messageDigest.digest()
            //字符数组转换成字符串返回
            return byteArrayToHex(resultByteArray)


        } catch (e: NoSuchAlgorithmException) {
            return null
        }

    }

    private fun byteArrayToHex(byteArray: ByteArray): String {
        //首先初始化一个字符数组，用来存放每个16进制字符
        val hexDigits = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')
        //new一个字符数组，这个就是用来组成结果字符串的（解释一下：一个byte是八位二进制，也就是2位十六进制字符）
        val resultCharArray = CharArray(byteArray.size * 2)
        //遍历字节数组，通过位运算（位运算效率高），转换成字符放到字符数组中去
        var index = 0
        for (b in byteArray) {
            resultCharArray[index++] = hexDigits[b.ushr(4) and 0xf]
            resultCharArray[index++] = hexDigits[b and 0xf]
        }

        //字符数组组合成字符串返回
        return String(resultCharArray)
    }
}

