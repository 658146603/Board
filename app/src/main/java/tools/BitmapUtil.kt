package tools

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

import java.io.ByteArrayOutputStream

import xyz.qscftyjm.board.R

object BitmapUtil {

    internal fun getDefaultPortrait(context: Context): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.ic_huaji)
    }

    internal fun getDefaultPics(context: Context): Array<Bitmap> {
        val size = 3
        val bitmaps = arrayOfNulls<Bitmap>(size)
        bitmaps[0] = BitmapFactory.decodeResource(context.resources, R.drawable.chat)
        bitmaps[1] = BitmapFactory.decodeResource(context.resources, R.drawable.user)
        bitmaps[2] = BitmapFactory.decodeResource(context.resources, R.drawable.write)
        return bitmaps
    }

    fun getHexBitmap(context: Context, hexStr: String): Bitmap {
        val bitmap: Bitmap
        if (hexStr.length < 128) {
            bitmap = getDefaultPortrait(context)
        } else {
            val b = BitmapIOUtil.hexStringToByteArray(hexStr)
            bitmap = BitmapFactory.decodeByteArray(b, 0, b.size)
        }
        return bitmap
    }

    fun Bitmap2Bytes(bm: Bitmap): ByteArray {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
        return baos.toByteArray()
    }

}
