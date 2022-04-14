package vsper.webConnect

import vsper.Config.defaultServerIP
import vsper.Config.defaultServerPort

object WebRegistry {

    var serverIP: String = defaultServerIP
    var serverPort: String = defaultServerPort
    val USER_AGENT: String = "Mozilla/5.0"
    val HEADER_SET_COOKIE: String = "Set-Cookie"
    val HEADER_COOKIE: String = "Cookie"
    var lostConRetryTime = 5

}