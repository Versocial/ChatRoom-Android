package vsper.importTest

import org.json.JSONArray
import org.json.JSONObject

/*what*/class test {

    fun testJson() {
        var arrayList = ArrayList<String>().apply {
            add("ncie")
            add("cool")
        }
        print(JSONArray().toString())
        print(JSONObject().apply { put("string", arrayList) }.toString())
    }
}