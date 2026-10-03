package com.flatcode.littlebooks.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.flatcode.littlebooks.model.ADs
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.model.Category
import com.flatcode.littlebooks.model.Comment
import com.flatcode.littlebooks.model.FavoriteEntity
import com.flatcode.littlebooks.model.InterestedEntity
import com.flatcode.littlebooks.model.Setting
import com.flatcode.littlebooks.model.SliderEntity
import com.flatcode.littlebooks.model.User

@Database(
    entities = [Book::class, User::class, Category::class, Comment::class, ADs::class, Setting::class, FavoriteEntity::class, InterestedEntity::class, SliderEntity::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
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
    abstract fun favoriteDao(): FavoriteDao
    abstract fun interestedDao(): InterestedDao
    abstract fun sliderDao(): SliderDao

    companion object {
        const val DATABASE_NAME = "little_books_db"
    }
}