package vsper.eventChannel.event

import vsper.eventChannel.Event

/*what*/class ConFailEvent(val errorType: String, val account: String) : Event() {

}