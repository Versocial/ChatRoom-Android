package vsper.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import vsper.app.global.VsperApplication

object AppUtils {
    public fun copyToClipboard(context: Context, copiedText: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(copiedText, copiedText)
        clipboard.setPrimaryClip(clip)
    }

    public fun toast(info: String) =
        VsperApplication.shortToast(info, true)

    public fun toast(info: Int) =
        AppUtils.toast(VsperApplication.context.getString(info))


    /**
     * 取得当前版本号
     * @return
     */
    val versionCode: Long
        get() {
            try {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    VsperApplication.context.packageManager.getPackageInfo(
                        VsperApplication.context.packageName,
                        0
                    ).longVersionCode
                } else {
                    VsperApplication.context.packageManager.getPackageInfo(
                        VsperApplication.context.packageName,
                        0
                    ).versionCode.toLong()
                }
            } catch (ignored: PackageManager.NameNotFoundException) {
            }
            return 0
        }

    val versionName: String
        get() {
            return VsperApplication.context.packageManager.getPackageInfo(
                VsperApplication.context.packageName,
                0
            ).versionName
        }

    fun isDarkTheme(context: Context): Boolean {
        val flag = context.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK
        return flag == Configuration.UI_MODE_NIGHT_YES

    }

    fun isDarkTheme(): Boolean = isDarkTheme(VsperApplication.context)

    fun getColor(context: Context=VsperApplication.context, colorId: Int): Int = context.resources.getColor(colorId, null)


}