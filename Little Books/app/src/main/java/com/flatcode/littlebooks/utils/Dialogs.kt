package com.flatcode.littlebooks.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.DialogAboutAppBinding
import com.flatcode.littlebooks.databinding.DialogCloseAppBinding
import com.flatcode.littlebooks.databinding.DialogLogoutBinding
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.ui.auth.AuthActivity
import com.flatcode.littlebooks.ui.book.BookEditActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

fun Context.closeApp() {
    val activity = this as? Activity ?: return
    if (activity.isFinishing || activity.isDestroyed) return

    val dialogBinding = DialogCloseAppBinding.inflate(layoutInflater)
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.yes.setOnClickListener {
        activity.finish()
    }

    dialogBinding.no.setOnClickListener {
        alertDialog.dismiss()
    }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Context.dialogLogout() {
    val activity = this as? Activity ?: return
    if (activity.isFinishing || activity.isDestroyed) return

    val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.yes.setOnClickListener {
        FirebaseAuth.getInstance().signOut()
        activity.openActivity<AuthActivity>(true)
        alertDialog.dismiss()
    }

    dialogBinding.no.setOnClickListener {
        alertDialog.dismiss()
    }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Context.dialogAboutApp() {
    val activity = this as? Activity ?: return
    if (activity.isFinishing || activity.isDestroyed) return

    val dialogBinding = DialogAboutAppBinding.inflate(layoutInflater)
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.website.setOnClickListener {
        val intent = Intent(Intent.ACTION_VIEW, DATA.WEB_SITE.toUri())
        activity.startActivity(intent)
    }

    dialogBinding.facebook.setOnClickListener {
        val fbAppIntent = Intent(Intent.ACTION_VIEW, "fb://profile/${DATA.FB_ID}".toUri()).apply {
            setPackage("com.facebook.katana")
        }
        val fbWebIntent = Intent(Intent.ACTION_VIEW, "https://facebook.com/${DATA.FB_ID}".toUri())

        try {
            activity.startActivity(fbAppIntent)
        } catch (_: ActivityNotFoundException) {
            activity.startActivity(fbWebIntent)
        } catch (_: Exception) {
            activity.startActivity(fbWebIntent)
        }
    }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Context.moreOptionDialog(item: Book?) {
    item ?: return
    val activity = this as? Activity ?: return
    val options = arrayOf("Edit", "Delete")
    AlertDialog.Builder(this).setTitle("Choose Options").setItems(options) { _, which ->
        if (which == 0) activity.openActivity<BookEditActivity>(false, DATA.BOOK_ID to item.id)
        else if (which == 1) activity.dialogOptionDelete(item.publisher, item.id, item.title!!)
    }.show()
}

fun Context.dialogOptionDelete(
    publisher: String?, bookId: String?, bookTitle: String,
) {
    val activity = this as? Activity ?: return
    if (activity.isFinishing || activity.isDestroyed) return

    val dialogBinding = DialogLogoutBinding.inflate(layoutInflater)
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    dialogBinding.title.setText(R.string.do_you_want_to_delete_the_book)
    dialogBinding.yes.setOnClickListener {
        deleteBook(alertDialog, publisher, bookId, bookTitle)
    }
    dialogBinding.no.setOnClickListener { alertDialog.dismiss() }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}