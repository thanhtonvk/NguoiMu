package com.phuyen.nguoikhuyettat.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.phuyen.nguoikhuyettat.models.NguoiThan

class DBContext(context: Context?) :
    SQLiteOpenHelper(context, "database.sqlite", null, 1) {
    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL(
            "create table NguoiThan(" +
                    "Ten nvarchar(50)," +
                    "Anh blob," +
                    "Embedding text)"
        )
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {
    }

    val nguoiThans: List<NguoiThan>
        get() {
            val nguoiThans: MutableList<NguoiThan> =
                ArrayList()
            val database = readableDatabase
            val cursor = database.rawQuery("select * from NguoiThan", null)
            cursor.moveToFirst()
            while (!cursor.isAfterLast) {
                val nguoiThan =
                    NguoiThan(cursor.getString(0), cursor.getBlob(1), cursor.getString(2))
                nguoiThans.add(nguoiThan)
                cursor.moveToNext()
            }
            database.close()
            return nguoiThans
        }

    fun add(nguoiThan: NguoiThan) {
        val database = writableDatabase
        val values = ContentValues()
        values.put("Ten", nguoiThan.ten)
        values.put("Anh", nguoiThan.anh)
        values.put("Embedding", nguoiThan.embedding)
        database.insert("NguoiThan", null, values)
        database.close()
    }

    fun xoa(embedding: String) {
        val database = writableDatabase
        val result = database.delete("NguoiThan", "Embedding = ?", arrayOf(embedding))
        Log.e("TAG Xoa", "xoa: $result")
    }
}