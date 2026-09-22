package com.flatcode.littlebooks.di

import android.content.Context
import androidx.room.Room
import com.flatcode.littlebooks.db.AdsDao
import com.flatcode.littlebooks.db.AppDatabase
import com.flatcode.littlebooks.db.BookDao
import com.flatcode.littlebooks.db.CategoryDao
import com.flatcode.littlebooks.db.CommentDao
import com.flatcode.littlebooks.db.SettingDao
import com.flatcode.littlebooks.db.UserDao
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context, AppDatabase::class.java, AppDatabase.DATABASE_NAME
        ).fallbackToDestructiveMigration(true).build()
    }

    @Provides
    fun provideBookDao(db: AppDatabase): BookDao = db.bookDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()

    @Provides
    fun provideCommentDao(db: AppDatabase): CommentDao = db.commentDao()

    @Provides
    fun provideAdsDao(db: AppDatabase): AdsDao = db.adsDao()

    @Provides
    fun provideSettingDao(db: AppDatabase): SettingDao = db.settingDao()
}