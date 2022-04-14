package vsper.app.chat.user

import android.util.Log
import org.json.JSONObject
import vsper.app.global.Core
import vsper.app.global.GlobalRegistry
import vsper.app.global.Protocol
import vsper.webConnect.WebConnect

/*what*/class UserList {
    private val userList = HashMap<String, User>()

    companion object {
        const val sysId = "#sys-1"
        const val vistorId = "#npc-1"
        const val TAG = "userList"
    }

    val sysUser: User = User.defaultSys(sysId, "系统")
    val visitor: User = User.defaultNpc(vistorId, "游客")

    init {
        UserDbUtil.initList(userList)
        if (!userList.containsKey(sysId)) {
            userList[sysId] = sysUser
            UserDbUtil.save(sysUser)
        }
        if (!userList.containsKey(vistorId)) {
            userList[vistorId] = visitor
            UserDbUtil.save(visitor)
        }

    }

    fun onInfoRecv(msg: JSONObject) {
        val userId = msg.getString(Protocol.FILED.FROM)
        when (msg.get(Protocol.FILED.TYPE)) {
            Protocol.TYPE.USERQUERY -> {
                if (GlobalRegistry.logStatus() == GlobalRegistry.LogStatus.logined)
                    sendInfo(GlobalRegistry.user())
            }
            Protocol.TYPE.USERINFO -> {
                var user = Core.getUser(userId)
                if (user == null) {
                    user = addNewUser(userId)
                }
                user.updateInfoByDetail(msg.getString(Protocol.FILED.DETAIL))
                UserDbUtil.update(user)
            }
        }

    }

    fun queryInfo() =
        WebConnect.wsConnect.send(
            Protocol.msg(type = Protocol.TYPE.USERQUERY)
        )

    fun queryInfo(id: String) {
        WebConnect.wsConnect.send(
            Protocol.msg(
                type = Protocol.TYPE.USERQUERY,
                to = arrayListOf(id)
            )
        )
    }

    fun sendInfo(user: User) {
        WebConnect.wsConnect.send(
            Protocol.msg(
                type = Protocol.TYPE.USERINFO,
                detail = user.detail_Info()
            )
        )
    }


    fun addNewUser(id: String): User {
        var user = userList[id]
        if (user == null) {
            user = User(User.userType_usr, id, id, User.defaultAvatar, true)
            userList[id] = user
            UserDbUtil.save(user)
        } else {
            Log.e(TAG, "试图新建已存在的用户")
        }
        return user
    }

    fun getUser(id: String): User? =
        if (userList.containsKey(id)) {
            userList[id]
        } else null

    fun setOnline(id: String, isOnline: Boolean) {
        val user = userList[id]
        if (user != null) {
            user.isOnline = isOnline
        }
    }

    fun updateUser(id: String) {
        val user = userList[id]
        if (user != null)
            UserDbUtil.update(user)
    }

//    fun removeUser(id: String) {
//        if (userList.containsKey(id)) {
//            userList.remove(id)
//        }
//    }

    fun values() = userList.values
    fun keys()=userList.keys
}