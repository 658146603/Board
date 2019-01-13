package tools;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

public class ParamToString {

    public static String formLogin(String userid, String password) {
        //method=login&userid=10001&password=E10ADC3949BA59ABBE56E057F20F883E
        String result="method=login"+"&userid="+userid+"&password="+MD5Util.getMd5(password);
        return result;

    }

    public static String formAutoLogin(String userid, String token) {
        //method=autologin&userid=10001&token=f2b03e3a64c142519fd1066714fa217b
        String result="method=autologin&userid="+userid+"&token="+token;
        return result;
    }

    public static String formRegister(String nickname, String password) {
        //method=register&nickname=test&password=E10ADC3949BA59ABBE56E057F20F883E
        String result="method=register"+"&nickname="+nickname+"&password="+MD5Util.getMd5(password);
        return result;

    }

    public static String formChangePassword(String userid, String password, String newpassword) {
        //method=changepassword&userid=10003&password=1222211221212121&newpassword=E10ADC3949BA59ABBE56E057F20F883E
        String result="method=changepassword&userid="+userid+"&password="+password+"&newpassword="+newpassword;
        return result;
    }



    public static String formChangeUserInfo(String userid, String token, Map<String,String> updateInfo) {
        //method=changeinfo&userid=10036&token=ffe2e588c0d34cceb82044acb6532f12&nickname=test10036&portrait=00000000&email=123456@gmail.com
        String result="method=changeinfo&userid="+userid+"&token="+token;
        for (String key : updateInfo.keySet()){
            result+="&"+key+"="+updateInfo.get(key);
        }
        Log.d("Board","result: "+result);
        return result;
    }

    public static String formGetPublicInfo(String[] userids) {
        //method=getpublicinfo&userids=['10001','10041','100']
        Log.d("Board","userids: "+userids.toString());
        String result="method=getpublicinfo&userids="+userids.toString();
        return result;
    }

    public static String formGetUSerInfo(String userid, String token) {
        //method=getuserinfo&userid=10001&token=f0956e4857564917ba13008debcd6432
        String result="method=getuserinfo&userid="+userid+"&token="+token;
        return result;
    }

    public static String formGetMsg(String token,String time) {
        String result=null;
        return result;
    }

    public static String formSendMsg(String userid, String token, String content, int haspic, Bitmap pics) {
        String result=null;
        return result;
    }

}
