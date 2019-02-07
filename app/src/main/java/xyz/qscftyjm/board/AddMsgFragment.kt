package xyz.qscftyjm.board

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.Objects

import postutil.AsyncTaskUtil
import pub.devrel.easypermissions.EasyPermissions
import tools.BitmapIOUtil
import tools.BoardDBHelper
import tools.ParamToString
import tools.StringCollector


class AddMsgFragment : DialogFragment(), EasyPermissions.PermissionCallbacks, View.OnClickListener {
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var img_add_pic: ImageView? = null
    private var pics: Array<ImageView>? = null
    private var bt_submit: Button? = null
    private var et_content: EditText? = null
    private var isHasPic = false
    private var userid: String? = null
    private var token: String? = null
    private var add_msg_img_bar: LinearLayout? = null
    private val imagePath = arrayOf<String>(3)
    private val bitmaps = arrayOf<Bitmap>(3)
    private val haspics = booleanArrayOf(false, false, false)
    private var IMAGE_FILE_LOCATION_DIR: String? = null
    private val imageUri = arrayOf<Uri>(3)
    private val temp_picture = arrayOf<File>(3)

    private var database: SQLiteDatabase? = null

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        Objects.requireNonNull<FragmentActivity>(activity).getWindowManager().getDefaultDisplay().getMetrics(dm)
        dialog.window!!.setLayout(dm.widthPixels, dialog.window.attributes.height)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.fragment_add_msg, container)
        database = BoardDBHelper.getMsgDBHelper(getApplicationContext()).writableDatabase
        pics = arrayOfNulls(size = 3)
        img_add_pic = view.findViewById(R.id.add_msg_add_img)
        add_msg_img_bar = view.findViewById(R.id.add_msg_img_bar)
        pics[0] = view.findViewById(R.id.add_msg_img0)
        pics[1] = view.findViewById(R.id.add_msg_img1)
        pics[2] = view.findViewById(R.id.add_msg_img2)
        bt_submit = view.findViewById(R.id.add_msg_submit)
        et_content = view.findViewById(R.id.add_msg_content)
        pics!![0].setOnClickListener(this)
        pics!![1].setOnClickListener(this)
        pics!![2].setOnClickListener(this)

        val cursor = database!!.query("userinfo", arrayOf("userid", "token"), null, null, null, null, "id desc", "0,1")

        if (cursor.moveToFirst() && cursor.count > 0) {
            userid = cursor.getString(0)
            token = cursor.getString(1)
        } else {
            Toast.makeText(activity, "请登录账号！", Toast.LENGTH_LONG).show()
            startActivity(Intent(activity, LoginActivity::class.java))
            dismiss()
        }
        cursor.close()

        bt_submit!!.setOnClickListener(View.OnClickListener {
            val content = et_content!!.text.toString()
            if (content.length <= 0) {
                Toast.makeText(activity, "留言不能为空！", Toast.LENGTH_LONG).show()
                return@OnClickListener
            }

            isHasPic = haspics[0] || haspics[1] || haspics[2]
            if (!isHasPic) {
                // 没有图片
                AsyncTaskUtil.AsyncNetUtils.post(StringCollector.msgServer, ParamToString.formSendMsg(userid, token, content, 0, null)) { response ->
                    val jsonObj: JSONObject
                    val code: Int
                    if (response != null) {
                        try {
                            jsonObj = JSONObject(response)
                            code = jsonObj.optInt("code", -1)
                            Logd(jsonObj.optString("msg", "unknown error"))
                            if (code == 0) {
                                dismiss()
                            } else if (code != -1) {
                                if (code == -101) {
                                    val intent = Intent(activity, LoginActivity::class.java)
                                    val bundle = Bundle()
                                    bundle.putString("userid", userid)
                                    intent.putExtras(bundle)
                                    startActivity(intent)
                                    dismiss()
                                } else if (code == -99) {
                                    makeToast("留言发生错误")
                                }
                            } else {
                                Logd("sever error")
                            }
                        } catch (e: JSONException) {
                            Logd("服务器返回数据错误")
                            makeToast("服务器返回数据错误")
                        }

                    }
                }
            } else {
                // 有图片
                Log.d("Pics", "HasPic")
                val sendPicArray = ArrayList<Bitmap>()
                val hexPic = ArrayList<String>()
                for (i in 0..2) {
                    if (haspics[i] && bitmaps[i] != null) {
                        sendPicArray.add(bitmaps[i])
                        try {
                            val baos = ByteArrayOutputStream()
                            bitmaps[i].compress(Bitmap.CompressFormat.WEBP, 70, baos)
                            hexPic.add(BitmapIOUtil.bytesToHexString(baos.toByteArray()))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }

                if (hexPic.size > 0) {
                    val jsonArray = JSONArray(hexPic)
                    AsyncTaskUtil.AsyncNetUtils.post(StringCollector.msgServer, ParamToString.formSendMsg(userid, token, content, hexPic.size, jsonArray.toString())) { response ->
                        val jsonObj: JSONObject
                        val code: Int
                        if (response != null) {
                            try {
                                jsonObj = JSONObject(response)
                                code = jsonObj.optInt("code", -1)
                                Logd(jsonObj.optString("msg", "unknown error"))
                                if (code == 0) {
                                    dismiss()
                                } else if (code != -1) {
                                    if (code == -101) {
                                        val intent = Intent(activity, LoginActivity::class.java)
                                        val bundle = Bundle()
                                        bundle.putString("userid", userid)
                                        intent.putExtras(bundle)
                                        startActivity(intent)
                                        dismiss()
                                    } else if (code == -99) {
                                        makeToast("留言发生错误")
                                    }
                                } else {
                                    Logd("sever error")
                                }
                            } catch (e: JSONException) {
                                Logd("服务器返回数据错误")
                                makeToast("服务器返回数据错误")
                            }

                        }
                    }
                } else {
                    AsyncTaskUtil.AsyncNetUtils.post(StringCollector.msgServer, ParamToString.formSendMsg(userid, token, content, 0, null)) { response ->
                        val jsonObj: JSONObject
                        val code: Int
                        if (response != null) {
                            try {
                                jsonObj = JSONObject(response)
                                code = jsonObj.optInt("code", -1)
                                Logd(jsonObj.optString("msg", "unknown error"))
                                if (code == 0) {
                                    dismiss()
                                } else if (code != -1) {
                                    if (code == -101) {
                                        val intent = Intent(activity, LoginActivity::class.java)
                                        val bundle = Bundle()
                                        bundle.putString("userid", userid)
                                        intent.putExtras(bundle)
                                        startActivity(intent)
                                        dismiss()
                                    } else if (code == -99) {
                                        makeToast("留言发生错误")
                                    }
                                } else {
                                    Logd("sever error")
                                }
                            } catch (e: JSONException) {
                                Logd("服务器返回数据错误")
                                makeToast("服务器返回数据错误")
                            }

                        }
                    }
                }
            }
        })

        img_add_pic!!.setOnClickListener {
            img_add_pic!!.visibility = View.GONE
            add_msg_img_bar!!.visibility = View.VISIBLE
        }

        return view
    }

    private fun Logd(log: String) {
        try {
            Log.d("AMF", log)
        } catch (ignored: Exception) {
        }

    }

    private fun getPermission() {
        if (!EasyPermissions.hasPermissions(activity, *permissions)) {
            EasyPermissions.requestPermissions(this, "我们需要获取您的相册使用权限", 1, *permissions)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(activity, "请同意相关权限，否则无法选择图片", Toast.LENGTH_SHORT).show()
    }

    private fun makeToast(str: String) {
        try {
            Toast.makeText(activity, str, Toast.LENGTH_SHORT).show()
        } catch (ignored: Exception) {
        }

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.add_msg_img0 -> ChoosePic(0)
            R.id.add_msg_img1 -> if (haspics[0]) {
                //选择图片
                ChoosePic(1)
            }
            R.id.add_msg_img2 -> if (haspics[1]) {
                //选择图片\
                ChoosePic(2)
            }
        }
    }

    private fun ChoosePic(i: Int) {
        getPermission()
        val intent = Intent(Intent.ACTION_PICK, null)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        startActivityForResult(intent, RESULT_LOAD_IMAGE + i)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode <= RESULT_LOAD_IMAGE + 2 && requestCode >= RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
            val c = Objects.requireNonNull<FragmentActivity>(activity).getContentResolver().query(selectedImage, filePathColumns, null, null, null)
            Objects.requireNonNull<Cursor>(c).moveToFirst()
            val columnIndex = c!!.getColumnIndex(filePathColumns[0])
            imagePath[requestCode - 10] = c!!.getString(columnIndex)
            getCropImage(data.data, requestCode - 10)
            c!!.close()

        } else if (requestCode >= RESULT_CROP_IMAGE && requestCode <= RESULT_CROP_IMAGE + 2 && resultCode == Activity.RESULT_OK) {
            val bitmap = BitmapFactory.decodeFile(temp_picture[requestCode - 20].path)
            pics!![requestCode - 20].setImageBitmap(bitmap)
            bitmaps[requestCode - 20] = bitmap
            haspics[requestCode - 20] = true
            if (requestCode < 22) {
                pics!![requestCode - 19].visibility = View.VISIBLE
            }
        } else {
            Log.d("AMF", "requestCode: $requestCode resultCode: $resultCode")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun getCropImage(uri: Uri?, i: Int) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")

        intent.putExtra("crop", "true")
        intent.putExtra("scale", true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)

        intent.putExtra("outputX", 512)
        intent.putExtra("outputY", 512)

        intent.putExtra("return-data", false)
        IMAGE_FILE_LOCATION_DIR = Objects.requireNonNull<FragmentActivity>(activity).getExternalCacheDir()!! + "/xyz.qscftyjm.board/"
        val temp_dir = File(IMAGE_FILE_LOCATION_DIR)
        if (!temp_dir.exists()) {
            temp_dir.mkdir()
        }
        temp_picture[i] = File(temp_dir, "temp_picture$i.jpg")
        try {
            if (temp_picture[i].exists()) {
                temp_picture[i].delete()
            }
            temp_picture[i].createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        imageUri[i] = Uri.parse("file://" + IMAGE_FILE_LOCATION_DIR + "temp_picture" + i + ".jpg")
        Log.d("AMF", "PICURI " + i + " : " + imageUri[i].toString())

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri[i])
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true)

        startActivityForResult(intent, RESULT_CROP_IMAGE + i)
    }

    companion object {

        private val RESULT_LOAD_IMAGE = 10
        private val RESULT_CROP_IMAGE = 20
    }

}
