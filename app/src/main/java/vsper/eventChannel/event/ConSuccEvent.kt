package vsper.eventChannel.event

import vsper.eventChannel.Event
import vsper.webConnect.WsConnect

/*what*/class ConSuccEvent(val cookie: String, val wsConnect: WsConnect, val account: String) :
    Event() {

}