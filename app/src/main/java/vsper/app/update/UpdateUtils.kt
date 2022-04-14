package vsper.app.update

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import vsper.app.R
import vsper.app.utils.AppUtils

public object UpdateUtils {
    private const val TAG = "update"
    private var apkUrl: String = ""
    private var updateMessage: String = "default"

    private var latestVersion: Long = AppUtils.versionCode
    private var serverAvailable = false

    fun update(context: Context, lifecycleOwner: LifecycleOwner) {
        lifecycleOwner.lifecycleScope.launch {
            val result = DownloadUtil.getLatestVersionInfo()
            if (result == null) {
                AppUtils.toast("从服务器获取更新信息失败")
                return@launch
            }
            parseJson(result)

            if (!serverAvailable) {
                AppUtils.toast("抱歉，服务器还没准备好。")
                return@launch
            }

            //取得已经安装在手机的APP的版本号 versionCode
            val versionCode = AppUtils.versionCode

            //对比版本号判断是否需要更新
            if (latestVersion > versionCode) {
                showDialog(context, updateMessage, apkUrl)
            } else {
                AppUtils.toast(R.string.there_no_new_version)
            }

        }

    }

    private fun parseJson(result: String) {
        try {
            val obj = JSONObject(result)
            apkUrl = obj.getString("url") //APK下载路径
            updateMessage = obj.getString("updateMessage") //版本更新说明
            latestVersion = obj.getLong("versionCode") //新版APK对于的版本号
            serverAvailable = obj.getBoolean("available")
        } catch (e: JSONException) {
            Log.e(TAG, "parse json error")
        }
    }


    fun showDialog(context: Context, statement: String, downloadUrl: String) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.dialog_choose_update_title)
        builder.setMessage(statement)
            .setPositiveButton(R.string.dialog_btn_confirm_download)
            { dialog, id ->
                //下载apk文件
                val intent = Intent(context, DownLoadApkService::class.java)
                intent.putExtra("url", apkUrl)
                //android8.0以上通过startForegroundService启动service
//                startForegroundService(intent)
                context.startService(intent) // 启动Service
            }
            .setNegativeButton(R.string.dialog_btn_cancel_download)
            { dialog, id ->

            }
        val dialog = builder.create()
        //点击对话框外面,对话框不消失
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
    }
}