package vsper.app.global

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast

public class VsperApplication : Application() {
    companion object {
        private val info_ = "info"
        private val short_ = "short"
        private val TAG = "VsperApplication"

        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context


        private val toaster: Handler = object : Handler(Looper.getMainLooper()) {
            override fun handleMessage(msg: android.os.Message) {
                val info = msg.data.getString(info_)
                val short = msg.data.getBoolean(short_)
                if (short)
                    Toast.makeText(context, info, Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(context, info, Toast.LENGTH_LONG).show()
            }
        }

        fun shortToast(info: String, short: Boolean) {
            toaster.sendMessage(android.os.Message().apply {
                data = Bundle().apply {
                    putString(info_, info)
                    putBoolean(short_, short)
                }
            })
        }
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        GlobalRegistry.vsperInitialize()
        if (GlobalRegistry.autoLogin())
            Core.logIn()
    }

    override fun onTerminate() {
        Log.d(TAG, " is terminating.")
        Core.logOut()
        super.onTerminate()
        Log.d(TAG, " is terminated.")
    }

}