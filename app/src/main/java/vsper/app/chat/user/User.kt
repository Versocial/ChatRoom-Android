package vsper.app.chat.user

import com.stfalcon.chatkit.commons.models.IUser
import org.json.JSONObject
import vsper.Config

/*
* Created by verso.
*/
class User(
    val type: String = userType_usr,//detail
    private val id: String,// not detail
    private var name: String,//detail
    private var avatar: String,//detail
    var isOnline: Boolean// not detail
) : IUser {
    companion object {
        val defaultAvatar = Config.defaultUserAvatar

        fun defaultSys(id: String, name: String) =
            User(User.userType_sys, id, "$id(sys)", defaultAvatar, true)

        fun defaultNpc(id: String, name: String) =
            User(User.userType_npc, id, "$id(npc)", defaultAvatar, true)

        const val userType_sys = "sys"
        const val userType_npc = "npc"
        const val userType_usr = "usr"
        private const val avatar_ = "avatar"
        private const val nick_ = "nick"


        fun from_Db(type: String, id: String, detail: String): User {
            val json = JSONObject(detail)
            val avatar = json.getString(avatar_)
            val name = json.getString(nick_)
            return User(type, id, name, avatar, false)
        }


    }

    fun updateInfoByDetail(detailStr: String) {
        val detail = JSONObject(detailStr)
        if (detail.has(avatar_)) {
            this.avatar = detail.getString(avatar_)
        }
        if (detail.has(nick_)) {
            this.name = detail.getString(nick_)
        }
    }

    fun detail_Info(): String = detail_Db()

    fun detail_Db(): String =
        JSONObject().apply {
            put(nick_, name)
            put(avatar_, avatar)
        }.toString()

    override fun getId(): String {
        return id
    }

    override fun getName(): String {
        return name
    }

    override fun getAvatar(): String {
        return avatar
    }

    fun setName(name: String) {
        this.name = name
    }

    fun setAvatar(avatar: String) {
        this.avatar = avatar
    }

}