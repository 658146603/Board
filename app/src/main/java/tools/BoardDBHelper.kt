package tools

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class BoardDBHelper private constructor(context: Context) : SQLiteOpenHelper(context, dbname, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {

        db.execSQL(CREATE_TABLE_MSG)
        db.execSQL(CREATE_TABLE_USER_INFO)
        db.execSQL(CREATE_TABLE_PUBLIC_INFO)

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {

        for (i in oldVersion until newVersion) {
            when (i) {
                2 -> upgradeToVersion2(db)
                3 -> upgradeToVersion3(db)

                else -> {
                }
            }
        }
    }

    private fun upgradeToVersion2(db: SQLiteDatabase) {
        //        String sql1 = "ALTER TABLE msg ADD COLUMN comment BLOB";
        //        db.execSQL(sql1);
    }

    private fun upgradeToVersion3(db: SQLiteDatabase) {

    }

    companion object {

        private val dbname = "board.db"

        private val CREATE_TABLE_MSG = "create table msg(_id INTEGER PRIMARY KEY AUTOINCREMENT,id INTEGER not null unique,userid text not null,time text not null,content text not null,haspic INTEGER not null,picture BLOB,comment BLOB)"
        private val CREATE_TABLE_USER_INFO = "create table userinfo(id INTEGER primary key autoincrement,userid text not null,nickname text not null,portrait BLOB not null,email text not null,checktime text not null,priority integer not null,token text not null,data BLOB)"
        private val CREATE_TABLE_PUBLIC_INFO = "create table publicinfo(id INTEGER primary key autoincrement,userid text not null unique,nickname text not null,portrait BLOB not null)"

        private val DB_VERSION = 1

        private val msgDBHelper: BoardDBHelper? = null

        fun getMsgDBHelper(context: Context): BoardDBHelper {

            return msgDBHelper ?: BoardDBHelper(context)

        }
    }

}
