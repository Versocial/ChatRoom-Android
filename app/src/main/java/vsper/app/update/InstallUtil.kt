package vsper.app.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException


/*what*/object InstallUtil {
    /**
     * 调用系统安装程序安装下载好的apk
     * @param apkFile
     */
    fun installAPk(context: Context, apkFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
        intent.putExtra("name", "")
        intent.addCategory("android.intent.category.DEFAULT")
        try {
            val command = arrayOf("chmod", "777", apkFile.toString()) //777代表权限 rwxrwxrwx
            val builder = ProcessBuilder(*command)
            builder.start()
        } catch (ignored: IOException) {
        }
        val uri: Uri = FileProvider.getUriForFile(
            context,
            context.packageName + ".fileProvider",
            apkFile
        )

        val install = Intent(Intent.ACTION_VIEW)
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //添加这一句表示对目标应用临时授权该Uri所代表的文件
        install.setDataAndType(uri, "application/vnd.android.package-archive")
        context.startActivity(install)

    }
}