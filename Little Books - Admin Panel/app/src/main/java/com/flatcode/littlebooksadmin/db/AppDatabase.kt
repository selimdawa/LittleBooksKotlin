package com.flatcode.littlebooksadmin.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.model.Comment
import com.flatcode.littlebooksadmin.model.User

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


