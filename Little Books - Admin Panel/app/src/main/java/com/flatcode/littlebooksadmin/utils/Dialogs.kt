package com.flatcode.littlebooksadmin.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.ui.book.BookEditActivity
import com.flatcode.littlebooksadmin.ui.category.CategoryEditActivity
import com.google.firebase.database.FirebaseDatabase

object Dialogs {
    fun createProgressDialog(context: Context, message: String): AlertDialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        view.findViewById<TextView>(R.id.message).text = message
        return AlertDialog.Builder(context)
            .setView(view)
            .setCancelable(false)
            .create()
    }

    fun deleteBook(context: Context, dialogDelete: Dialog, publisher: String?, bookId: String?, bookTitle: String?) {
        val dialog = createProgressDialog(context, context.getString(R.string.deleting_item, bookTitle))
        dialog.show()

        val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        reference.child(bookId!!).removeValue().addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(context, R.string.books_deleted_successfully, Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
            FirebaseUtils.incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
        }.addOnFailureListener { _ ->
            dialog.dismiss()
            dialogDelete.dismiss()
            Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteCategory(context: Context, dialogDelete: Dialog, id: String?, name: String?) {
        val dialog = createProgressDialog(context, context.getString(R.string.deleting_item, name))
        dialog.show()
        val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        reference.child(id!!).removeValue().addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(context, R.string.category_deleted_successfully, Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
        }.addOnFailureListener { _ ->
            dialog.dismiss()
            Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show()
        }
    }

    fun moreOptionDialog(context: Context, item: Book?) {
        val bookId = item!!.id
        val bookTitle = item.title
        val publisher = item.publisher

        val options = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.choose_options)
            .setItems(options) { _: DialogInterface?, which: Int ->
                if (which == 0) {
                    context.openActivity<BookEditActivity>(extras = arrayOf(DATA.BOOK_ID to bookId))
                } else if (which == 1) {
                    dialogOptionDelete(
                        context = context,
                        publisher = DATA.EMPTY + publisher,
                        bookId = DATA.EMPTY + bookId,
                        bookTitle = DATA.EMPTY + bookTitle,
                        isCategory = false,
                        isEditorsChoice = false,
                        categoryId = null,
                        categoryName = null
                    )
                }
            }.show()
    }

    fun moreCategories(context: Context, item: Category) {
        val id = item.id
        val name = item.category
        val publisher = item.publisher

        val options = arrayOf(context.getString(R.string.edit), context.getString(R.string.delete))

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.choose_options)
            .setItems(options) { _: DialogInterface?, which: Int ->
                if (which == 0) {
                    context.openActivity<CategoryEditActivity>(extras = arrayOf(DATA.CATEGORY_ID to id))
                } else if (which == 1) {
                    dialogOptionDelete(
                        context = context,
                        publisher = DATA.EMPTY + publisher,
                        bookId = null,
                        bookTitle = null,
                        isCategory = true,
                        isEditorsChoice = false,
                        categoryId = DATA.EMPTY + id,
                        categoryName = DATA.EMPTY + name
                    )
                }
            }.show()
    }

    fun dialogOptionDelete(
        context: Context, publisher: String?, bookId: String?,
        bookTitle: String?, isCategory: Boolean, isEditorsChoice: Boolean,
        categoryId: String?, categoryName: String?,
    ) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        val title = dialog.findViewById<TextView>(R.id.title)
        if (isCategory) {
            title.setText(R.string.do_you_want_to_delete_the_category)
        } else {
            title.setText(R.string.do_you_want_to_delete_the_book)
        }
        dialog.findViewById<View>(R.id.yes).setOnClickListener {
            if (isCategory) {
                deleteCategory(context, dialog, categoryId, categoryName)
            } else if (isEditorsChoice) {
                dialogUpdateEditorChoice(context, dialog, bookId)
            } else {
                deleteBook(context, dialog, publisher, bookId, bookTitle)
            }
        }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun dialogUpdateEditorChoice(context: Context, dialogDelete: Dialog, bookId: String?) {
        val dialog = createProgressDialog(context, context.getString(R.string.updating_editors_choice))
        dialog.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.EDITORS_CHOICE] = 0
        val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(context, R.string.editors_choice_updated, Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
        }.addOnFailureListener { _ ->
            dialog.dismiss()
            Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
        }
    }

    fun addToEditorsChoice(context: Context, activity: Activity?, bookId: String?, number: Int) {
        val dialog = createProgressDialog(context, context.getString(R.string.updating_editors_choice))
        dialog.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.EDITORS_CHOICE] = number
        val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(context, R.string.editors_choice_updated, Toast.LENGTH_SHORT).show()
            activity!!.finish()
        }.addOnFailureListener { _ ->
            dialog.dismiss()
            Toast.makeText(context, R.string.error_occurred, Toast.LENGTH_SHORT).show()
        }
    }
}
