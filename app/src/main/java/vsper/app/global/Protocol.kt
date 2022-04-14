package vsper.app.global

import org.json.JSONArray
import org.json.JSONObject

/*what*/object Protocol {

    object TYPE {
        const val COMING = "coming"
        const val LEAVING = "leaving"
        const val USERINFO = "userinfo"
        const val DIALOG = "dialog"
        const val TEXT = "text"
        const val FROM = "from"
        const val TO = "TO"
        const val USERQUERY = "userquery"
        val MSGTYPES = arrayOf(TEXT)
        val USRTYPES = arrayOf(USERINFO, USERQUERY)
    }

    object FILED {
        const val TYPE = "type"
        const val FROM = "from"
        const val TO = "to"
        const val DIALOG = "dialog"
        const val DETAIL = "detail"
    }

    object INFO {
        const val NONEDIALOG = "#noneDialog"
    }

    fun msg(
        to: ArrayList<String>? = null,
        type: String,
        dialog: String = INFO.NONEDIALOG,
        detail: String = ""
    ): String {
        val json = JSONObject()
        if (to == null)
            json.put(Protocol.FILED.TO, JSONArray(ArrayList<String>()))
        else
            json.put(Protocol.FILED.TO, to)

        return JSONObject().apply {
            put(Protocol.FILED.DIALOG, dialog)
            put(Protocol.FILED.TYPE, type)
            put(Protocol.FILED.DETAIL, detail)
        }.toString()
    }

}