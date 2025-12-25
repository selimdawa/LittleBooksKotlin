package com.flatcode.littlebooks.Unit

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
    var NULL = "null"
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
    var DOT = "."
    var CURRENT_VERSION = 1
    var ZERO = 0
    var SPLASH_TIME = 2000
    var ITEM_DOUBLE = 20
    var MAX_BYTES_PDF = 50000000 // Here Max Size PDF 50MB
    var ORDER_MAIN = 2 // Here Max Item Show
    var MIN_SQUARE = 500
    var searchStatus = false

    //Shared
    var SHOW_MORE_TYPE = "showMoreType"
    var PROFILE_ID = "profileId"
    var SHOW_MORE_NAME = "showMoreName"
    var SHOW_MORE_BOOLEAN = "showMoreBoolean"
    var COLOR_OPTION = "color_option"
    var BOOK_ID = "bookId"
    var CATEGORY_ID = "categoryId"
    var CATEGORY_NAME = "categoryName"

    //Other
    val AUTH = FirebaseAuth.getInstance()
    val FIREBASE_USER = AUTH.currentUser
    val FirebaseUserUid = FIREBASE_USER!!.uid
    const val WEB_SITE = ""
    const val FB_ID = ""

    //ADs
    var AD_S = "ADs"
    var BANNER_SMART_HOME = "BannerSmartHome"
    var BANNER_SMART_HOME_2 = "BannerSmartHome2"
    var INTERSTITIAL_MAIN = "InterstitialMain"
    var BANNER_SMART_FOLLOWERS_BOOKS = "BannerSmartFollowersBooks"
    var BANNER_SMART_CATEGORY_BOOKS = "BannerSmartCategoryBooks"
    var BANNER_SMART_EXPLORE_PUBLISHERS = "BannerSmartExplorePublishers"
    var BANNER_SMART_FAVORITES = "BannerSmartFavorites"
    var BANNER_SMART_FOLLOWERS = "BannerSmartFollowers"
    var BANNER_SMART_FOLLOWING = "BannerSmartFollowing"
    var BANNER_SMART_MORE_BOOKS = "BannerSmartMoreBooks"
    var BANNER_SMART_MY_BOOKS = "BannerSmartMyBooks"
    var BANNER_SMART_PUBLISHERS_BOOKS = "BannerSmartPublishersBooks"
}