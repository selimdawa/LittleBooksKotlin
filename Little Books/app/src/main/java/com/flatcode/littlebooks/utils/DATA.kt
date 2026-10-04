@file:Suppress("SpellCheckingInspection")

package com.flatcode.littlebooks.utils

import com.google.firebase.auth.FirebaseAuth

object DATA {
    //Database
    var USERS = "Users"
    var CATEGORIES = "Categories"
    var BOOKS = "Books"
    var TOOLS = "Tools"
    var PRIVACY_POLICY = "privacyPolicy"
    var FOLLOW = "Follow"
    var FOLLOWERS = "followers"
    var FOLLOWING = "following"
    var COMMENTS = "Comments"
    var LOVES = "Loves"
    var VERSION = "version"
    var BOOKS_COUNT = "booksCount"
    var EMAIL = "email"
    var BASIC = "basic"
    var USER_NAME = "username"
    var PROFILE_IMAGE = "profileImage"
    var EMPTY = ""
    var SPACE = " "
    var TIMESTAMP = "timestamp"
    var COMMENT = "comment"
    var URL = "url"
    var ID = "id"
    var IMAGE = "image"
    var SLIDER_SHOW = "SliderShow"
    var PUBLISHER = "publisher"
    var CATEGORY = "category"
    var DESCRIPTION = "description"
    var TITLE = "title"
    var FAVORITES = "Favorites"
    var VIEWS_COUNT = "viewsCount"
    var DOWNLOADS_COUNT = "downloadsCount"
    var LOVES_COUNT = "lovesCount"
    var EDITORS_CHOICE = "editorsChoice"
    var NAME = "name"
    var ADS_LOADED_COUNT = "adsLoadedCount"
    var ADS_CLICKED_COUNT = "adsClickedCount"
    var AD_CLICK = "adClick"
    var AD_LOAD = "adLoad"

    //Others
    var CURRENT_VERSION = 1
    var ORDER_MAIN = 2 // Here Max Item Show
    var MIN_SQUARE = 500
    var searchStatus = false

    //Shared
    var SHOW_MORE_TYPE = "showMoreType"
    var PROFILE_ID = "profileId"
    var SHOW_MORE_NAME = "showMoreName"
    var SHOW_MORE_BOOLEAN = "showMoreBoolean"
    var BOOK_ID = "bookId"
    var CATEGORY_ID = "categoryId"
    var CATEGORY_NAME = "categoryName"

    //Other
    val AUTH: FirebaseAuth get() = FirebaseAuth.getInstance()
    val FIREBASE_USER: com.google.firebase.auth.FirebaseUser? get() = AUTH.currentUser
    val FirebaseUserUid: String get() = FIREBASE_USER?.uid ?: ""
    const val WEB_SITE = ""
    const val FB_ID = ""

    //ADs
    var AD_S = "ADs"
    var BANNER_SMART_HOME = "BannerSmartHome"
    var BANNER_SMART_HOME_2 = "BannerSmartHome2"
    var BANNER_SMART_FOLLOWERS_BOOKS = "BannerSmartFollowersBooks"
    var BANNER_SMART_CATEGORY_BOOKS = "BannerSmartCategoryBooks"
    var BANNER_SMART_EXPLORE_PUBLISHERS = "BannerSmartExplorePublishers"
    var BANNER_SMART_FOLLOWERS = "BannerSmartFollowers"
    var BANNER_SMART_FOLLOWING = "BannerSmartFollowing"
    var BANNER_SMART_MORE_BOOKS = "BannerSmartMoreBooks"
    var BANNER_SMART_MY_BOOKS = "BannerSmartMyBooks"
    var BANNER_SMART_PUBLISHERS_BOOKS = "BannerSmartPublishersBooks"

    //Cloudinary
    const val CLOUDINARY_CLOUD_NAME = "j8jsphcf"
    const val CLOUDINARY_UPLOAD_PRESET = "flat_code"

    //Setting IDs
    const val EDIT_PROFILE = "editProfile"
    const val EXPLORE_PUBLISHERS = "explorePublishers"
    const val FOLLOWERS_ID = "followers"
    const val FOLLOWING_ID = "following"
    const val MY_BOOKS = "myBooks"
    const val ADD_BOOK = "addBook"
    const val FAVORITES_ID = "favorites"
    const val ABOUT_APP = "aboutApp"
    const val LOGOUT = "logout"
    const val SHARE_APP = "shareApp"
    const val RATE_APP = "rateApp"
    const val PRIVACY_POLICY_ID = "privacyPolicy"
}


