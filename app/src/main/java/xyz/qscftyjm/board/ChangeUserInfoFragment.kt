package xyz.qscftyjm.board

import android.Manifest
import android.app.Activity
import android.app.DialogFragment
import android.content.ContentValues
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.HashMap
import java.util.Objects

import postutil.AsyncTaskUtil
import pub.devrel.easypermissions.EasyPermissions
import tools.BitmapIOUtil
import tools.BitmapUtil
import tools.BoardDBHelper
import tools.ParamToString
import tools.StringCollector
import tools.UserUtil


class ChangeUserInfoFragment : DialogFragment(), EasyPermissions.PermissionCallbacks {
    private val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private var img_portrait: ImageView? = null
    private var tv_userid: TextView? = null
    private var ed_nickname: EditText? = null
    private var ed_email: EditText? = null
    private var submit: Button? = null
    private var userInfo: Map<String, Any>? = null
    private var bitmap: Bitmap? = null
    private var IMAGE_FILE_LOCATION_DIR: String? = null
    private var imagePath: String? = null
    private var imageUri: Uri? = null
    private var temp_portrait: File? = null
    private var database: SQLiteDatabase? = null

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(dm)
        dialog.window!!.setLayout(dm.widthPixels, Objects.requireNonNull(dialog.window).attributes.height)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle): View? {
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = inflater.inflate(R.layout.change_info_layout, container)

        database = BoardDBHelper.getMsgDBHelper(activity).writableDatabase

        img_portrait = view.findViewById(R.id.change_info_portrait)
        tv_userid = view.findViewById(R.id.change_info_userid)
        ed_nickname = view.findViewById(R.id.change_info_nickname)
        ed_email = view.findViewById(R.id.change_info_email)
        submit = view.findViewById(R.id.change_info_submit)
        userInfo = UserUtil.getUserInfo(activity)
        img_portrait!!.setImageBitmap(userInfo!!["portrait"] as Bitmap?)
        tv_userid!!.text = userInfo!!["userid"] as String?
        ed_nickname!!.setText(userInfo!!["nickname"] as String?)
        ed_email!!.setText(userInfo!!["email"] as String?)
        submit!!.setOnClickListener(View.OnClickListener {
            Log.d("Board", "Submit change info")
            val cursor = database!!.query("userinfo", arrayOf("id", "userid", "nickname", "portrait", "email", "priority", "token"), null, null, null, null, "id desc", "0,1")
            var token: String
            var userid: String
            var id: Int
            if (cursor.moveToFirst()) {
                if (cursor.count > 0) {
                    val changeInfo = HashMap<String, String>()
                    do {
                        id = cursor.getInt(0)
                        userid = cursor.getString(1)
                        token = cursor.getString(6)
                    } while (cursor.moveToNext())

                    cursor.close()
                    val finalId = id

                    val values = ContentValues()
                    if (ed_nickname!!.text.toString().length >= 2 && ed_nickname!!.text.toString().length <= 15) {
                        values.put("nickname", ed_nickname!!.text.toString())
                        changeInfo["nickname"] = ed_nickname!!.text.toString()
                    } else {
                        Toast.makeText(activity, "Nickname 长度应在2~15个字符", Toast.LENGTH_SHORT).show()
                        return@OnClickListener
                    }
                    if (ed_email!!.text.toString().length >= 6 && ed_email!!.text.toString().length <= 30) {
                        values.put("email", ed_email!!.text.toString())
                        changeInfo["email"] = ed_email!!.text.toString()
                    } else {
                        Toast.makeText(activity, "Email 长度应在6~30个字符", Toast.LENGTH_SHORT).show()
                        return@OnClickListener
                    }


                    if (bitmap != null) {
                        values.put("portrait", BitmapIOUtil.bytesToHexString(BitmapUtil.Bitmap2Bytes(bitmap!!)))
                        val baos = ByteArrayOutputStream()
                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                        changeInfo["portrait"] = Objects.requireNonNull(BitmapIOUtil.bytesToHexString(baos.toByteArray()))
                    }

                    val finalUserid = userid
                    AsyncTaskUtil.AsyncNetUtils.post(StringCollector.userServer, ParamToString.formChangeUserInfo(userid, token, changeInfo)) { response ->
                        Log.d("Board", response)
                        val jsonObj: JSONObject
                        if (response != null) {
                            try {
                                jsonObj = JSONObject(response)
                                val code = jsonObj.optInt("code", -1)
                                if (code == 0) {

                                    Log.d("Board", "用户数据修改成功")
                                    //("用户数据修改成功")

                                    val cursor = database!!.query("userinfo", arrayOf("id", "userid", "nickname", "portrait", "email", "priority", "token"), null, null, null, null, "id desc", "0,1")
                                    val token: String
                                    var id: Int
                                    if (cursor.moveToFirst() && cursor.count > 0) {
                                        val changeInfo = HashMap<String, String>()
                                        do {
                                            id = cursor.getInt(0)
                                        } while (cursor.moveToNext())

                                        cursor.close()
                                        val finalId = id
                                        val values = ContentValues()
                                        token = jsonObj.optString("token", "00000000000000000000000000000000")
                                        values.put("token", token)

                                        database!!.update("userinfo", values, "id=?", arrayOf(finalId.toString()))
                                    }

                                } else if (code != -1) {
                                    if (code == -104) {
                                        try {
                                            val intent = Intent(activity, LoginActivity::class.java)
                                            val bundle = Bundle()
                                            bundle.putString("userid", finalUserid)
                                            intent.putExtras(bundle)
                                            startActivity(intent)
                                            Log.d("Intent", "Start LoginActivity")
                                        } catch (e: NullPointerException) {
                                            Log.d("Intent", "java.lang.NullPointerException")
                                        }

                                    }
                                    Log.d("Board", jsonObj.optString("msg", "未知错误"))
                                }

                            } catch (e: JSONException) {
                                e.printStackTrace()
                            }

                        } else {
                            Toast.makeText(activity, "网络错误，请稍后再试", Toast.LENGTH_SHORT).show()
                        }
                    }

                    database!!.update("userinfo", values, "id=?", arrayOf(finalId.toString()))
                }
            }
            dismiss()
        })

        img_portrait!!.setOnClickListener {
            Log.d("Board", "Pick image")
            getPermission()
            val intent = Intent(Intent.ACTION_PICK, null)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, RESULT_LOAD_IMAGE)
            //                getCropImage();
        }

        return view
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        // 打开存储权限
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val selectedImage = data.data
            val filePathColumns = arrayOf(MediaStore.Images.Media.DATA)
            val c = activity.contentResolver.query(Objects.requireNonNull(selectedImage), filePathColumns, null, null, null)
            Objects.requireNonNull(c).moveToFirst()
            val columnIndex = c!!.getColumnIndex(filePathColumns[0])
            imagePath = c.getString(columnIndex)
            //bitmap= BitmapFactory.decodeFile(imagePath);
            //img_portrait.setImageBitmap(bitmap);
            getCropImage(data.data)
            c.close()

        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            bitmap = BitmapFactory.decodeFile(temp_portrait!!.path)
            img_portrait!!.setImageBitmap(bitmap)
        } else {
            Log.d("Board", "requestCode: $requestCode resultCode: $resultCode")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun getCropImage(uri: Uri?) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")

        intent.putExtra("crop", "true")
        intent.putExtra("scale", true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)

        intent.putExtra("outputX", 256)
        intent.putExtra("outputY", 256)

        intent.putExtra("return-data", false)
        IMAGE_FILE_LOCATION_DIR = activity.externalCacheDir!!.toString() + "/xyz.qscftyjm.board/"
        val temp_dir = File(IMAGE_FILE_LOCATION_DIR)
        if (!temp_dir.exists()) {
            temp_dir.mkdir()
        }
        temp_portrait = File(temp_dir, "temp_portrait.jpg")
        try {
            if (temp_portrait!!.exists()) {
                temp_portrait!!.delete()
            }
            temp_portrait!!.createNewFile()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        imageUri = Uri.parse("file://" + IMAGE_FILE_LOCATION_DIR + "temp_portrait.jpg")
        Log.d("Board", "PICURI: " + imageUri!!.toString())

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", true)

        startActivityForResult(intent, 1)
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

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        //Toast.makeText(getActivity(), "相关权限获取成功", Toast.LENGTH_SHORT).show();
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        Toast.makeText(activity, "请同意相关权限，否则无法选择图片", Toast.LENGTH_SHORT).show()
    }

    override fun dismiss() {
        super.dismiss()
        startActivity(Intent(activity, MoreInfoActivity::class.java))
    }

    companion object {
        private val RESULT_LOAD_IMAGE = 10
    }

}
