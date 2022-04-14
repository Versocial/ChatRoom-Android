package vsper.app.holders.message

import android.net.Uri
import android.view.View
import com.squareup.picasso.Picasso
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingTextMessageViewHolder
import com.stfalcon.chatkit.utils.ShapeImageView
import vsper.app.R
import vsper.app.chat.message.Message

class CustomOutcomingTextMessageViewHolder(itemView: View, payload: Any?) :
    OutcomingTextMessageViewHolder<Message>(itemView, payload) {
    private val userAvatar: ShapeImageView =
        itemView.findViewById(R.id.msgUserAvatar) as ShapeImageView

    override fun onBind(message: Message) {
        super.onBind(message)
        val uri = Uri.parse(message.user.avatar)
        Picasso.get().load(uri).into(
            userAvatar
        )
//        time.text = message.status + " " + time.text
    }
}