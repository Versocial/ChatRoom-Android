package vsper.app.global

import android.content.Intent
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import vsper.app.chat.dialog.Dialog
import vsper.app.chat.dialog.DialogDbUtil
import vsper.app.chat.user.User
import vsper.app.chat.user.UserList
import vsper.app.utils.AppUtils
import vsper.app.utils.DatabaseHelper
import vsper.eventChannel.event.ConLostEvent
import vsper.eventChannel.event.MsgEvent
import vsper.eventChannel.event.ToShowContactsEvent
import vsper.webConnect.WebConnect
import vsper.webConnect.WebRegistry

/*
* Created by Anton Bevza on 1/13/17.
*/
internal object Core {
    private const val TAG = "Core"
    const val dataBaseVersion = 3
    private val dialogList = HashMap<String, Dialog>()
    private const val dialog_chatHall: String = "#Chat_Hall#"
    private val userList: UserList = UserList()
    private var lostConToTry = 0//重连剩余次数
    private var lostRetry=false

    lateinit var chatHall: Dialog

    fun initialize() {
        DialogDbUtil.initList(dialogList)
        if (!dialogList.containsKey(dialog_chatHall)) {
            chatHall = Dialog(dialog_chatHall, "聊天大厅")
            dialogList[chatHall.id] = chatHall
            DialogDbUtil.save(chatHall)
        } else
            chatHall = dialogList[dialog_chatHall]!!
        if (GlobalRegistry.foreService()) {
            val intent = Intent(VsperApplication.context, ListenMsgService::class.java)
            VsperApplication.context.startService(intent)
        }

        GlobalRegistry.eventChannel.addListenerAlways("data write", MsgEvent::class) {
            val e = it as MsgEvent
            onMessageRecv(e.info)
        }

    }


    fun onMessageRecv(message: String) {
        Log.d("$TAG:recvMsg", message)
        var json: JSONObject = JSONObject()
        try {
            json = JSONObject(message)
        } catch (e: JSONException) {
            Log.d("$TAG:msg format error", message)
            return
        }
        when (json.getString(Protocol.FILED.TYPE)) {
            Protocol.TYPE.COMING -> {
                val detail=json.getString(Protocol.FILED.DETAIL)
                userList.setOnline(detail, true)
                GlobalRegistry.eventChannel.addEvent(ToShowContactsEvent(detail))
            }
            Protocol.TYPE.LEAVING -> {
                val detail=json.getString(Protocol.FILED.DETAIL)
                userList.setOnline(detail, false)
                GlobalRegistry.eventChannel.addEvent(ToShowContactsEvent(detail))
            }
            in Protocol.TYPE.USRTYPES -> {
                val from=json.getString(Protocol.FILED.FROM)
                userList.setOnline(from, true)
                userList.onInfoRecv(json)
                GlobalRegistry.eventChannel.addEvent(ToShowContactsEvent(from))
            }
            in Protocol.TYPE.MSGTYPES ->
                dialogList[json.getString(Protocol.TYPE.DIALOG)]?.onMessageRecv(json)
        }
    }

    fun initDialog(dialog: Dialog) {
        if (dialogList.containsKey(dialog.id))
            removeDialog(dialog.id)
        dialogList[dialog.id] = dialog
        DialogDbUtil.save(dialog)
    }

    fun getDialog(id: String): Dialog? =
        if (dialogList.containsKey(id)) {
            dialogList[id]
        } else null

    fun removeDialog(id: String) {
        if (dialogList.containsKey(id)) {
            dialogList.remove(id)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun logIn(retryNowIfLost:Boolean=false) {
        //关闭已经打开的连接
        logOut()
        GlobalRegistry.setLogStatus(GlobalRegistry.LogStatus.logining)

        val serverIP = WebRegistry.serverIP
        val serverPort = WebRegistry.serverPort
        val account = GlobalRegistry.account
        val passwd = GlobalRegistry.passwd
        GlobalScope.launch {
            val loginSuccess = WebConnect.logIn(serverIP, serverPort, account, passwd)
            //登录成功
            if (loginSuccess != null) {
                //设置重连次数和重连监听
                lostConToTry = WebRegistry.lostConRetryTime
                lostRetry=true
                GlobalRegistry.eventChannel.addListenerAlways("connect_lost", ConLostEvent::class) {
                    val e=it as ConLostEvent
                    if(e.account==GlobalRegistry.logAccount) {//是当前账号
                        GlobalRegistry.setLogStatus(GlobalRegistry.LogStatus.logout)
                        if(lostRetry)//重连
                            reConnect()
                        else {//不重连
                            logOut()
                            Log.d(TAG, "logOut finish for ${e.account} (without connect Retry).")
                        }
                    }
                    else{
                        Log.d(TAG,"unknown connect lost ： ${e.account}")
                    }
                }
                //设置用户信息
                WebConnect.wsConnect = loginSuccess.wsConnect
                GlobalRegistry.setUser(loginSuccess.account)
                GlobalRegistry.setLogStatus(GlobalRegistry.LogStatus.logined)
                //同步用户信息
                sendUserInfo()
                queryAllUsersInfo()
                //报告
                Log.d(TAG, "$account 登录成功")
                AppUtils.toast("$account 登录成功")
            }
            //登录失败
            else {
                GlobalRegistry.setLogStatus(GlobalRegistry.LogStatus.logout)
                //报告
                Log.d(TAG, "$account 登录失败")
                AppUtils.toast("$account 登录失败")
                if(retryNowIfLost)//若正在进行重连
                    reConnect()
            }
        }
    }

    fun logOut() {
        //设置重连
        lostRetry=false

        //登出状态
        val statusBefore = GlobalRegistry.logStatus()
        GlobalRegistry.setLogStatus(GlobalRegistry.LogStatus.logout)
        //关闭wsConnect
        val connect = WebConnect.wsConnect
        if (!connect.isClosed) {
            connect.close()
            if (statusBefore == GlobalRegistry.LogStatus.logined)
                AppUtils.toast("${GlobalRegistry.user().id}登出！")
            Log.d(TAG, "logOut for ${GlobalRegistry.user().id}")
        }
    }

    private fun reConnect(){
        if (lostConToTry > 0) {//重连，还有剩余次数
            lostConToTry--
            AppUtils.toast("${GlobalRegistry.account} 重连中(剩余$lostConToTry 次)...")

            Log.d(
                "${TAG}:ConLostRetry:",
                "$lostConToTry of ${WebRegistry.lostConRetryTime} try"
            )
            logIn(retryNowIfLost = true)
        } else  {//重连失败,超过次数
            lostConToTry = 0
            AppUtils.toast("${GlobalRegistry.account}重连失败（${WebRegistry.lostConRetryTime}次）")

            Log.d(
                "${TAG}:ConLostRetry:",
                "logOut finish for ${GlobalRegistry.account} with retry all times"
            )
            logOut()
        }
    }


    fun addNewUser(id: String) = userList.addNewUser(id)
    fun getUser(id: String) = userList.getUser(id)
    fun updateUser(id: String) = userList.updateUser(id)
    fun sendUserInfo() = userList.sendInfo(GlobalRegistry.user())
    fun queryUserInfo(id: String) = userList.queryInfo(id)
    fun queryAllUsersInfo() = userList.queryInfo()
    val sysUser = userList.sysUser
    val visitor = userList.visitor
    fun getUsers(): ArrayList<User> = ArrayList<User>(userList.values())
    fun getDialogs(): ArrayList<Dialog> = ArrayList<Dialog>(dialogList.values)
    fun usersList():UserList= userList
    fun db() =
        DatabaseHelper(VsperApplication.context, "vsper.db", dataBaseVersion)

}