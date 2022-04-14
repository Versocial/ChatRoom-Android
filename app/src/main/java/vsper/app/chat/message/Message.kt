package vsper.app.chat.message

import com.stfalcon.chatkit.commons.models.IMessage
import com.stfalcon.chatkit.commons.models.IUser
import com.stfalcon.chatkit.commons.models.MessageContentType
import org.json.JSONObject
import vsper.app.chat.user.User
import java.util.*

abstract class Message(
    private val id: String,//not detail
    private val user: User,//id not detail
    val dialogId: String,//not detail
    private val createdAt: Date = Date(),//not detail
    private val text: String = "default text",//detail
) : IMessage, MessageContentType.Image,
    MessageContentType /*and this one is for custom content type (in this case - voice message)*/ {

    open fun detail_Db(): String =
        JSONObject().toString()

    open fun detail_Web(): String =
        JSONObject().toString()

    open fun type(): String = "abstract"

    override fun getId(): String {
        return id
    }

    override fun getText(): String {
        return text
    }

    override fun getCreatedAt(): Date {
        return createdAt
    }

    override fun getUser(): IUser {
        return user
    }

    override fun getImageUrl(): String? {
        return null
    }

}