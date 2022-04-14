package vsper.app.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import vsper.app.chat.dialog.DialogDbUtil
import vsper.app.chat.message.MessageDbUtil
import vsper.app.chat.user.UserDbUtil

class DatabaseHelper(val context: Context, name: String, version: Int) :
    SQLiteOpenHelper(context, name, null, version) {

    private fun deleteTable(tableId: String) =
        "drop table $tableId"

    private fun cleanTable(tableId: String) =
        "delete from table $tableId"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(UserDbUtil.createTable)
        db.execSQL(DialogDbUtil.createTable)
        db.execSQL(MessageDbUtil.createTable)
//        AppUtils.toast("DataBase: Create tables succeeded.")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        var nowVersion = oldVersion
        while (nowVersion != newVersion) {
            when (nowVersion) {
                0 -> {
                    db.execSQL(UserDbUtil.createTable)
                }
                1 -> {
                    db.execSQL(DialogDbUtil.createTable)
                    db.execSQL(MessageDbUtil.createTable)
                }
                2 -> {
                    db.execSQL(deleteTable(MessageDbUtil.tableId))
                    db.execSQL(MessageDbUtil.createTable)
                }
            }
            nowVersion++
        }
    }
}