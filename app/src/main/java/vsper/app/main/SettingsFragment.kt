package vsper.app.main

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.coroutineScope
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vsper.app.R
import vsper.app.global.Core
import vsper.app.global.GlobalRegistry
import vsper.app.global.GlobalRegistry.LogStatus
import vsper.app.global.VsperApplication
import vsper.app.update.UpdateUtils
import vsper.app.utils.AppUtils
import vsper.eventChannel.event.LogStatusChangeEvent

class SettingsFragment : PreferenceFragmentCompat() {
    companion object {
        private val TAG = "settingsFragment"
    }

    private lateinit var loginButton: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?): Unit {
        addPreferencesFromResource(R.xml.prefs)
        initServerIpPortInput()
        initAccountInput()
        initPasswordInput()
        initLoginButton()
        initUpdateButton()
        initNightMod()
    }

    private fun initAccountInput() {
        val accountInput: EditTextPreference = textPreference(R.string.accountButton)
        accountInput.title = "account: ${GlobalRegistry.account}"
        accountInput.text = GlobalRegistry.account
        accountInput.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val account: String = newValue.toString()
                GlobalRegistry.resetAccount(account)
                accountInput.title = "account: $account"
                true
            }
    }

    private fun initPasswordInput() {
        val passwordInput: EditTextPreference = textPreference(R.string.passwordButton)
        passwordInput.title = "password: ****"
        passwordInput.text = GlobalRegistry.passwd
        passwordInput.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val password: String = newValue.toString()
                GlobalRegistry.resetPassWd(password)
                true
            }
    }

    private fun initLoginButton() {
        loginButton = preference(R.string.loginButton)
        refreshLogStatus()
        GlobalRegistry.eventChannel.addListenerAlways("listen Log Status",LogStatusChangeEvent::class){
            this.lifecycle.coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    refreshLogStatus()
                }
            }
        }

        loginButton.setOnPreferenceClickListener {
            return@setOnPreferenceClickListener logIn()
        }
    }

    fun refreshLogStatus(){
            loginButton.summary = when (GlobalRegistry.logStatus()) {
                LogStatus.logining -> "正在登录账户 ${GlobalRegistry.account}"
                LogStatus.logined -> "账户 ${GlobalRegistry.logAccount} 已登录"
                LogStatus.logout -> "暂未登录"
                else -> "unkown status"
            }
    }

    private fun initUpdateButton() {
        val update = preference(R.string.updateButton)
        update.summary = "version: ${AppUtils.versionName} code :${AppUtils.versionCode}"
        update.setOnPreferenceClickListener {
            val theActivity = activity
            if (theActivity != null) {
                UpdateUtils.update(theActivity, theActivity)
            } else
                AppUtils.toast("update fail when no activity is called.")
            return@setOnPreferenceClickListener true
        }
    }

    private fun initServerIpPortInput() {
        val serverIP: EditTextPreference = textPreference(R.string.serverIPPortButton)
        serverIP.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                val ipString: String = serverIP.text
                val ipString2: String = newValue.toString()
                serverIP.title = "server IP:port=$ipString2"
                Toast.makeText(
                    VsperApplication.context,
                    "$ipString $ipString2",
                    Toast.LENGTH_SHORT
                ).show()
                true
            }
    }

    private fun logIn(): Boolean {
        when (GlobalRegistry.logStatus()) {
            LogStatus.logout, LogStatus.logined -> {
                if (GlobalRegistry.logStatus() == LogStatus.logined && GlobalRegistry.user().id == GlobalRegistry.account) {
                    AppUtils.toast("宁已经登录啦")
                    return true
                } else if (GlobalRegistry.logStatus() == LogStatus.logined && GlobalRegistry.user().id != GlobalRegistry.account) {
                    AppUtils.toast("切换账户：${GlobalRegistry.user().id}->${GlobalRegistry.account}")
                }
                loginButton.summary = "login ing: ${GlobalRegistry.account}"
                Core.logIn()
            }
            LogStatus.logining -> AppUtils.toast("上次点击的登陆在进行中，请稍候")
        }
        return true
    }

    private fun initNightMod() {
        val nightModeBox = boxPreference(R.string.nightMod)
        val isNightModOn = AppUtils.isDarkTheme(requireContext())
        if (GlobalRegistry.isNightModSetOn() != isNightModOn) {
            nightModeBox.isChecked = isNightModOn
        }
        nightModeBox.setOnPreferenceChangeListener { preference, isChecked ->
            val isNightModOn = AppUtils.isDarkTheme(requireContext())
            Log.d(TAG, "$isNightModOn,$isChecked")
            if (isChecked != isNightModOn) {
                MainActivity.fragmentLoadFirst = SettingsFragment()
                if (isChecked as Boolean)
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                else
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                if (isChecked as Boolean)
                    AppUtils.toast("已经是夜间模式啦")
                else
                    AppUtils.toast("已经是白天模式啦")
            }
            true
        }
    }


    private fun textPreference(RstringInt: Int): EditTextPreference {
        val str = VsperApplication.context.getString(RstringInt)
        return findPreference(str)!!
    }

    private fun preference(RstringInt: Int): Preference {
        val str = VsperApplication.context.getString(RstringInt)
        return findPreference(str)!!
    }

    private fun boxPreference(RstringInt: Int): CheckBoxPreference {
        val str = VsperApplication.context.getString(RstringInt)
        return findPreference(str)!!
    }

    fun autoLogin(): Boolean {
        logIn()
        return true
    }
}