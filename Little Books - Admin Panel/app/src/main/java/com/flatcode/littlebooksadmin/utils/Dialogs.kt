package com.flatcode.littlebooksadmin.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.DialogLogoutBinding
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.ui.book.BookEditActivity
import com.flatcode.littlebooksadmin.ui.category.CategoryEditActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

fun Context.deleteBook(
    dialogDelete: Dialog, publisher: String?, bookId: String?, bookTitle: String?
) {
    val dialog = AlertDialog.Builder(this).apply {
        setMessage(getString(R.string.deleting_item, bookTitle))
    }.show()

    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).removeValue().addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.books_deleted_successfully, Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
        FirebaseUtils.incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
    }.addOnFailureListener { _ ->
        dialog.dismiss()
        dialogDelete.dismiss()
        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
    }
}

fun Context.deleteCategory(dialogDelete: Dialog, id: String?, name: String?) {
    val dialog = AlertDialog.Builder(this).apply {
        setMessage(getString(R.string.deleting_item, name))
    }.show()

    val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
    reference.child(id!!).removeValue().addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.category_deleted_successfully, Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
    }.addOnFailureListener { _ ->
        dialog.dismiss()
        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
    }
}

fun Context.moreOptionDialog(item: Book?) {
    val bookId = item!!.id
    val bookTitle = item.title
    val publisher = item.publisher

    val options = arrayOf(getString(R.string.edit), getString(R.string.delete))

    val builder = AlertDialog.Builder(this)
    builder.setTitle(R.string.choose_options).setItems(options) { _: DialogInterface?, which: Int ->
        if (which == 0) {
            openActivity<BookEditActivity>(extras = arrayOf(DATA.BOOK_ID to bookId))
        } else if (which == 1) {
            dialogOptionDelete(
                publisher = publisher.orEmpty(),
                bookId = bookId,
                bookTitle = bookTitle.orEmpty(),
                isCategory = false,
                isEditorsChoice = false,
                categoryId = null,
                categoryName = null
            )
        }
    }.show()
}

fun Context.moreCategories(item: Category) {
    val id = item.id
    val name = item.category
    val publisher = item.publisher

    val options = arrayOf(getString(R.string.edit), getString(R.string.delete))

    val builder = AlertDialog.Builder(this)
    builder.setTitle(R.string.choose_options).setItems(options) { _: DialogInterface?, which: Int ->
        if (which == 0) {
            openActivity<CategoryEditActivity>(extras = arrayOf(DATA.CATEGORY_ID to id))
        } else if (which == 1) {
            dialogOptionDelete(
                publisher = publisher.orEmpty(),
                bookId = null,
                bookTitle = null,
                isCategory = true,
                isEditorsChoice = false,
                categoryId = id,
                categoryName = name.orEmpty()
            )
        }
    }.show()
}

fun Context.dialogOptionDelete(
    publisher: String?, bookId: String?,
    bookTitle: String?, isCategory: Boolean, isEditorsChoice: Boolean,
    categoryId: String?, categoryName: String?,
) {
    if (this is Activity && (isFinishing || isDestroyed)) return

    val dialogBinding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    val alertDialog = MaterialAlertDialogBuilder(this).setView(dialogBinding.root).create()

    alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    if (isCategory) {
        dialogBinding.title.setText(R.string.do_you_want_to_delete_the_category)
    } else {
        dialogBinding.title.setText(R.string.do_you_want_to_delete_the_book)
    }
    dialogBinding.yes.setOnClickListener {
        if (isCategory) {
            deleteCategory(alertDialog, categoryId, categoryName)
        } else if (isEditorsChoice) {
            dialogUpdateEditorChoice(alertDialog, bookId)
        } else {
            deleteBook(alertDialog, publisher, bookId, bookTitle)
        }
    }
    dialogBinding.no.setOnClickListener { alertDialog.dismiss() }

    alertDialog.show()

    val widthPx = (300 * resources.displayMetrics.density).toInt()
    alertDialog.window?.setLayout(widthPx, ViewGroup.LayoutParams.WRAP_CONTENT)
}

fun Context.dialogUpdateEditorChoice(dialogDelete: Dialog, bookId: String?) {
    val dialog = AlertDialog.Builder(this).apply {
        setMessage(getString(R.string.updating_editors_choice))
    }.show()

    val hashMap = HashMap<String?, Any>()
    hashMap[DATA.EDITORS_CHOICE] = 0
    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.editors_choice_updated, Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
    }.addOnFailureListener { _ ->
        dialog.dismiss()
        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
    }
}

fun Context.addToEditorsChoice(activity: Activity?, bookId: String?, number: Int) {
    val dialog = AlertDialog.Builder(this).apply {
        setMessage(getString(R.string.updating_editors_choice))
    }.show()

    val hashMap = HashMap<String?, Any>()
    hashMap[DATA.EDITORS_CHOICE] = number
    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.editors_choice_updated, Toast.LENGTH_SHORT).show()
        activity?.finish()
    }.addOnFailureListener { _ ->
        dialog.dismiss()
        Toast.makeText(this, R.string.error_occurred, Toast.LENGTH_SHORT).show()
    }
}