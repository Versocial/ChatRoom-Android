package vsper.app.chat.user

import android.annotation.SuppressLint
import android.content.ContentValues
import android.util.Log
import vsper.app.global.Core

internal object UserDbUtil {
    private const val TAG = "UserDbUtil"
    private const val type_ = "type";
    private const val detail_ = "detail"
    private const val userId_ = "userId"
    private const val dbId_ = "dbId"
    private const val tableId = "user"
    const val createTable =
        "create table $tableId (" +
                " $dbId_ integer primary key autoincrement," +
                " $userId_ text," +
                " $type_ text," +
                " $detail_ text );"

    @SuppressLint("Range")
    fun initList(users: HashMap<String, User>) {
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
                val id = cursor.getString(cursor.getColumnIndex(userId_))
                val detail = cursor.getString(cursor.getColumnIndex(detail_))
                users[id] = User.from_Db(type, id, detail)
            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    @SuppressLint("Recycle")
    fun has(user: User): Boolean {
        val db = Core.db().readableDatabase
        val cursor = db.rawQuery("select * from $tableId where $userId_ = ?", arrayOf(user.id));
        if (cursor.count > 1) {
            Log.d(TAG, "has ${cursor.count} user ${user.id}")
        }
        return cursor.count != 0
    }

    fun save(user: User) {
        if (has(user))
            return
        val db = Core.db().writableDatabase

        val values = ContentValues()
        values.apply {
            put(userId_, user.id)
            put(type_, user.type)
            put(detail_, user.detail_Db())
        }
        db.insert(tableId, null, values)
    }

    fun update(user: User) {
        if (!has(user)) {
            Log.d(TAG, "no user ${user.id}")
            return
        }
        val db = Core.db().writableDatabase
        val values = ContentValues()
        values.apply {
            put(type_, user.type)
            put(detail_, user.detail_Db())
        }
        db.update(tableId, values, "$userId_=?", arrayOf(user.id))
    }
}