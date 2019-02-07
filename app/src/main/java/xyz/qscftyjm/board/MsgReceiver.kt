package xyz.qscftyjm.board

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class MsgReceiver : BroadcastReceiver() {

    private var message: Message? = null

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "有新的留言", Toast.LENGTH_SHORT).show()
        val len = intent.getStringExtra("msg")
        message!!.getMsg(len)
    }

    fun setMessage(message: Message) {
        this.message = message
    }

    internal interface Message {
        fun getMsg(str: String)
    }
}
