package vsper.eventChannel.event

import vsper.app.chat.message.Message
import vsper.eventChannel.Event

/*what*/class ToShowMsgEvent(val message: Message, val dialogId: String) : Event() {
}