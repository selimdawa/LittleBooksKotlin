package com.flatcode.littlebooks.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.viewbinding.ViewBinding
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.DialogAboutAppBinding
import com.flatcode.littlebooks.databinding.DialogCloseAppBinding
import com.flatcode.littlebooks.databinding.DialogLogoutBinding
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.ui.auth.AuthActivity
import com.flatcode.littlebooks.ui.book.BookEditActivity
import com.google.firebase.auth.FirebaseAuth

private fun Context.showCustomDialog(binding: ViewBinding, setup: (Dialog) -> Unit) {
    Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        setCancelable(true)
        window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        val lp = WindowManager.LayoutParams().apply {
            copyFrom(window?.attributes)
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        setup(this)
        show()
        window?.attributes = lp
    }
}

fun Context.closeApp(a: Activity?) {
    val binding = DialogCloseAppBinding.inflate(LayoutInflater.from(this))
    showCustomDialog(binding) { dialog ->
        binding.yes.setOnClickListener { a?.finish() }
        binding.no.setOnClickListener { dialog.cancel() }
    }
}

fun Context.dialogLogout() {
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    showCustomDialog(binding) { dialog ->
        binding.yes.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            openActivity<AuthActivity>(true)
        }
        binding.no.setOnClickListener { dialog.cancel() }
    }
}

fun Context.dialogAboutApp() {
    val binding = DialogAboutAppBinding.inflate(LayoutInflater.from(this))
    showCustomDialog(binding) {
        binding.website.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, DATA.WEB_SITE.toUri()))
        }
        binding.facebook.setOnClickListener {
            val fbUri = try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                "fb://profile/${DATA.FB_ID}".toUri()
            } catch (_: Exception) {
                "https://www.facebook.com/${DATA.FB_ID}".toUri()
            }
            startActivity(Intent(Intent.ACTION_VIEW, fbUri))
        }
    }
}

fun Context.moreOptionDialog(item: Book?) {
    item ?: return
    val options = arrayOf("Edit", "Delete")
    AlertDialog.Builder(this).setTitle("Choose Options").setItems(options) { _, which ->
        if (which == 0) openActivity<BookEditActivity>(false, DATA.BOOK_ID to item.id)
        else if (which == 1) dialogOptionDelete(item.publisher, item.id, item.title!!)
    }.show()
}

fun Context.dialogOptionDelete(
    publisher: String?, bookId: String?, bookTitle: String,
) {
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    showCustomDialog(binding) { dialog ->
        binding.title.setText(R.string.do_you_want_to_delete_the_book)
        binding.yes.setOnClickListener {
            deleteBook(dialog, publisher, bookId, bookTitle)
        }
        binding.no.setOnClickListener { dialog.dismiss() }
    }
}