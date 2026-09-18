package com.flatcode.littlebooksadmin.utils

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import coil3.request.transformations
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.ui.book.BookEditActivity
import com.flatcode.littlebooksadmin.ui.category.CategoryEditActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.FileOutputStream
import java.text.MessageFormat

fun Context.intentClear(c: Class<*>?) {
    val intent = Intent(this, c)
    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
    this.startActivity(intent)
}

fun Context.intent1(c: Class<*>?) {
    val intent = Intent(this, c)
    this.startActivity(intent)
}

fun Context.intentExtra(c: Class<*>?, key: String?, value: String?) {
    val intent = Intent(this, c)
    intent.putExtra(key, value)
    this.startActivity(intent)
}

fun Context.intentExtra2(
    c: Class<*>?, key: String?, value: String?,
    key2: String?, value2: String?,
) {
    val intent = Intent(this, c)
    intent.putExtra(key, value)
    intent.putExtra(key2, value2)
    this.startActivity(intent)
}

fun Context.deleteBook(
    dialogDelete: Dialog, publisher: String?, bookId: String?,
    bookUrl: String?, bookTitle: String?,
) {
    val dialog = ProgressDialog(this)
    dialog.setTitle("Please wait")
    dialog.setMessage("Deleting $bookTitle ...")
    dialog.show()
    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
    storageReference.delete().addOnSuccessListener {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        reference.child(bookId!!).removeValue().addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(this, "Books Deleted Successfully...", Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
            incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
        }.addOnFailureListener { e: Exception ->
            dialog.dismiss()
            dialogDelete.dismiss()
            Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
        }
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        dialogDelete.dismiss()
        Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
    }
}

fun Context.deleteCategory(dialogDelete: Dialog, id: String?, name: String?) {
    val dialog = ProgressDialog(this)
    dialog.setTitle("Please wait")
    dialog.setMessage("Deleting $name ...")
    dialog.show()
    val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
    reference.child(id!!).removeValue().addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, "category Deleted Successfully...", Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
    }
}

fun TextView.loadPdfInfo(pdfUrl: String?) {
    val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl!!)
    ref.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
        val bytes = storageMetadata.sizeBytes.toDouble()
        val kb = bytes / 1024
        val mb = kb / 1024
        if (mb > 1) this.text = String.format("%.2f", mb) + " MB" else if (kb > 1) this.text =
            String.format("%.2f", mb) + " MB" else this.text =
            String.format("%.2f", bytes) + " bytes"
    }
}

fun TextView.loadCategory(categoryId: String?) {
    val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
    ref.child(categoryId!!).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val Category = DATA.EMPTY + snapshot.child(DATA.CATEGORY).value
            this@loadCategory.text = Category
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun incrementItemCount(database: String?, id: String?, childDB: String?) {
    val ref = FirebaseDatabase.getInstance().getReference(database!!)
    ref.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var itemsCount = DATA.EMPTY + snapshot.child(childDB!!).value
            if (itemsCount == DATA.EMPTY || itemsCount == DATA.NULL) itemsCount =
                DATA.EMPTY + DATA.ZERO

            val newItemsCount = itemsCount.toInt() + 1
            val hashMap = HashMap<String?, Any>()
            hashMap[childDB] = newItemsCount
            val reference = FirebaseDatabase.getInstance().getReference(database)
            reference.child(id).updateChildren(hashMap)
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun incrementItemRemoveCount(database: String?, id: String?, childDB: String?) {
    val ref = FirebaseDatabase.getInstance().getReference(database!!)
    ref.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            var lovesCount = DATA.EMPTY + snapshot.child(childDB!!).value
            if (lovesCount == DATA.EMPTY || lovesCount == DATA.NULL) lovesCount =
                DATA.EMPTY + DATA.ZERO

            val i = lovesCount.toInt()
            if (i > 0) {
                val removeLovesCount = lovesCount.toInt() - 1
                val hashMap = HashMap<String?, Any>()
                hashMap[childDB] = removeLovesCount
                val reference = FirebaseDatabase.getInstance().getReference(database)
                reference.child(id).updateChildren(hashMap)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun Context.downloadBook(bookId: String, bookTitle: String, bookUrl: String?) {
    val nameWithExtension = "$bookTitle.pdf"

    val progressDialog = ProgressDialog(this)
    progressDialog.setTitle("Please wait")
    progressDialog.setMessage("Downloading $nameWithExtension...")
    progressDialog.setCanceledOnTouchOutside(false)
    progressDialog.show()

    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
    storageReference.getBytes(DATA.MAX_BYTES_PDF.toLong())
        .addOnSuccessListener { bytes: ByteArray ->
            saveDownloadedBook(progressDialog, bytes, nameWithExtension, bookId)
            incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
        }.addOnFailureListener { e: Exception ->
            progressDialog.dismiss()
            Toast.makeText(
                this, "Failed to download due to " + e.message, Toast.LENGTH_SHORT
            ).show()
        }
}

private fun Context.saveDownloadedBook(
    progressDialog: ProgressDialog, bytes: ByteArray,
    nameWithExtension: String, bookId: String,
) {
    try {
        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsFolder.mkdirs()
        val FilePath = downloadsFolder.path + "/" + nameWithExtension
        val out = FileOutputStream(FilePath)
        out.write(bytes)
        out.close()
        Toast.makeText(this, "Saved to Download Folder", Toast.LENGTH_SHORT).show()
        progressDialog.dismiss()
        incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
    } catch (e: Exception) {
        Toast.makeText(
            this, "Failed saving to Download Folder due to " + e.message, Toast.LENGTH_SHORT
        ).show()
        progressDialog.dismiss()
    }
}

fun ImageView.loadWithGlide(isUser: Boolean, Url: String) {
    try {
        if (Url == DATA.BASIC) {
            if (isUser) {
                this.setImageResource(R.drawable.basic_user)
            } else {
                this.setImageResource(R.drawable.basic_book)
            }
        } else {
            this.load(Url) {
                placeholder(R.color.image_profile)
                crossfade(true)
            }
        }
    } catch (e: Exception) {
        this.setImageResource(R.drawable.basic_book)
    }
}

fun ImageView.loadWithGlideBlur(isUser: Boolean, Url: String, level: Int) {
    try {
        if (Url == DATA.BASIC) {
            if (isUser) {
                this.setImageResource(R.drawable.basic_user)
            } else {
                this.setImageResource(R.drawable.basic_book)
            }
        } else {
            this.load(Url) {
                placeholder(R.color.image_profile)
                transformations(SimpleBlurTransformation(level.toFloat()))
            }
        }
    } catch (e: Exception) {
        this.setImageResource(R.drawable.basic_book)
    }
}

fun ImageView.isFavorite(Id: String?, UserId: String?) {
    val reference =
        FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(UserId!!)
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.child(Id!!).exists()) {
                this@isFavorite.setImageResource(R.drawable.ic_star_selected)
                this@isFavorite.tag = "added"
            } else {
                this@isFavorite.setImageResource(R.drawable.ic_star_unselected)
                this@isFavorite.tag = "add"
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    })
}

fun ImageView.checkFavorite(bookId: String?) {
    if (this.tag == "add") {
        FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES)
            .child(DATA.FirebaseUserUid).child(bookId!!).setValue(true)
    } else {
        FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES)
            .child(DATA.FirebaseUserUid).child(bookId!!).removeValue()
    }
}

fun ImageView.checkLove(bookId: String?) {
    if (this.tag == "love") {
        FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
            .child(DATA.FirebaseUserUid).setValue(true)
        incrementItemCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    } else {
        FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
            .child(DATA.FirebaseUserUid).removeValue()
        incrementItemRemoveCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    }
}

fun Context.moreOptionDialog(item: Book?) {
    val bookId = item!!.id
    val bookUrl = item.url
    val bookTitle = item.title
    val publisher = item.publisher

    val options = arrayOf("Edit", "Delete")

    val builder = AlertDialog.Builder(this)
    builder.setTitle("Choose Options")
        .setItems(options) { dialog: DialogInterface?, which: Int ->
            if (which == 0) {
                this.intentExtra(BookEditActivity::class.java, DATA.BOOK_ID, bookId)
            } else if (which == 1) {
                this.dialogOptionDelete(
                    DATA.EMPTY + publisher,
                    DATA.EMPTY + bookId,
                    DATA.EMPTY + bookUrl,
                    DATA.EMPTY + bookTitle,
                    false,
                    false,
                    null,
                    null
                )
            }
        }.show()
}

fun Context.moreCategories(item: Category) {
    val id = item.id
    val name = item.category
    val publisher = item.publisher

    val options = arrayOf("Edit", "Delete")

    val builder = AlertDialog.Builder(this)
    builder.setTitle("Choose Options")
        .setItems(options) { dialog: DialogInterface?, which: Int ->
            if (which == 0) {
                this.intentExtra(CategoryEditActivity::class.java, DATA.CATEGORY_ID, id)
            } else if (which == 1) {
                this.dialogOptionDelete(
                    DATA.EMPTY + publisher,
                    null,
                    null,
                    null,
                    true,
                    false,
                    DATA.EMPTY + id,
                    DATA.EMPTY + name
                )
            }
        }.show()
}

fun ImageView.isLoves(bookId: String?) {
    val reference = FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(
        bookId!!
    )
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.child(DATA.FirebaseUserUid).exists()) {
                this@isLoves.setImageResource(R.drawable.ic_heart_selected)
                this@isLoves.tag = "loved"
            } else {
                this@isLoves.setImageResource(R.drawable.ic_heart_unselected)
                this@isLoves.tag = "love"
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    })
}

fun TextView.nrLoves(bookId: String?) {
    val reference = FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            this@nrLoves.text = MessageFormat.format(" {0} ", dataSnapshot.childrenCount)
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    })
}

fun Context.dialogOptionDelete(
    publisher: String?, bookId: String?, bookUrl: String?,
    bookTitle: String?, isCategory: Boolean, isEditorsChoice: Boolean,
    categoryId: String?, categoryName: String?,
) {
    val dialog = Dialog(this)
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
            this.deleteCategory(dialog, categoryId, categoryName)
        } else if (isEditorsChoice) {
            this.dialogUpdateEditorChoice(dialog, bookId)
        } else {
            this.deleteBook(dialog, publisher, bookId, bookUrl, bookTitle)
        }
    }
    dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.dismiss() }
    dialog.show()
    dialog.window!!.attributes = lp
}

fun Activity.cropImageSquare() {
    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE).setAspectRatio(1, 1)
        .setCropShape(CropImageView.CropShape.OVAL).start(this)
}

fun Activity.cropImageSlider() {
    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIX_SLIDER_X, DATA.MIX_SLIDER_Y).setAspectRatio(16, 9)
        .setCropShape(CropImageView.CropShape.OVAL).start(this)
}

fun Context.dialogUpdateEditorChoice(dialogDelete: Dialog, bookId: String?) {
    val dialog = ProgressDialog(this)
    dialog.setMessage("Updating Editors Choice...")
    dialog.show()
    val hashMap = HashMap<String?, Any>()
    hashMap[DATA.EDITORS_CHOICE] = 0
    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, "Editors Choice updated...", Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        Toast.makeText(this, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT)
            .show()
        dialogDelete.dismiss()
    }
}

fun Context.addToEditorsChoice(activity: Activity?, bookId: String?, number: Int) {
    val dialog = ProgressDialog(this)
    dialog.setMessage("Updating Editors Choice...")
    dialog.show()
    val hashMap = HashMap<String?, Any>()
    hashMap[DATA.EDITORS_CHOICE] = number
    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, "Editors Choice updated...", Toast.LENGTH_SHORT).show()
        activity!!.finish()
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        Toast.makeText(this, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT)
            .show()
    }
}

fun Context.getFileExtension(uri: Uri?): String? {
    val cR = this.contentResolver
    val mime = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cR.getType(uri!!))
}
