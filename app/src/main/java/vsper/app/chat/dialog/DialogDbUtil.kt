package vsper.app.chat.dialog

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import vsper.app.global.Core

object DialogDbUtil {
    private const val TAG = "DialogDbUtil"
    private const val type_ = "type";
    private const val detail_ = "detail"
    private const val dialogId_ = "dialogId"
    private const val dbId_ = "dbId"
    private const val tableId = "dialog"
    const val createTable =
        "create table $tableId (" +
                " $dbId_ integer primary key autoincrement," +
                " $dialogId_ text," +
                " $type_ text," +
                " $detail_ text );"

    @SuppressLint("Range")
    fun initList(dialogs: HashMap<String, Dialog>) {
        val db = Core.db().readableDatabase
        val cursor = db.query(
            this.tableId, null,
            null, null,
            null, null,
            null, null
        )
        if (cursor.moveToFirst()) {
            do {// 遍历Cursor对象，取出数据并打印
                val type = cursor.getString(cursor.getColumnIndex(type_))
                val id = cursor.getString(cursor.getColumnIndex(dialogId_))
                val detail = cursor.getString(cursor.getColumnIndex(detail_))
                dialogs[id] = Dialog.from_Db(type, id, detail)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    @SuppressLint("Recycle")
    fun has(dialog: Dialog): Boolean {
        val db = Core.db().readableDatabase
        val cursor = db.rawQuery("select * from $tableId where $dialogId_ = ?", arrayOf(dialog.id));
        if (cursor.count > 1) {
            Log.d(TAG, "has ${cursor.count} dialog ${dialog.id}")
        }
        return cursor.count != 0
    }

    fun save(dialog: Dialog) {
        if (has(dialog))
            return

        val db = Core.db().readableDatabase
        val values = ContentValues()
        values.apply {
            put(dialogId_, dialog.id)
            put(type_, dialog.type)
            put(detail_, dialog.detail_Db())
        }
        db.insert(tableId, null, values)
    }

    fun update(dialog: Dialog) {
        if (has(dialog)) {
            Log.d(TAG, "no dialog ${dialog.id}")
            return
        }
        val db = Core.db().writableDatabase
        val values = ContentValues()
        values.apply {
            put(type_, dialog.type)
            put(detail_, dialog.detail_Db())
        }
        db.update(tableId, values, "${dialogId_}=?", arrayOf(dialog.id))
    }


}