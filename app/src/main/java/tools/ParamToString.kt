package tools

import org.json.JSONArray

import java.util.ArrayList

object ParamToString {

    fun formLogin(userid: String?, password: String): String {
        var userid = userid
        userid = StringUtil.toURLEncoded(userid)
        return "method=login" + "&userid=" + userid + "&password=" + MD5Util.getMd5(password)

    }

    fun formAutoLogin(userid: String?, token: String): String {
        var userid = userid
        userid = StringUtil.toURLEncoded(userid)
        return "method=autologin&userid=$userid&token=$token"
    }

    fun formRegister(nickname: String?, password: String): String {
        var nickname = nickname
        nickname = StringUtil.toURLEncoded(nickname)
        return "method=register" + "&nickname=" + nickname + "&password=" + MD5Util.getMd5(password)

    }

    fun formChangePassword(userid: String?, password: String, newpassword: String): String {
        var userid = userid
        userid = StringUtil.toURLEncoded(userid)
        return "method=changepassword&userid=$userid&password=$password&newpassword=$newpassword"
    }

    fun formChangeUserInfo(userid: String, token: String, updateInfo: Map<String, String>): String {
        val result = StringBuilder("method=changeinfo&userid=" + StringUtil.toURLEncoded(userid) + "&token=" + token)
        for (key in updateInfo.keys) {
            result.append("&").append(key).append("=").append(StringUtil.toURLEncoded(updateInfo[key]))
        }
        return result.toString()
    }

    fun formGetPublicInfo(userids: String): String {
        return "method=getpublicinfo&userids=$userids"
    }

    fun formGetUSerInfo(userid: String, token: String): String {
        return "method=getuserinfo&userid=$userid&token=$token"
    }

    fun formGetMsg(userid: String, token: String, id: String): String {
        return "method=checknew&userid=$userid&token=$token&msgid=$id"
    }

    fun formSendMsg(userid: String?, token: String, content: String?, haspics: Int, pics: String): String {
        var userid = userid
        var content = content
        userid = StringUtil.toURLEncoded(userid)
        content = StringUtil.toURLEncoded(content)
        return "method=add&userid=$userid&content=$content&token=$token&haspics=$haspics&pics=$pics"
    }

    fun formDelMsg(userid: String, token: String, msgid: String): String {
        return "method=deletemsg&userid=$userid&token=$token&msgid=$msgid"
    }

    fun formDelMsg(userid: String, token: String, msgid: Int): String {
        return "method=deletemsg&userid=$userid&token=$token&msgid=$msgid"
    }

    fun formUpdatePublicUserInfo(userInfoMd5Array: ArrayList<Map<String, String>>): String {
        val jsonArray = JSONArray(userInfoMd5Array)
        return "method=updatepublicinfo&usermap=" + jsonArray.toString()
    }

    fun formAddComment(msgid: String, userid: String, token: String, comment: String): String {
        val result = "method=comment&targetmsgid=" + msgid + "&userid=" + StringUtil.toURLEncoded(userid) + "&token=" + StringUtil.toURLEncoded(token) + "&comment=" + StringUtil.toURLEncoded(comment)
        return result
    }

    fun formCheckNewComment(): String? {
        return null
    }

    fun formDelComment(): String? {
        return null
    }

}
