package vsper.app.holders.message

import android.view.View
import com.stfalcon.chatkit.messages.MessageHolders.OutcomingImageMessageViewHolder
import vsper.app.chat.message.Message

/*
* Created by troy379 on 05.04.17.
*/
class CustomOutcomingImageMessageViewHolder(itemView: View?, payload: Any?) :
    OutcomingImageMessageViewHolder<Message>(itemView, payload) {
    override fun onBind(message: Message) {
        super.onBind(message)
        time.text = " " + time.text
    }

    //Override this method to have ability to pass custom data in ImageLoader for loading image(not avatar).
    override fun getPayloadForImageLoader(message: Message): Any {
        //For example you can pass size of placeholder before loading
        return Pair(100, 100)
    }
}