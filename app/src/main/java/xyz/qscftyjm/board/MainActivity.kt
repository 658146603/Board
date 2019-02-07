package xyz.qscftyjm.board

import android.app.ActivityManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

import org.json.JSONException
import org.json.JSONObject

import java.util.ArrayList

import postutil.AsyncTaskUtil
import tools.BoardDBHelper
import tools.MainFragmentpagerAdapter
import tools.ParamToString
import tools.PublicUserInfoUpdater
import tools.StringCollector

class MainActivity : AppCompatActivity(), MsgReceiver.Message {
    private var bottomNavigationView: BottomNavigationView? = null
    private var mainMsgFragment: MainMsgFragment? = null
    private var mainChatFragment: MainChatFragment? = null
    private var mainUserFragment: MainUserFragment? = null
    private var mainFragList: ArrayList<Fragment>? = null
    private var mainViewPager: ViewPager? = null
    private var boardDBHelper: BoardDBHelper? = null
    private var database: SQLiteDatabase? = null
    private var msgReceiver: MsgReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        boardDBHelper = BoardDBHelper.getMsgDBHelper(this)
        database = boardDBHelper!!.writableDatabase

        bottomNavigationView = findViewById(R.id.main_bottom_navigation)
        mainViewPager = findViewById(R.id.main_parent_frag)

        mainMsgFragment = MainMsgFragment()
        mainChatFragment = MainChatFragment()
        mainUserFragment = MainUserFragment()
        mainFragList = ArrayList()
        mainFragList!!.add(mainMsgFragment)
        mainFragList!!.add(mainChatFragment)
        mainFragList!!.add(mainUserFragment)

        val fragmentAdapter = MainFragmentpagerAdapter(supportFragmentManager, mainFragList)
        mainViewPager!!.adapter = fragmentAdapter
        mainViewPager!!.currentItem = 0
        setListener()

        val cursor = database!!.query("userinfo", arrayOf("id", "userid", "nickname", "portrait", "email", "priority", "token"), null, null, null, null, "id desc", "0,1")
        var token: String
        var userid: String
        var id: Int
        if (cursor.moveToFirst() && cursor.count > 0) {
            do {
                id = cursor.getInt(0)
                userid = cursor.getString(1)
                token = cursor.getString(6)
            } while (cursor.moveToNext())

            cursor.close()
            val finalId = id
            AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formAutoLogin(userid, token)) { response ->
                val jsonObj: JSONObject
                if (response != null) {
                    Log.d(TAG, response)
                    try {
                        jsonObj = JSONObject(response)
                        val code = jsonObj.optInt("code", -1)
                        if (code == 0) {
                            val newToken = jsonObj.optString("token", "00000000000000000000000000000000")
                            Log.d("Board", "newToken: $newToken")
                            val values = ContentValues()
                            values.put("token", newToken)
                            database!!.update("userinfo", values, "id=?", arrayOf(finalId.toString()))
                            //Toast.makeText(MainActivity.this,"自动登录成功",Toast.LENGTH_SHORT).show();
                            msgReceiver = MsgReceiver()
                            val intentFilter = IntentFilter()
                            intentFilter.addAction("xyz.qscftyjm.board.HAS_NEW_MSG")
                            applicationContext.registerReceiver(msgReceiver, intentFilter)
                            msgReceiver!!.setMessage(this@MainActivity)

                            val startMsgSyncService = Intent(this@MainActivity, MsgSyncService::class.java)
                            if (!isServiceRunning("xyz.qscftyjm.board.MsgSyncService")) {
                                Log.d("MA", "StartService")
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    startForegroundService(startMsgSyncService)
                                } else {
                                    startService(startMsgSyncService)
                                }
                            } else {
                                Log.d("MA", "Serviec is running")
                            }

                        } else if (code < 0) {
                            Toast.makeText(this@MainActivity, jsonObj.optString("msg", "未知错误"), Toast.LENGTH_LONG).show()
                            // TODO 不同error code的处理
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                } else {
                    Toast.makeText(this@MainActivity, "服务器或网络异常", Toast.LENGTH_SHORT).show()
                }
            }

        } else {
            Toast.makeText(this, "请登录您的账号", Toast.LENGTH_SHORT).show()
        }

        //检查公开信息的更新
        PublicUserInfoUpdater.CheckPublicUserInfoUpdate(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        if (id == R.id.item_add_msg) {
            val fragment = AddMsgFragment()
            fragment.show(this.supportFragmentManager, "添加留言")
            return true
        }


        return if (id == R.id.item_huaji) {

            true
        } else super.onOptionsItemSelected(item)

    }

    private fun setListener() {
        mainViewPager!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNavigationView!!.selectedItemId = R.id.tab_msg
                    1 -> bottomNavigationView!!.selectedItemId = R.id.tab_chat
                    2 -> bottomNavigationView!!.selectedItemId = R.id.tab_user
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        bottomNavigationView!!.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.tab_msg -> {
                    mainViewPager!!.currentItem = 0
                    title = "留言板"
                    Log.d(TAG, "MSG")
                }
                R.id.tab_chat -> {
                    mainViewPager!!.currentItem = 1
                    title = "对话"
                    Log.d(TAG, "CHAT")
                }
                R.id.tab_user -> {
                    mainViewPager!!.currentItem = 2
                    title = "个人中心"
                    Log.d(TAG, "USER")
                }
            }
            true
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            applicationContext.unregisterReceiver(msgReceiver)
            Log.d("MA", "Broadcast closed successfully")
        } catch (e: IllegalArgumentException) {
            Log.d("MA", "Broadcast closed failed")
            e.printStackTrace()
        }

        if (isServiceRunning("xyz.qscftyjm.board.MsgSyncService")) {
            val intent = Intent(this@MainActivity, MsgSyncService::class.java)
            stopService(intent)
            //Log.d("MA","Service Stop");
        }

    }

    override fun getMsg(str: String) {
        Log.d("MA", "get broadcast")
    }

    private fun isServiceRunning(className: String): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = activityManager.getRunningServices(Integer.MAX_VALUE)
        if (info == null || info.size == 0) return false
        for (aInfo in info) {
            if (className == aInfo.service.className) return true
        }
        return false
    }

    override fun onResume() {
        super.onResume()
        if (msgReceiver == null) {
            msgReceiver = MsgReceiver()
            val intentFilter = IntentFilter()
            intentFilter.addAction("xyz.qscftyjm.board.HAS_NEW_MSG")
            applicationContext.registerReceiver(msgReceiver, intentFilter)
            msgReceiver!!.setMessage(this@MainActivity)

            val startMsgSyncService = Intent(this@MainActivity, MsgSyncService::class.java)
            if (!isServiceRunning("xyz.qscftyjm.board.MsgSyncService")) {
                Log.d("MA", "StartService")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(startMsgSyncService)
                } else {
                    startService(startMsgSyncService)
                }
            } else {
                Log.d("MA", "Service is running")
            }
        }
    }

    companion object {

        private val TAG = "Board"
    }
}
