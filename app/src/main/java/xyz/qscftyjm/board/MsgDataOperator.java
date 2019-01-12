package xyz.qscftyjm.board;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import postutil.AsynTaskUtil;
import tools.ParamToJSON;
import tools.StringCollector;
import tools.TimeUtil;

public class MsgDataOperator {

    public final static String TAG = "Board";
    private static BoardDBHelper boardDBHelper;
    private static SQLiteDatabase database;


    public static ArrayList<Msg> getTestMsgData(Context context){
        ArrayList<Msg> msgs=new ArrayList<>();

        // public Msg(int id, String userid, String nickname, String time, String content, Bitmap portrait, boolean hasPic, Bitmap[] picture) {
        for(int i=0;i<10;i++){
            msgs.add(new Msg(i,"10001"+i,"wcf","2019/01/01 19:0"+i,"3132165136513213"+i,BitMapUtil.getDefaultPortrait(context),true,BitMapUtil.getDefaultPics(context)));
        }


        return msgs;
    }

    public static ArrayList<Msg> setTestMsgData(final Context context, final ListView listView){
        final ArrayList<Msg> msgs=new ArrayList<>();

        String token="";
        String time="1970/1/1 08:00:00";
        boardDBHelper=BoardDBHelper.getMsgDBHelper(context);
        database=boardDBHelper.getWritableDatabase();
        Cursor cursor;
        cursor = database.query("userinfo", new String[] {"token"}, null, null, null, null, "id desc", "0,1");
        int count = 0;
        if(cursor.moveToFirst()){
            count=cursor.getCount();
        }
        if(count>0) {
            do {
                token = cursor.getString(0);
                Log.d(TAG, "token : "+token);
            } while (cursor.moveToNext());
            cursor.close();
        }
        // public Msg(int id, String userid, String nickname, String time, String content, Bitmap portrait, boolean hasPic, Bitmap[] picture) {
        AsynTaskUtil.AsynNetUtils.post(StringCollector.getMsgServer(), ParamToJSON.formGetMsgJson(token,time), new AsynTaskUtil.AsynNetUtils.Callback() {

            @Override
            public void onResponse(String response) {
                if(response!=null){
                    JSONObject jsonObj;
                    Log.d(TAG, response);

                    try {
                        jsonObj=new JSONObject(response);
                        int code=jsonObj.optInt("code",-1);
                        if(code==200){
                            JSONObject data=jsonObj.optJSONObject("data");
                            if(data!=null){
                                String tTime=data.optString("time","1970/1/1 08:00:00");
                                Log.d(TAG,"time "+tTime);
                                JSONArray msgArr=data.optJSONArray("content");
                                if(msgArr!=null&&msgArr.length()>0){
                                    for (int i=0;i<msgArr.length();i++){
                                        msgs.add(new Msg(msgArr.getJSONObject(i).optInt("id",-1),msgArr.getJSONObject(i).optString("username","null"),"nickname",msgArr.getJSONObject(i).optString("time","1970/1/1 08:00:00"),msgArr.getJSONObject(i).optString("content","null"),BitMapUtil.getDefaultPortrait(context),true,BitMapUtil.getDefaultPics(context)));
//                                        ContentValues values=new ContentValues();
//                                        values.put("token", data.optString("credit","null"));
//                                        //values.put("username",data.optString("username","null"));
//                                        values.put("nickname",data.optString("nickname","null"));
//                                        values.put("checktime", TimeUtil.getTime());
//                                        String userid=data.optString("username","null");
//                                        database.update("userinfo", values, "userid = ?", new String[] { userid });
//                                        Log.d(TAG, "更新留言数据 ");
                                    }
                                } else {
                                    msgs.add(new Msg(0,"null","null","2019/01/01","没有新的留言",BitMapUtil.getDefaultPortrait(context),true,BitMapUtil.getDefaultPics(context)));
                                }

//                                ContentValues values=new ContentValues();
//                                values.put("token", data.optString("credit","null"));
//                                //values.put("username",data.optString("username","null"));
//                                values.put("nickname",data.optString("nickname","null"));
//                                values.put("checktime", TimeUtil.getTime());
//                                String userid=data.optString("username","null");
//                                database.update("userinfo", values, "userid = ?", new String[] { userid });
//                                Log.d(TAG, "更新账号数据 "+userid);
                                MsgListAdapter adapter=new MsgListAdapter(msgs,context);
                                //adapter.itemMoreClickListener(this);

                                listView.setAdapter(adapter);
                                adapter.notifyDataSetChanged();

                            }

                        } else if(code!=-1) {
                            Log.d(TAG,"token 过期");
                            Log.d(TAG,jsonObj.optString("msg","null"));
                        } else {
                            Log.d(TAG,"返回错误");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG,"服务器错误");
                }
            }
        });


//        for(int i=0;i<10;i++){
//            msgs.add(new Msg(i,"10001"+i,"wcf","2019-01-01 19:0"+i,"3132165136513213"+i,BitMapUtil.getDefaultPortrait(context),true,BitMapUtil.getDefaultPics(context)));
//        }


        return msgs;
    }

//    public void itemClick(View v) {
//        int position;
//        position = (Integer) v.getTag();
//        switch (v.getId()) {
//            case R.id.msg_more:
//                Log.d("Board", "more@"+position);
//                break;
//            case R.id.msg_head_portrait:
//                Log.d("Board", "portrait@"+position);
//                break;
//            default:
//                break;
//        }
//
//    }
}
