package vsper.app.update

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Provides application storage paths
 */
internal object StorageUtils {
    private const val TAG = "StorageUtils"
    private const val EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE"

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * *("/Android/data/[app_package_name]/cache")* if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache [directory][File]
     */
    fun getCacheDirectory(context: Context): File? {
        var appCacheDir: File? = null
        if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() && hasExternalStoragePermission(
                context
            )
        ) {
            appCacheDir = getExternalCacheDir(context)
        }
        if (appCacheDir == null) {
            appCacheDir = context.cacheDir
        }
        if (appCacheDir == null) {
            Log.w(TAG, "Can't define system cache directory! The app should be re-installed.")
        }
        return appCacheDir
    }

    private fun getExternalCacheDir(context: Context): File? {
        val dataDir = File(File(context.externalCacheDir, "Android"), "data")
        val appCacheDir = File(File(dataDir, context.packageName), "cache")
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Log.w(TAG, "Unable to create external cache directory")
                return null
            }
            try {
                File(appCacheDir, ".nomedia").createNewFile()
            } catch (e: IOException) {
                Log.i(TAG, "Can't create \".nomedia\" file in application external cache directory")
            }
        }
        return appCacheDir
    }

    private fun hasExternalStoragePermission(context: Context): Boolean {
        val perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION)
        return perm == PackageManager.PERMISSION_GRANTED
    }
}