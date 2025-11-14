package com.flatcode.littlebooksadmin.Unit

import com.flatcode.littlebooksadmin.Activity.*
import com.flatcode.littlebooksadmin.Auth.ForgetPasswordActivity
import com.flatcode.littlebooksadmin.Auth.LoginActivity
import com.flatcode.littlebooksadmin.Activity.FollowingActivity
import com.flatcode.littlebooksadmin.Activity.MyBooksActivity
import com.flatcode.littlebooksadmin.Activity.PrivacyPolicyEditActivity
import com.flatcode.littlebooksadmin.Activity.SliderShowActivity
import com.flatcode.littlebooksadmin.Activity.AdsInfoActivity
import com.flatcode.littlebooksadmin.Activity.AllBooksActivity
import com.flatcode.littlebooksadmin.Activity.BookAddActivity
import com.flatcode.littlebooksadmin.Activity.EditorsChoiceAddActivity
import com.flatcode.littlebooksadmin.Activity.SplashActivity

object CLASS {
    var MAIN: Class<*> = MainActivity::class.java
    var SPLASH: Class<*> = SplashActivity::class.java
    var LOGIN: Class<*> = LoginActivity::class.java
    var USERS: Class<*> = UsersActivity::class.java
    var CATEGORY_ADD: Class<*> = CategoryAddActivity::class.java
    var CATEGORY_EDIT: Class<*> = CategoryEditActivity::class.java
    var CATEGORY_BOOKS: Class<*> = BooksCategoryActivity::class.java
    var BOOK_ADD: Class<*> = BookAddActivity::class.java
    var BOOK_EDIT: Class<*> = BookEditActivity::class.java
    var BOOK_DETAIL: Class<*> = BookDetailsActivity::class.java
    var BOOK_VIEW: Class<*> = BookViewActivity::class.java
    var PROFILE: Class<*> = ProfileActivity::class.java
    var PROFILE_EDIT: Class<*> = ProfileEditActivity::class.java
    var PROFILE_INFO: Class<*> = ProfileInfoActivity::class.java
    var FOLLOWERS: Class<*> = FollowersActivity::class.java
    var FOLLOWING: Class<*> = FollowingActivity::class.java
    var MY_BOOKS: Class<*> = MyBooksActivity::class.java
    var FAVORITES: Class<*> = FavoritesActivity::class.java
    var PRIVACY_POLICY: Class<*> = PrivacyPolicyActivity::class.java
    var PRIVACY_POLICY_EDIT: Class<*> = PrivacyPolicyEditActivity::class.java
    var FORGET_PASSWORD: Class<*> = ForgetPasswordActivity::class.java
    var SLIDER_SHOW: Class<*> = SliderShowActivity::class.java
    var ALL_BOOKS: Class<*> = AllBooksActivity::class.java
    var ADS: Class<*> = ADsActivity::class.java
    var ADS_INFO: Class<*> = AdsInfoActivity::class.java
    var TOP_PUBLISHERS: Class<*> = TopPublishersActivity::class.java
    var EDITORS_CHOICE: Class<*> = EditorsChoiceActivity::class.java
    var EDITORS_CHOICE_ADD: Class<*> = EditorsChoiceAddActivity::class.java
    var CATEGORIES: Class<*> = CategoriesActivity::class.java
}