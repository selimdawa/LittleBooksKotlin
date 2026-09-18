package com.flatcode.littlebooks.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.flatcode.littlebooks.model.*

@Database(
    entities = [
        Book::class,
        User::class,
        Category::class,
        Comment::class,
        ADs::class,
        Setting::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun commentDao(): CommentDao
    abstract fun adsDao(): AdsDao
    abstract fun settingDao(): SettingDao

    companion object {
        const val DATABASE_NAME = "little_books_db"
    }
}


