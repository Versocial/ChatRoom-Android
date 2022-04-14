package vsper.eventChannel.event

import vsper.eventChannel.Event


/*what*/class SignInEvent(val success: Boolean, val account: String, val password: String) :
    Event() {
}