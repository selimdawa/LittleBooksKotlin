package com.flatcode.littlebooksadmin.Unit

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
    var VERSION = "version"
    var PRIVACY_POLICY = "privacyPolicy"
    var CURRENT_VERSION = 1
    var BOOKS_COUNT = "booksCount"
    var EMAIL = "email"
    var BASIC = "basic"
    var AD_CLICK = "adClick"
    var AD_LOAD = "adLoad"
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
    var ALL = "all"
    var USER = "user"
    var DOT = "."
    var ZERO = 0
    var SPLASH_TIME = 2000
    var ITEM_DOUBLE = 20
    var MAX_BYTES_PDF = 50000000 // Here Max Size PDF 50MB
    var ORDER_MAIN = 10 // Here Max Size PDF 50MB
    var MIX_SQUARE = 500
    var MIX_SLIDER_X = 680
    var MIX_SLIDER_Y = 360
    var MIX_BOOK_X = 400
    var MIX_BOOK_Y = 560
    var searchStatus = false

    //Shared
    var PROFILE_ID = "profileId"
    var COLOR_OPTION = "color_option"
    var BOOK_ID = "bookId"
    var EDITORS_CHOICE_ID = "editorsChoiceId"
    var CATEGORY_ID = "categoryId"
    var OLD_BOOK_ID = "oldBookId"
    var CATEGORY_NAME = "categoryName"

    //ADs
    var AD_S = "ADs"
    var AD_LOADED = "AdLoaded"
    var AD_CLICKED = "AdClicked"

    //Other
    val AUTH = FirebaseAuth.getInstance()
    val FIREBASE_USER = AUTH.currentUser
    val FirebaseUserUid = FIREBASE_USER!!.uid
}