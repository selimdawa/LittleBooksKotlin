@file:Suppress("SpellCheckingInspection")

package com.flatcode.littlebooksadmin.utils

import com.google.firebase.auth.FirebaseAuth

object DATA {
    //Database
    var USERS = "Users"
    var TOOLS = "Tools"
    var CATEGORIES = "Categories"
    var BOOKS = "Books"
    var FOLLOW = "Follow"
    var FOLLOWERS = "followers"
    var FOLLOWING = "following"
    var COMMENTS = "Comments"
    var LOVES = "Loves"
    var PRIVACY_POLICY = "privacyPolicy"
    var BOOKS_COUNT = "booksCount"
    var BASIC = "basic"
    var AD_CLICK = "adClick"
    var AD_LOAD = "adLoad"
    var USER_NAME = "username"
    var PROFILE_IMAGE = "profileImage"
    var EMPTY = ""
    var TIMESTAMP = "timestamp"
    var IMAGE = "image"
    var SLIDER_SHOW = "SliderShow"
    var PUBLISHER = "publisher"
    var CATEGORY = "category"
    var TITLE = "title"
    var NULL = "null"
    var FAVORITES = "Favorites"
    var VIEWS_COUNT = "viewsCount"
    var DOWNLOADS_COUNT = "downloadsCount"
    var LOVES_COUNT = "lovesCount"
    var EDITORS_CHOICE = "editorsChoice"
    var NAME = "name"
    var ALL = "all"
    var USER = "user"
    var ZERO = 0
    var MIX_SQUARE = 500
    var MIX_SLIDER_X = 680
    var MIX_SLIDER_Y = 360
    var searchStatus = false

    //Shared
    var PROFILE_ID = "profileId"
    var BOOK_ID = "bookId"
    var EDITORS_CHOICE_ID = "editorsChoiceId"
    var CATEGORY_ID = "categoryId"
    var OLD_BOOK_ID = "oldBookId"
    var CATEGORY_NAME = "categoryName"

    //ADs
    var AD_S = "ADs"

    //Other
    val AUTH = FirebaseAuth.getInstance()
    val FIREBASE_USER = AUTH.currentUser
    val FirebaseUserUid = FIREBASE_USER!!.uid

    //Cloudinary
    const val CLOUDINARY_CLOUD_NAME = "j8jsphcf"
    const val CLOUDINARY_UPLOAD_PRESET = "flat_code"
}
