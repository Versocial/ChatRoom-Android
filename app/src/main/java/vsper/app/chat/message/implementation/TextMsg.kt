package vsper.app.chat.message.implementation

import org.json.JSONObject
import vsper.app.chat.message.Message
import vsper.app.chat.user.User
import vsper.app.global.Core
import vsper.app.global.Protocol
import java.util.*

/*what*/class TextMsg(
    id: String, user: User, dialogId: String, createdAt: Date, text: String
) : Message(id, user, dialogId, createdAt, text) {

    companion object {
        const val type = Protocol.TYPE.TEXT

        fun from_Web(
            id: String,
            dialogId: String,
            userId: String,
            jsonObject: JSONObject
        ): TextMsg {
            var user = Core.getUser(userId)
            if (user == null) {
                Core.addNewUser(userId)
                user = Core.getUser(userId)!!
            }
            return TextMsg(id, user, dialogId, Date(), jsonObject.getString(Protocol.FILED.DETAIL))
        }

        fun from_Db(
            id: String,
            userId: String,
            dialog: String,
            date: Date,
            detail: String
        ): TextMsg {
            var user = Core.getUser(userId)
            if (user == null)
                user = Core.visitor
            return TextMsg(id, user, dialog, date, detail)
        }
    }

    override fun detail_Web(): String =
        text

    override fun detail_Db(): String =
        text

    override fun type(): String = type

}