package vsper.app.chat.message

import org.json.JSONArray
import org.json.JSONObject
import vsper.app.chat.message.implementation.TextMsg
import vsper.app.chat.user.User
import vsper.app.global.Protocol

object MessageWebUtil {

    val TO: String = Protocol.FILED.TO
    val FROM: String = Protocol.FILED.FROM
    val TYPE: String = Protocol.FILED.TYPE
    val DIALOGID: String = Protocol.FILED.DIALOG
    val DETAIL: String = Protocol.FILED.DETAIL

    fun msgRecvFrom(msgId: String, dialogId: String, json: JSONObject): Message {
        val message: Message
        val userId = json.getString(FROM)
        when (json[TYPE].toString()) {
            else -> message = TextMsg.from_Web(msgId, dialogId, userId, json)
        }

        return message
    }

    fun msgSendTo(dialogId: String, msg: Message, users: ArrayList<User>?): String =
        JSONObject().apply {
            if (users != null) {
                put(TO, users.map { it.id })
            } else
                put(TO, JSONArray())
            put(TYPE, msg.type())
            put(DIALOGID, dialogId)
            put(DETAIL, msg.detail_Web())
        }.toString()


}