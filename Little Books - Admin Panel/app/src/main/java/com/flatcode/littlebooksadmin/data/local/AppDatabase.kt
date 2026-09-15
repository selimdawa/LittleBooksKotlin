package com.flatcode.littlebooksadmin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flatcode.littlebooksadmin.data.model.Book
import com.flatcode.littlebooksadmin.data.model.Category
import com.flatcode.littlebooksadmin.data.model.Comment
import com.flatcode.littlebooksadmin.data.model.User

@Database(
    entities = [Book::class, Category::class, User::class, Comment::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun categoryDao(): CategoryDao
    abstract fun userDao(): UserDao
    abstract fun commentDao(): CommentDao
}
