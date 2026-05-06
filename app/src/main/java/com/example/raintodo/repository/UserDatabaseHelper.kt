// UserDatabaseHelper.kt
package com.example.raintodo.repository

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import java.security.MessageDigest
import java.security.SecureRandom

class UserDatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "user.db"
        private const val DATABASE_VERSION = 4

        // 用户表
        private const val TABLE_USERS = "users"
        private const val COL_ID = "id"
        private const val COL_USERNAME = "username"
        private const val COL_PASSWORD_HASH = "password_hash"
        private const val COL_SALT = "salt"
        private const val COL_CREATED_AT = "created_at"

        // 主表
        private const val TABLE_TODOS = "todos"
        private const val COL_TODO_ID = "id"
        private const val COL_USER_ID = "user_id"
        private const val COL_TITLE = "title"
        private const val COL_TODO_IS_COMPLETED = "is_completed"
        private const val COL_TODO_CREATED_AT = "created_at"


        // 明细
        private const val TABLE_TODO_ITEMS = "todo_items"
        private const val COL_ITEM_ID = "id"
        private const val COL_TODO_ID_FK = "todo_id"
        private const val COL_CONTENT = "content"
        private const val COL_IS_COMPLETED = "is_completed"
        private const val COL_ITEM_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 用户表
        val createUsersTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USERNAME TEXT NOT NULL UNIQUE,
                $COL_PASSWORD_HASH TEXT NOT NULL,
                $COL_SALT TEXT NOT NULL,
                $COL_CREATED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createUsersTable)

        // 主表
        val createTodosTable = """
            CREATE TABLE $TABLE_TODOS (
                $COL_TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID INTEGER NOT NULL,
                $COL_TITLE TEXT NOT NULL,
                $COL_TODO_IS_COMPLETED INTEGER DEFAULT 0,
                $COL_TODO_CREATED_AT TEXT DEFAULT (datetime('now','localtime')),
                FOREIGN KEY ($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID)
            )
        """.trimIndent()
        db.execSQL(createTodosTable)
        //明细
        val createTodoItemsTable = """
            CREATE TABLE $TABLE_TODO_ITEMS (
                $COL_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_TODO_ID_FK INTEGER NOT NULL,
                $COL_CONTENT TEXT NOT NULL,
                $COL_IS_COMPLETED INTEGER DEFAULT 0,
                $COL_ITEM_CREATED_AT TEXT DEFAULT (datetime('now','localtime')),
                FOREIGN KEY ($COL_TODO_ID_FK) REFERENCES $TABLE_TODOS($COL_TODO_ID)
            )
        """.trimIndent()
        db.execSQL(createTodoItemsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_TODOS (
                    $COL_TODO_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_USER_ID INTEGER NOT NULL,
                    $COL_TITLE TEXT NOT NULL,
                    $COL_TODO_CREATED_AT TEXT DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY ($COL_USER_ID) REFERENCES $TABLE_USERS($COL_ID)
                )
            """.trimIndent())
        }
        if (oldVersion < 3) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_TODO_ITEMS (
                    $COL_ITEM_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                    $COL_TODO_ID_FK INTEGER NOT NULL,
                    $COL_CONTENT TEXT NOT NULL,
                    $COL_IS_COMPLETED INTEGER DEFAULT 0,
                    $COL_ITEM_CREATED_AT TEXT DEFAULT (datetime('now','localtime')),
                    FOREIGN KEY ($COL_TODO_ID_FK) REFERENCES $TABLE_TODOS($COL_TODO_ID)
                )
            """.trimIndent())
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_TODOS ADD COLUMN $COL_TODO_IS_COMPLETED INTEGER DEFAULT 0")
        }
    }

    private fun generateSalt(): String {
        val random = SecureRandom()
        val saltBytes = ByteArray(16)
        random.nextBytes(saltBytes)
        return bytesToHex(saltBytes)
    }

    private fun hashPassword(password: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val saltedPassword = salt + password
        val hashBytes = digest.digest(saltedPassword.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = "0123456789ABCDEF"[v ushr 4]
            hexChars[i * 2 + 1] = "0123456789ABCDEF"[v and 0x0F]
        }
        return String(hexChars)
    }

    fun registerUser(username: String, password: String): Boolean {
        val db = writableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_ID),
            "$COL_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        if (exists) return false

        val salt = generateSalt()
        val passwordHash = hashPassword(password, salt)
        val values = ContentValues().apply {
            put(COL_USERNAME, username)
            put(COL_PASSWORD_HASH, passwordHash)
            put(COL_SALT, salt)
            put(COL_CREATED_AT, System.currentTimeMillis())
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    fun loginUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_PASSWORD_HASH, COL_SALT),
            "$COL_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        if (cursor.moveToFirst()) {
            val storedHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_PASSWORD_HASH))
            val salt = cursor.getString(cursor.getColumnIndexOrThrow(COL_SALT))
            cursor.close()
            val inputHash = hashPassword(password, salt)
            return storedHash == inputHash
        }
        cursor.close()
        return false
    }

    fun isUsernameExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_ID),
            "$COL_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    fun getUserIdByUsername(username: String): Int? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COL_ID),
            "$COL_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )
        var userId: Int? = null
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID))
        }
        cursor.close()
        return userId
    }

    //获取特定用户的待办
    fun getTodosByUserId(userId: Int): List<TodoList> {
        val db = readableDatabase
        val todoLists = mutableListOf<TodoList>()

        val todoCursor = db.query(
            TABLE_TODOS,
            null,
            "$COL_USER_ID = ?",
            arrayOf(userId.toString()),
            null, null,
            "$COL_TODO_CREATED_AT DESC"
        )

        with(todoCursor) {
            while (moveToNext()) {
                val todoId = getInt(getColumnIndexOrThrow(COL_TODO_ID))
                val title = getString(getColumnIndexOrThrow(COL_TITLE))
                val isCompleted = getInt(getColumnIndexOrThrow(COL_TODO_IS_COMPLETED)) == 1
                val createdAt = getString(getColumnIndexOrThrow(COL_TODO_CREATED_AT))

                val items = getTodoItemsByTodoId(todoId)

                todoLists.add(
                    TodoList(
                        id = todoId,
                        userId = userId,
                        title = title,
                        isCompleted = isCompleted,
                        items = items,
                        createdAt = createdAt,
                    )
                )
            }
        }
        todoCursor.close()
        return todoLists
    }

    //获取清单下的所有明细事项
    private fun getTodoItemsByTodoId(todoId: Int): List<TodoItem> {
        val db = readableDatabase
        val items = mutableListOf<TodoItem>()

        val cursor = db.query(
            TABLE_TODO_ITEMS,
            null,
            "$COL_TODO_ID_FK = ?",
            arrayOf(todoId.toString()),
            null, null,
            "$COL_ITEM_CREATED_AT ASC"
        )

        with(cursor) {
            while (moveToNext()) {
                items.add(
                    TodoItem(
                        id = getInt(getColumnIndexOrThrow(COL_ITEM_ID)),
                        todoId = todoId,
                        content = getString(getColumnIndexOrThrow(COL_CONTENT)),
                        isCompleted = getInt(getColumnIndexOrThrow(COL_IS_COMPLETED)) == 1,
                        createdAt = getString(getColumnIndexOrThrow(COL_ITEM_CREATED_AT))
                    )
                )
            }
        }
        cursor.close()
        return items
    }

    //添加待办清单
    fun addTodo(userId: Int, title: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_USER_ID, userId)
            put(COL_TITLE, title)
            put(COL_TODO_IS_COMPLETED, 0)  // 新清单默认未完成
        }
        return db.insert(TABLE_TODOS, null, values)
    }

    //更新清单标题
    fun updateTodoTitle(id: Int, title: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TITLE, title)
        }
        return db.update(TABLE_TODOS, values, "$COL_TODO_ID = ?", arrayOf(id.toString()))
    }

    //删除待办清单（同时删除所有明细）
    fun deleteTodo(id: Int): Int {
        val db = writableDatabase
        db.delete(TABLE_TODO_ITEMS, "$COL_TODO_ID_FK = ?", arrayOf(id.toString()))
        return db.delete(TABLE_TODOS, "$COL_TODO_ID = ?", arrayOf(id.toString()))
    }

    //向清单添加一条明细
    fun addTodoItem(todoId: Int, content: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TODO_ID_FK, todoId)
            put(COL_CONTENT, content)
        }
        val result = db.insert(TABLE_TODO_ITEMS, null, values)

        // 添加新事项后，清单自动变为未完成
        if (result != -1L) {
            updateTodoCompletedStatus(todoId, false)
        }

        return result
    }

    //更新明细事项内容
    fun updateTodoItem(id: Int, content: String): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CONTENT, content)
        }
        return db.update(TABLE_TODO_ITEMS, values, "$COL_ITEM_ID = ?", arrayOf(id.toString()))
    }

    //切换明细事项的完成状态
    fun toggleTodoItemStatus(id: Int, isCompleted: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        val result = db.update(TABLE_TODO_ITEMS, values, "$COL_ITEM_ID = ?", arrayOf(id.toString()))

        // 明细全都完成自动将清单完成
        if (result > 0) {
            val cursor = db.query(
                TABLE_TODO_ITEMS,
                arrayOf(COL_TODO_ID_FK),
                "$COL_ITEM_ID = ?",
                arrayOf(id.toString()),
                null, null, null
            )
            if (cursor.moveToFirst()) {
                val todoId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TODO_ID_FK))
                cursor.close()

                // 检查该清单明细的完成状态
                updateTodoStatusBasedOnItems(todoId)
            } else {
                cursor.close()
            }
        }

        return result
    }

    //更新清单完成昨天
    private fun updateTodoStatusBasedOnItems(todoId: Int) {
        val db = readableDatabase

        // 查询该清单下所有明细的数量
        val totalCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_TODO_ITEMS WHERE $COL_TODO_ID_FK = ?",
            arrayOf(todoId.toString())
        )
        totalCursor.moveToFirst()
        val totalCount = totalCursor.getInt(0)
        totalCursor.close()

        if (totalCount == 0) {
            // 没有明细，清单保持未完成
            updateTodoCompletedStatus(todoId, false)
            return
        }

        // 查询已完成明细的数量
        val completedCursor = db.rawQuery(
            "SELECT COUNT(*) FROM $TABLE_TODO_ITEMS WHERE $COL_TODO_ID_FK = ? AND $COL_IS_COMPLETED = 1",
            arrayOf(todoId.toString())
        )
        completedCursor.moveToFirst()
        val completedCount = completedCursor.getInt(0)
        completedCursor.close()

        // 所有明细都完成，清单自动完成
        val allCompleted = totalCount == completedCount
        updateTodoCompletedStatus(todoId, allCompleted)
    }

    //更新清单的完成状态
    private fun updateTodoCompletedStatus(todoId: Int, isCompleted: Boolean) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TODO_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        db.update(TABLE_TODOS, values, "$COL_TODO_ID = ?", arrayOf(todoId.toString()))
    }

    //删除一条明细事项,并检查清单状态
    fun deleteTodoItem(id: Int): Int {
        val db = writableDatabase
        val cursor = db.query(
            TABLE_TODO_ITEMS,
            arrayOf(COL_TODO_ID_FK),
            "$COL_ITEM_ID = ?",
            arrayOf(id.toString()),
            null, null, null
        )
        var todoId: Int? = null
        if (cursor.moveToFirst()) {
            todoId = cursor.getInt(cursor.getColumnIndexOrThrow(COL_TODO_ID_FK))
        }
        cursor.close()

        val result = db.delete(TABLE_TODO_ITEMS, "$COL_ITEM_ID = ?", arrayOf(id.toString()))

        if (result > 0 && todoId != null) {
            updateTodoStatusBasedOnItems(todoId)
        }

        return result
    }

    //手动切换清单的完成状态
    fun toggleTodoStatus(todoId: Int, isCompleted: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_TODO_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.update(TABLE_TODOS, values, "$COL_TODO_ID = ?", arrayOf(todoId.toString()))
    }

    private fun getCurrentDateTime(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}

