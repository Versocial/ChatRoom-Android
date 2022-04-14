package vsper.app.main

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import vsper.app.R
import vsper.app.global.Core
import vsper.app.global.GlobalRegistry
import vsper.app.utils.AppUtils

class UserInfoFragment : Fragment() {
    lateinit var avatar: CircleImageView
    lateinit var name: TextView

    companion object {
        fun newInstance() = UserInfoFragment()
    }

    private lateinit var viewModel: userInfoModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_userinfo, container, false)
        avatar = view.findViewById(R.id.userinfo_avatar) as CircleImageView
        name = view.findViewById(R.id.userinfo_name) as TextView
        freshUserInfo()
        val nameInput = view.findViewById(R.id.inputName) as EditText
        val avatarInput = view.findViewById(R.id.inputAvatar) as EditText
        val commit = view.findViewById(R.id.commitButton) as Button
        commit.setOnClickListener {
            if (GlobalRegistry.logStatus() != GlobalRegistry.LogStatus.logined) {
                AppUtils.toast("请先登录")
                return@setOnClickListener
            }
            val user = GlobalRegistry.user()
            val nameStr = nameInput.text.toString()
            val avatarStr = avatarInput.text.toString()
            if (nameStr != "") {
                user.name = nameStr
            }
            if (avatarStr != "") {
                user.avatar = avatarStr
            }
            Core.updateUser(user.id)
            freshUserInfo()
        }
        val push = view.findViewById(R.id.pushButton) as Button
        push.setOnClickListener {
            if (GlobalRegistry.logStatus() != GlobalRegistry.LogStatus.logined) {
                AppUtils.toast("请先登录")
                return@setOnClickListener
            }
            Core.sendUserInfo()
        }

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(userInfoModel::class.java)
    }

    private fun freshUserInfo() {
        name.text = GlobalRegistry.user().name
        val uri = Uri.parse(GlobalRegistry.user().avatar)
        Picasso.get().load(uri).into(
            avatar
        )
    }
}