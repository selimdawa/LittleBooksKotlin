package com.flatcode.littlebooksadmin.repository

import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.User
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    private val db: FirebaseDatabase
) {

    suspend fun getUserInfo(userId: String): Resource<User> {
        return try {
            val snapshot = db.getReference(DATA.USERS).child(userId).get().await()
            val user = snapshot.getValue(User::class.java)
            if (user != null) Resource.Success(user) else Resource.Error("User not found")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun getDashboardStats(): Resource<DashboardStats> {
        return try {
            val userId = DATA.FirebaseUserUid

            val usersSnapshot = db.getReference(DATA.USERS).get().await()
            var usersCount = 0
            var publishersCount = 0
            var adsUsersCount = 0
            for (child in usersSnapshot.children) {
                val user = child.getValue(User::class.java)
                if (user?.id != null) {
                    usersCount++
                    if (user.booksCount >= 1) publishersCount++
                    if (user.adLoad != 0 || user.adClick != 0) adsUsersCount++
                }
            }

            val booksSnapshot = db.getReference(DATA.BOOKS).get().await()
            var allBooksCount = 0
            var myBooksCount = 0
            var editorsChoiceCount = 0
            for (child in booksSnapshot.children) {
                val book = child.getValue(Book::class.java)
                if (book?.id != null) {
                    allBooksCount++
                    if (book.publisher == userId) myBooksCount++
                    if (book.editorsChoice != 0) editorsChoiceCount++
                }
            }

            val categoriesSnapshot = db.getReference(DATA.CATEGORIES).get().await()
            var myCategoriesCount = 0
            for (child in categoriesSnapshot.children) {
                val categoryPublisher = child.child(DATA.PUBLISHER).getValue(String::class.java)
                if (categoryPublisher == userId) myCategoriesCount++
            }

            val sliderCount = db.getReference(DATA.SLIDER_SHOW).get().await().childrenCount.toInt()
            val followersCount =
                db.getReference(DATA.FOLLOW).child(userId).child(DATA.FOLLOWERS).get()
                    .await().childrenCount.toInt()
            val followingCount =
                db.getReference(DATA.FOLLOW).child(userId).child(DATA.FOLLOWING).get()
                    .await().childrenCount.toInt()
            val favoritesCount =
                db.getReference(DATA.FAVORITES).child(userId).get().await().childrenCount.toInt()

            Resource.Success(
                DashboardStats(
                    usersCount,
                    publishersCount,
                    myBooksCount,
                    allBooksCount,
                    sliderCount,
                    followersCount,
                    followingCount,
                    favoritesCount,
                    adsUsersCount,
                    editorsChoiceCount,
                    myCategoriesCount
                )
            )
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error")
        }
    }

    data class DashboardStats(
        val users: Int,
        val publishers: Int,
        val myBooks: Int,
        val allBooks: Int,
        val sliderShow: Int,
        val followers: Int,
        val following: Int,
        val favorites: Int,
        val ads: Int,
        val editorsChoice: Int,
        val categories: Int
    )
}