package vsper.eventChannel.event

import vsper.app.global.GlobalRegistry
import vsper.eventChannel.Event


/*what*/class LogStatusChangeEvent(val before:GlobalRegistry.LogStatus,val now:GlobalRegistry.LogStatus): Event() {
}