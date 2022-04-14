package vsper.app.global

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import vsper.app.R
import vsper.app.main.MainActivity

class ListenMsgService : Service() {
    companion object {
        val TAG = "listenMsgService"
    }

    private val channelId = "vsperListenMsgService"
    private val NOTIFICATION_ID = 2
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate ListenMsg executed")
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            channelId, "接收消息前台Service通知",
            NotificationManager.IMPORTANCE_MIN
        )
        notificationManager.createNotificationChannel(channel)
        val intent = Intent(this, MainActivity::class.java)
        val pi = PendingIntent.getActivity(this, 0, intent, 0)
        notificationBuilder = NotificationCompat.Builder(this, channelId)
        val notification = notificationBuilder
            .setContentTitle("正在监听网络消息")
            .setContentText("vsper")
            .setSmallIcon(R.drawable.ic_arrow1)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.ic_arrow2))
            .setContentIntent(pi)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        return super.onStartCommand(intent, flags, startId)
    }


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}