package vsper.app.holders.message

import android.view.View
import android.widget.TextView
import com.stfalcon.chatkit.messages.MessageHolders.IncomingTextMessageViewHolder
import vsper.app.R
import vsper.app.chat.message.Message

class CustomIncomingTextMessageViewHolder(itemView: View, payload: Any?) :
    IncomingTextMessageViewHolder<Message>(itemView, payload) {
    private val userName: TextView = itemView.findViewById(R.id.messageUserName)

    override fun onBind(message: Message) {
        super.onBind(message)
        userName.text = message.user.name
        userName.visibility = View.VISIBLE

        //We can set click listener on view from payload
        val payload = if (payload != null) payload as Payload else Payload()
        userAvatar.setOnClickListener {
            if (payload.avatarClickListener != null) {
                payload.avatarClickListener!!.onAvatarClick()
            }
        }


    }

    class Payload {
        var avatarClickListener: OnAvatarClickListener? = null
    }

    interface OnAvatarClickListener {
        fun onAvatarClick()
    }

}