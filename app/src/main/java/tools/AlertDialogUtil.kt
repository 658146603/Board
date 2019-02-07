package tools

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.support.v7.app.AlertDialog
import android.widget.TextView

import xyz.qscftyjm.board.LoginActivity

object AlertDialogUtil {

    private val RESULT_LOAD_IMAGE = 10

    fun makeRegisterResultDialog(context: Context, userid: String, nickname: String) {

        val ac = TextView(context)
        ac.height = 120
        ac.setLines(2)
        ac.text = "你的账号为 ： $userid\n后续登录账号仅以该账号和密码登录有效"
        AlertDialog.Builder(context).setTitle("注册成功，欢迎 $nickname ！")
                .setView(ac)
                .setPositiveButton("跳转登录界面") { dialogInterface, i ->
                    val intent = Intent(context, LoginActivity::class.java)
                    context.startActivity(intent)
                    (context as Activity).finish()
                }.show()

    }

}