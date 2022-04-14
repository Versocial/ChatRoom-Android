package vsper.app.global

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import vsper.app.R
import vsper.app.chat.user.User
import vsper.app.debug.Talk
import vsper.eventChannel.EventChannel
import vsper.eventChannel.event.LogStatusChangeEvent

object GlobalRegistry {
    enum class LogStatus {
        logined, logining, logout
    }

    var isDebugOn: Boolean = false
    var account: String = "#username"
    var passwd: String = "#password"
    private const val savingFile: String = "Settings"
    private const val password_ = "password"
    private const val account_ = "account"


    val eventChannel: EventChannel = EventChannel()
    lateinit var editor: SharedPreferences.Editor
    lateinit var globalPrefs: SharedPreferences
    var defaultPrefs: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(VsperApplication.context)

    private var theLogStatus: LogStatus = LogStatus.logout
    private var user: User = Core.visitor

    val logAccount: String
        get() = user.id

    val TALK_LIST: MutableList<Talk> = ArrayList()


    fun resetAccount(account: String) {
        editor.putString(account_, account)
        editor.apply()
        this.account = account
    }

    fun resetPassWd(password: String) {
        editor.putString(password_, password)
        editor.apply()
        passwd = password
    }


    @SuppressLint("CommitPrefEdits")
    fun vsperInitialize() {
        globalPrefs = VsperApplication.context.getSharedPreferences(
            savingFile,
            Context.MODE_PRIVATE
        )
        editor = globalPrefs.edit()
        account = globalPrefs.getString(account_, account)!!
        passwd = globalPrefs.getString(password_, passwd)!!
        Core.initialize()
        eventChannel.start()
    }

    fun user(): User {
        return user
    }


    fun setUser(userId: String) {
        val theUser = Core.getUser(userId)
        if (theUser == null) {
            user = Core.addNewUser(userId)
        } else {
            user = theUser
        }
    }

    fun setLogStatus(newStatus: LogStatus){
        val before= theLogStatus
        theLogStatus=newStatus
        eventChannel.addEvent(LogStatusChangeEvent(before,newStatus))
    }

    fun logStatus()= theLogStatus

    fun msgCopyFree(): Boolean =
        defaultPrefs.getBoolean(VsperApplication.context.getString(R.string.msgFreeCopy), false)

    fun autoUpdate(): Boolean =
        defaultPrefs.getBoolean(VsperApplication.context.getString(R.string.autoUpdate), false)

    fun foreService(): Boolean =
        defaultPrefs.getBoolean(VsperApplication.context.getString(R.string.foreService), false)

    fun autoLogin(): Boolean =
        defaultPrefs.getBoolean(VsperApplication.context.getString(R.string.autoLogin), false)

    fun isNightModSetOn(): Boolean =
        defaultPrefs.getBoolean(VsperApplication.context.getString(R.string.nightMod), false)
}