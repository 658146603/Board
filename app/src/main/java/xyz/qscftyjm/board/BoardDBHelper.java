package xyz.qscftyjm.board;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BoardDBHelper extends SQLiteOpenHelper {

    final static String dbname = "board.db";

    final static String CREATE_TABLE_MSG = "create table msg(_id INTEGER PRIMARY KEY AUTOINCREMENT,userid text,time text,content text,picture BLOB)";
    final static String CREATE_TABLE_USER_INFO = "create table userinfo(id INTEGER primary key autoincrement,userid text,nickname text,portrait BLOB,email text,checktime text,statue integer,token text,data BLOB)";
    final static String CREATE_TABLE_PUBLIC_INFO = "create table publicinfo(id INTEGER primary key autoincrement,userid text,nickname text,portrait BLOB)";

    final static int DB_VERSION = 1;

    public static BoardDBHelper msgDBHelper;

    public BoardDBHelper(Context context) {
        super(context, dbname, null, DB_VERSION);
    }

    public static BoardDBHelper getMsgDBHelper(Context context) {

        if(msgDBHelper==null){
            return new BoardDBHelper(context);
        }

        return msgDBHelper;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_MSG);
        db.execSQL(CREATE_TABLE_USER_INFO);
        db.execSQL(CREATE_TABLE_PUBLIC_INFO);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        for (int i = oldVersion; i < newVersion; i++) {
            switch (i) {
                case 2:
                    upgradeToVersion2(db);
                    break;
                case 3:
                    upgradeToVersion3(db);
                    break;

                default:
                    break;
            }
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
//        String sql1 = "ALTER TABLE userinfo ADD COLUMN id INTEGER";
//        db.execSQL(sql1);
    }

    private void upgradeToVersion3(SQLiteDatabase db) {

    }

}
