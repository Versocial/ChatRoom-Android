package vsper.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import vsper.app.R
import vsper.app.global.GlobalRegistry
import vsper.app.utils.AppUtils
import vsper.eventChannel.event.SignInEvent
import vsper.webConnect.WebConnect
import java.util.*


/*what*/class SiginInFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sign, container, false)
        val account: TextView = view.findViewById(R.id.signInAccount)
        val password: TextView = view.findViewById(R.id.signInPassWord)
        val button: Button = view.findViewById(R.id.signInButton)
        button.setOnClickListener {
            val account_ = account.text.toString()
            val password_ = password.text.toString()
            val now = Date()
            GlobalRegistry.eventChannel.addListenerOnce(
                "signIn${now.time}", SignInEvent::class
            ) {
                val e = it as SignInEvent
                if (e.success) {
                    GlobalRegistry.resetAccount(account_)
                    GlobalRegistry.resetPassWd(password_)
                    AppUtils.toast("$account_ 注册成功！请在设置页面登录。")

                } else {
                    AppUtils.toast("$account_ 注册失败！请检查你的网络连接。")
                }
            }
            WebConnect.signIn(account_, password_)
        }
        return view
    }

}