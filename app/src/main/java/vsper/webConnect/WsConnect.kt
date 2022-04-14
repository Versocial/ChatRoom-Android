package vsper.webConnect

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import vsper.app.global.GlobalRegistry
import vsper.eventChannel.event.ConLostEvent
import vsper.eventChannel.event.MsgEvent
import java.net.URI

class WsConnect(uri: URI, header: Map<String, String>) : WebSocketClient(uri, header) {
    private val TAG = "WsConnect"


    override fun onOpen(handshakedata: ServerHandshake?) {
        Log.d(TAG, "open ing")
    }

    override fun onMessage(message: String?) {
        val msg: String = if (message is String) message else "null"
        GlobalRegistry.eventChannel.addEvent(MsgEvent(msg))
        Log.d(TAG, "recving $message")
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        GlobalRegistry.eventChannel.addEvent(
            ConLostEvent(
                GlobalRegistry.logAccount
            )
        )

        Log.d(TAG, "close $code - $reason - $remote")
    }

    override fun onError(ex: Exception?) {
        if (ex != null) {
            Log.d(TAG, "error " + ex.printStackTrace())
        } else Log.d(TAG, "error null")
    }

    override fun send(text: String?) {
        Log.d(TAG, "sending:$text")
        super.send(text)
    }

}