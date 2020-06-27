package com.example.filemanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CursorFactory
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

/**
 * @name: DBHelper
 * @date: 2020-05-18 10:19
 * @comment: 处理数据库操作类
 */
class DBHelper(context: Context?, name: String?, factory: CursorFactory?, version: Int) : SQLiteOpenHelper(context, name, factory, version) {
    private val TAG = "DBHelper"

    //用户表
/*表名*/
    private val TABLE_NAME_USER = "_user"

    /*id字段*/
    private val VALUE_ID = "_id"
    private val VALUE_NAME = "name"
    private val VALUE_PWD = "pwd"

    /*创建表语句 语句对大小写不敏感 create table 表名(字段名 类型，字段名 类型，…)*/
    private val CREATE_USER = "create table " + TABLE_NAME_USER + "(" +
            VALUE_ID + " integer primary key," +
            VALUE_NAME + " text ," +
            VALUE_PWD + " text" +
            ")"

    constructor(context: Context?) : this(context, "notebook.db", null, 1) {
        Log.e(TAG, "-------> MySqliteHelper")
    }

    override fun onCreate(db: SQLiteDatabase) { //创建表
        db.execSQL(CREATE_USER)
        Log.e(TAG, "-------> onCreate")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        Log.e(TAG, "-------> onUpgrade  oldVersion = $oldVersion   newVersion = $newVersion")
    }

    /**
     * 注册
     *
     * @param pwd
     * @param phone
     * @return
     */
    fun register(pwd: String, phone: String): Boolean {
        val cursor = writableDatabase.query(TABLE_NAME_USER,
                null, "$VALUE_NAME=? and $VALUE_PWD=?", arrayOf(phone, pwd), null, null, null)
        if (cursor.count > 0) {
            cursor.close()
            return false
        }
        cursor.close()
        val values = ContentValues()
        values.put(VALUE_PWD, pwd)
        values.put(VALUE_NAME, phone)
        //添加数据到数据库
        val index = writableDatabase.insert(TABLE_NAME_USER, null, values)
        writableDatabase.close()
        return if (index != -1L) {
            true
        } else {
            false
        }
    }

    /**
     * 登录
     *
     * @param name
     * @param pwd
     * @return
     */
    fun login(name: String?, pwd: String?): User? {
        val cursor = writableDatabase.query(TABLE_NAME_USER,
                null, "$VALUE_NAME=? and $VALUE_PWD=?", arrayOf(name, pwd), null, null, null)
        if (cursor.count == 0) {
            cursor.close()
            return null
        }
        cursor.moveToFirst()
        val user = User()
        user.id = cursor.getInt(cursor.getColumnIndex(VALUE_ID))
        user.name = cursor.getString(cursor.getColumnIndex(VALUE_NAME))
        user.pwd = cursor.getString(cursor.getColumnIndex(VALUE_PWD))
        cursor.close()
        writableDatabase.close()
        return user
    }

    fun updateUser(name: String?, pwd: String?, id: Int): Boolean {
        val values = ContentValues()
        values.put(VALUE_NAME, name)
        values.put(VALUE_PWD, pwd)
        //修改model的数据
        val index = writableDatabase.update(TABLE_NAME_USER, values, "$VALUE_ID=?", arrayOf("" + id)).toLong()
        writableDatabase.close()
        return if (index != -1L) {
            true
        } else {
            false
        }
    }

    init {
        Log.e(TAG, "-------> MySqliteHelper")
    }
}