package com.flatcode.littlebooksadmin.di

import android.content.Context
import androidx.room.Room
import com.flatcode.littlebooksadmin.db.AppDatabase
import com.flatcode.littlebooksadmin.db.BookDao
import com.flatcode.littlebooksadmin.db.CategoryDao
import com.flatcode.littlebooksadmin.db.CommentDao
import com.flatcode.littlebooksadmin.db.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "little_books_database"
        ).fallbackToDestructiveMigration()
         .build()
    }

    @Provides
    @Singleton
    fun provideBookDao(database: AppDatabase): BookDao = database.bookDao()

    @Provides
    @Singleton
    fun provideCategoryDao(database: AppDatabase): CategoryDao = database.categoryDao()

    @Provides
    @Singleton
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    @Singleton
    fun provideCommentDao(database: AppDatabase): CommentDao = database.commentDao()
}


