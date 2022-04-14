package vsper.app.chat.dialog

import com.stfalcon.chatkit.commons.models.IDialog
import org.json.JSONArray
import org.json.JSONObject
import vsper.Config
import vsper.app.chat.message.Message
import vsper.app.chat.message.MessageDbUtil
import vsper.app.chat.message.MessageWebUtil
import vsper.app.chat.message.implementation.TextMsg
import vsper.app.chat.user.User
import vsper.app.global.Core
import vsper.app.global.GlobalRegistry
import vsper.app.global.Protocol
import vsper.eventChannel.event.ToShowMsgEvent
import vsper.webConnect.WebConnect
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*


class Dialog(
    private val id: String,
    private val dialogName: String,
    private var dialogPhoto: String,
    private val users: ArrayList<User>,
    private var lastMessage: Message,
    private var unreadCount: Int,
    val type: String
) :
    IDialog<Message> {
    companion object {
        const val dialogType_hall = "hall"
        const val dialogType_group = "group"
        const val dialogType_private = "private"
        const val dialogType_sys = "#sys"

        private val defaultPhoto = Config.defaultDialogPhoto
        private const val defaultMessageId: String = "-1"
        private fun defaultMsg() =
            TextMsg(defaultMessageId, Core.sysUser, defaultMessageId, Date(), "还没有消息，快来聊天吧")

        private const val users_ = "users"
        private const val photo_ = "photo"
        private const val name_ = "name"
        private const val day_ = "day"
        private const val unreadCount_ = "unread"
        private const val lastMessageId_ = "latestMsg"
        private const val latestRecvId_ = "latestRecv"
        private const val latestSendId_ = "latestSend"

        fun from_Db(type: String, id: String, detail: String): Dialog {
            val json = JSONObject(detail)
            val dialogPhoto = json.getString(photo_)
            val dialogName = json.getString(name_)
            val day = json.getString(day_)
            val unreadCount = json.getInt(unreadCount_)
            val lastMessageId = json.getString(lastMessageId_)
            val users = ArrayList<User>()
            json.getJSONArray(users_).apply {
                var i = 0
                while (i < length()) {
                    var user = Core.getUser(this.getString(i))
                    if (user == null) {
                        user = User.defaultNpc(this.getString(i), "未找到的用户:${this.getString(i)}")
                    }
                    users.add(user)
                    i++
                }
            }
//            var lastMessage = MessageDbUtil.queryMsg(lastMessageId)
            var lastMessage = MessageDbUtil.queryLatestMsg(id, Date())
            if (lastMessage == null)
                lastMessage = defaultMsg()
            val dialog = Dialog(id, dialogName, dialogPhoto, users, lastMessage, unreadCount, type)
            dialog.day = day
            return dialog
        }
    }

    fun detail_Db(): String = JSONObject().apply {
        put(photo_, dialogPhoto)
        put(name_, dialogName)
        put(day_, day)
        put(users_, JSONArray(users.map { it.id }))
        put(unreadCount_, unreadCount)
        put(lastMessageId_, lastMessage.id)
        put(latestRecvId_, latestRecvId)
        put(latestSendId_, latestSendId)
    }.toString()

    constructor(id: String, dialogName: String, users: ArrayList<User>) :
            this(id, dialogName, defaultPhoto, users, defaultMsg(), 0, dialogType_group) {
    }

    constructor(id: String, dialogName: String) :
            this(
                id,
                dialogName,
                defaultPhoto,
                Core.getUsers(),
                defaultMsg(),
                0,
                dialogType_hall
            ) {
    }


    private val dateFormatter = DateTimeFormatter.ISO_DATE
    var day: String = getDayNow()
    var latestRecvId: Int = 0
    var latestSendId: Int = 0

    init {
        //to update day string
        if (getDayNow() != day) {
            latestRecvId = 1
            day = getDayNow()
        }
    }


    public fun onMessageRecv(jsonObject: JSONObject) {
        when (jsonObject.getString(Protocol.FILED.TYPE)) {
            else -> parseMessage(jsonObject)
        }
    }

    private fun parseMessage(json: JSONObject) {
        //to update day string
        if (getDayNow() != day) {
            latestRecvId = 1
            day = getDayNow()
        } else
            latestRecvId++
        //to analyze the message
        val msgId = "$day<$latestRecvId"
        lastMessage = MessageWebUtil.msgRecvFrom(msgId, id, json)
        //to update unreadCount
        unreadCount++
        //to show msg
        GlobalRegistry.eventChannel.addEvent(
            ToShowMsgEvent(
                lastMessage, id
            )
        )
        //to save msg
        MessageDbUtil.save(lastMessage)
    }

    public fun sendTextMsg(message: String): TextMsg {
        val msg = TextMsg("$day>$latestSendId", GlobalRegistry.user(), id, Date(), text = message)
        latestSendId++
        val ToSendTo = if (type != dialogType_hall) users else null
        WebConnect.wsConnect.send(MessageWebUtil.msgSendTo(id, msg, ToSendTo))
        onMessageSend(msg)
        return msg
    }


    private fun onMessageSend(message: Message) {
//        MessageDbUtil.save(message)
        lastMessage = message
    }

    private fun getDayNow():
            String = LocalDateTime.now().format(dateFormatter)


    override fun getId(): String {
        return id
    }


    override fun getDialogName(): String {
        return dialogName
    }

    override fun getDialogPhoto(): String {
        return dialogPhoto
    }

    override fun getUsers(): ArrayList<User> {
        return users
    }

    override fun getLastMessage(): Message {
        return lastMessage
    }

    override fun setLastMessage(lastMessage: Message) {
        this.lastMessage = lastMessage
    }

    override fun getUnreadCount(): Int {
        return unreadCount
    }

    fun setUnreadCount(unreadCount: Int) {
        this.unreadCount = unreadCount
    }

}