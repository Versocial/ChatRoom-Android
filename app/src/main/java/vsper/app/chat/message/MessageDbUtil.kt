package vsper.app.chat.message

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import vsper.app.chat.message.implementation.TextMsg
import vsper.app.global.Core
import java.util.*

object MessageDbUtil {
    private const val TAG = "MsgDbUtil"
    const val tableId = "message"
    private const val type_ = "type";
    private const val detail_ = "detail"
    private const val msgId_ = "msgId"
    private const val dbId_ = "dbId"
    private const val dialogId_ = "dialogId"
    private const val userId_ = "userId"
    private const val date_ = "date"
    const val createTable =
        "create table $tableId (" +
                " $dbId_ integer primary key autoincrement," +
                " $msgId_ text," +
                " $dialogId_ text," +
                " $userId_ text," +
                " $date_ integer," +
                " $type_ text," +
                " $detail_ text );"

    fun save(message: Message) {
        val db = Core.db().writableDatabase
        val values = ContentValues()
        values.apply {
            put(msgId_, message.id)
            put(userId_, message.user.id)
            put(dialogId_, message.dialogId)
            put(date_, message.createdAt.time)
            put(type_, message.type())
            put(detail_, message.detail_Db())
        }
        db.insert(tableId, null, values)

    }

    /**
     * @param dialogId query messages with dialog id
     * @param date query messages before date
     * @param num query messages at most num messages
     * @return messages sorted in created date
     */
    fun queryMsg(dialogId: String, before: Date, num: Int): ArrayList<Message> {
        val db = Core.db().readableDatabase
        val cursor = db.query(
            tableId, null,
            "$dialogId_ like ? and $date_ < ?",
            arrayOf(dialogId, before.time.toString()),
            null, null,
            "$date_ desc", "0,$num"
        )
        val msgList = getAllQueryMessages(cursor)
        cursor.close()
        return msgList
    }

    fun queryMsg(msgId: String): Message? {
        val db = Core.db().readableDatabase
        val cursor = db.query(
            tableId, null,
            "$msgId_ like ?",
            arrayOf(msgId),
            null, null, null
        )
        val msgList = getAllQueryMessages(cursor)
        cursor.close()
        if (msgList.size != 1) {
            Log.d("$TAG query Message exception: ", "query msgs length ${msgList.size}")
            return null
        } else
            return msgList[0]
    }

    fun queryLatestMsg(dialogId: String, before: Date): Message? {
        val msgList = queryMsg(dialogId, before, 1)
        if (msgList.size == 0)
            return null
        else
            return msgList[0]
    }


    @SuppressLint("Range")
    private fun getAllQueryMessages(cursor: Cursor): ArrayList<Message> {
        val msgList = ArrayList<Message>()
        if (cursor.moveToFirst()) {
            do {// 遍历Cursor对象，取出数据并打印
                val type = cursor.getString(cursor.getColumnIndex(type_))
                val id = cursor.getString(cursor.getColumnIndex(msgId_))
                val user = cursor.getString(cursor.getColumnIndex(userId_))
                val date: Date = Date(cursor.getLong(cursor.getColumnIndex(date_)))
                val detail = cursor.getString(cursor.getColumnIndex(detail_))
                val dialogId = cursor.getString(cursor.getColumnIndex(dialogId_))
                when (type) {
                    TextMsg.type -> msgList.add(
                        TextMsg.from_Db(
                            id,
                            user,
                            dialogId,
                            date,
                            detail
                        )
                    )
                }
            } while (cursor.moveToNext())
        }
        return msgList
    }

}