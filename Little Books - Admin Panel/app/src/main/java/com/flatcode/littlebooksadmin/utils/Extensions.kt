package com.flatcode.littlebooksadmin.utils

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.os.bundleOf
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import coil3.request.transformations
import coil3.size.Size
import coil3.transform.Transformation
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.model.Book
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.ui.book.BookEditActivity
import com.flatcode.littlebooksadmin.ui.category.CategoryEditActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.MessageFormat
import java.util.concurrent.Executors

inline fun <reified T : Activity> Context.openActivity(
    clear: Boolean = false, vararg extras: Pair<String, Any?>
) {
    val intent = Intent(this, T::class.java).apply {
        if (clear) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (extras.isNotEmpty()) {
            putExtras(bundleOf(*extras))
        }
    }
    startActivity(intent)
}

fun Context.deleteBook(
    dialogDelete: Dialog, publisher: String?, bookId: String?,
    bookUrl: String?, bookTitle: String?,
) {
    val dialog = ProgressDialog(this)
    dialog.setTitle(R.string.please_wait)
    dialog.setMessage(getString(R.string.deleting_item, bookTitle))
    dialog.show()

    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).removeValue().addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.books_deleted_successfully, Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
        incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        dialogDelete.dismiss()
        Toast.makeText(this, getString(R.string.error_message, e.message), Toast.LENGTH_SHORT)
            .show()
    }
}

fun Context.deleteCategory(dialogDelete: Dialog, id: String?, name: String?) {
    val dialog = ProgressDialog(this)
    dialog.setTitle(R.string.please_wait)
    dialog.setMessage(getString(R.string.deleting_item, name))
    dialog.show()
    val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
    reference.child(id!!).removeValue().addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.category_deleted_successfully, Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        Toast.makeText(this, getString(R.string.error_message, e.message), Toast.LENGTH_SHORT)
            .show()
    }
}

fun TextView.loadPdfInfo(pdfUrl: String?) {
    // Cloudinary metadata is not easily accessible from client without Admin API
    // Setting a placeholder or empty for now
    this.text = "N/A"
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
    progressDialog.setTitle(R.string.please_wait)
    progressDialog.setMessage(getString(R.string.downloading_item, nameWithExtension))
    progressDialog.setCanceledOnTouchOutside(false)
    progressDialog.show()

    val executor = Executors.newSingleThreadExecutor()
    executor.execute {
        try {
            val url = URL(bookUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connect()

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                (this as Activity).runOnUiThread {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this,
                        getString(R.string.server_returned_http, connection.responseCode),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return@execute
            }

            val inputStream = connection.inputStream
            val bytes = inputStream.readBytes()
            inputStream.close()

            (this as Activity).runOnUiThread {
                saveDownloadedBook(progressDialog, bytes, nameWithExtension, bookId)
                incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
            }
        } catch (e: Exception) {
            (this as Activity).runOnUiThread {
                progressDialog.dismiss()
                Toast.makeText(
                    this,
                    getString(R.string.error_message, e.message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
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
        Toast.makeText(this, R.string.saved_to_download_folder, Toast.LENGTH_SHORT).show()
        progressDialog.dismiss()
        incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
    } catch (e: Exception) {
        Toast.makeText(
            this,
            getString(R.string.failed_saving_to_download_folder, e.message),
            Toast.LENGTH_SHORT
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
    val reference = FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(UserId!!)
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
        FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(DATA.FirebaseUserUid)
            .child(bookId!!).setValue(true)
    } else {
        FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(DATA.FirebaseUserUid)
            .child(bookId!!).removeValue()
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

    val options = arrayOf(getString(R.string.edit), getString(R.string.delete))

    val builder = AlertDialog.Builder(this)
    builder.setTitle(R.string.choose_options)
        .setItems(options) { dialog: DialogInterface?, which: Int ->
            if (which == 0) {
                this.openActivity<BookEditActivity>(extras = arrayOf(DATA.BOOK_ID to bookId))
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

    val options = arrayOf(getString(R.string.edit), getString(R.string.delete))

    val builder = AlertDialog.Builder(this)
    builder.setTitle(R.string.choose_options)
        .setItems(options) { dialog: DialogInterface?, which: Int ->
            if (which == 0) {
                this.openActivity<CategoryEditActivity>(extras = arrayOf(DATA.CATEGORY_ID to id))
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

fun Context.dialogUpdateEditorChoice(dialogDelete: Dialog, bookId: String?) {
    val dialog = ProgressDialog(this)
    dialog.setMessage(getString(R.string.updating_editors_choice))
    dialog.show()
    val hashMap = HashMap<String?, Any>()
    hashMap[DATA.EDITORS_CHOICE] = 0
    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.editors_choice_updated, Toast.LENGTH_SHORT).show()
        dialogDelete.dismiss()
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        Toast.makeText(this, getString(R.string.error_message, e.message), Toast.LENGTH_SHORT)
            .show()
        dialogDelete.dismiss()
    }
}

fun Context.addToEditorsChoice(activity: Activity?, bookId: String?, number: Int) {
    val dialog = ProgressDialog(this)
    dialog.setMessage(getString(R.string.updating_editors_choice))
    dialog.show()
    val hashMap = HashMap<String?, Any>()
    hashMap[DATA.EDITORS_CHOICE] = number
    val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
    reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
        dialog.dismiss()
        Toast.makeText(this, R.string.editors_choice_updated, Toast.LENGTH_SHORT).show()
        activity!!.finish()
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        Toast.makeText(this, getString(R.string.error_message, e.message), Toast.LENGTH_SHORT)
            .show()
    }
}

fun Context.getFileExtension(uri: Uri?): String? {
    val cR = this.contentResolver
    val mime = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cR.getType(uri!!))
}

class SimpleBlurTransformation(private val radius: Float) : Transformation() {
    override val cacheKey: String = "${SimpleBlurTransformation::class.java.name}-$radius"

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        if (input.isRecycled) return input
        val scaleFactor = 6
        val w = (input.width / scaleFactor).coerceAtLeast(1)
        val h = (input.height / scaleFactor).coerceAtLeast(1)
        val small = input.scale(w, h, true)
        val r = (radius / scaleFactor).toInt().coerceAtLeast(1)
        val pix = IntArray(w * h)
        small.getPixels(pix, 0, w, 0, 0, w, h)
        val blurred = IntArray(w * h)
        for (y in 0 until h) for (x in 0 until w) {
            var rs = 0L
            var gs = 0L
            var bs = 0L
            var c = 0
            for (i in -r..r) {
                val xi = (x + i).coerceIn(0, w - 1)
                val p = pix[y * w + xi]
                rs += (p shr 16) and 0xff
                gs += (p shr 8) and 0xff
                bs += p and 0xff
                c++
            }
            blurred[y * w + x] =
                (0xff shl 24) or ((rs / c).toInt() shl 16) or ((gs / c).toInt() shl 8) or (bs / c).toInt()
        }
        for (x in 0 until w) for (y in 0 until h) {
            var rs = 0L
            var gs = 0L
            var bs = 0L
            var c = 0
            for (i in -r..r) {
                val yi = (y + i).coerceIn(0, h - 1)
                val p = blurred[yi * w + x]
                rs += (p shr 16) and 0xff
                gs += (p shr 8) and 0xff
                bs += p and 0xff
                c++
            }
            pix[y * w + x] =
                (0xff shl 24) or ((rs / c).toInt() shl 16) or ((gs / c).toInt() shl 8) or (bs / c).toInt()
        }
        val output = createBitmap(w, h, Bitmap.Config.ARGB_8888)
        output.setPixels(pix, 0, w, 0, 0, w, h)
        val finalOutput = output.scale(input.width, input.height, true)
        if (output != finalOutput) output.recycle()
        if (small != input) small.recycle()
        return finalOutput
    }
}