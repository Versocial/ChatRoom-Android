package vsper.app.holders.message

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders.IncomingImageMessageViewHolder
import vsper.app.R
import vsper.app.chat.message.Message

/*
* Created by troy379 on 05.04.17.
*/
class CustomIncomingImageMessageViewHolder(itemView: View, payload: Any?) :
    IncomingImageMessageViewHolder<Message>(itemView, payload) {
    private val onlineIndicator: View
    override fun onBind(message: Message) {
        super.onBind(message)
//        val isOnline = message.user.isOnline
//        if (isOnline) {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_online)
//        } else {
//            onlineIndicator.setBackgroundResource(R.drawable.shape_bubble_offline)
//        }
    }

    init {
        onlineIndicator = itemView.findViewById(R.id.onlineIndicator)
    }
}