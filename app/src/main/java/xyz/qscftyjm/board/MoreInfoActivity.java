package xyz.qscftyjm.board;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MoreInfoActivity extends AppCompatActivity {

    public final static String TAG = "Board";

    private TextView tv_userid,tv_nickname,tv_email;
    private ImageView img_portrait;
    private Button bt_change_info;
    String userid,nickname,email;
    Bitmap bitmap_portrait;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_info);
        tv_userid=findViewById(R.id.user_info_userid);
        tv_nickname=findViewById(R.id.user_info_nickname);
        tv_email=findViewById(R.id.user_info_email);
        img_portrait=findViewById(R.id.user_info_portrait);
        bt_change_info=findViewById(R.id.user_info_change_info);
        bt_change_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"Change Info");
                // TODO 弹出窗口更改信息
            }
        });

        setUserInfo(tv_userid,tv_nickname,tv_email,img_portrait);
    }

    public int setUserInfo(TextView tv_userid, TextView tv_nickname,TextView tv_email, ImageView img_portrait){
        SQLiteDatabase db=BoardDBHelper.getMsgDBHelper(MoreInfoActivity.this).getWritableDatabase();
        Cursor cursor=db.query("userinfo",new String[]{"userid","nickname","portrait","email","priority","token"},null,null,null,null,"id desc","0,1");
        String token;int count=0;
        if(cursor.moveToFirst()){
            count=cursor.getCount();
            if(count>0){

                do{
                    this.userid=cursor.getString(0);
                    tv_userid.setText(this.userid);
                    this.nickname=cursor.getString(1);
                    tv_nickname.setText(nickname);

                    byte[] byte_portrait=cursor.getBlob(2);
                    //this.bitmap_portrait = BitmapFactory.decodeByteArray(byte_portrait, 0, byte_portrait.length);
                    img_portrait.setImageBitmap(BitMapUtil.getBitmap(MoreInfoActivity.this,byte_portrait));

                    this.email=cursor.getString(3);
                    tv_email.setText(this.email);
                    token=cursor.getString(5);
                }while (cursor.moveToNext());

                cursor.close();

            }
        }
        return count;
    }
}
