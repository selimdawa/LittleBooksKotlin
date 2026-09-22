package com.flatcode.littlebooksadmin.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import coil3.load
import coil3.request.crossfade
import coil3.request.placeholder
import com.flatcode.littlebooksadmin.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

inline fun <reified T : Activity> Context.openActivity(
    clear: Boolean = false, vararg extras: Pair<String, Any?>
) {
    val intent = Intent(this, T::class.java).apply {
        if (clear) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        if (extras.isNotEmpty()) {
            val bundle = Bundle()
            extras.forEach { pair ->
                val key = pair.first
                when (val value = pair.second) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Float -> bundle.putFloat(key, value)
                    else -> bundle.putString(key, value.toString())
                }
            }
            putExtras(bundle)
        }
    }
    startActivity(intent)
}

fun TextView.loadPdfInfo() {
    // Cloudinary metadata is not easily accessible from client without Admin API
    // Setting a placeholder or empty for now
    this.text = "N/A"
}

fun TextView.loadCategory(categoryId: String?) {
    val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
    ref.child(categoryId!!).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val category = snapshot.child(DATA.CATEGORY).value?.toString().orEmpty()
            this@loadCategory.text = category
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun Context.downloadBook(bookId: String, bookTitle: String, bookUrl: String?) {
    val nameWithExtension = "$bookTitle.pdf"

    val progressDialog =
        Dialogs.createProgressDialog(this, getString(R.string.downloading_item, nameWithExtension))
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
                FirebaseUtils.incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
            }
        } catch (_: Exception) {
            (this as Activity).runOnUiThread {
                progressDialog.dismiss()
                Toast.makeText(
                    this, R.string.error_occurred, Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

private fun Context.saveDownloadedBook(
    progressDialog: AlertDialog, bytes: ByteArray,
    nameWithExtension: String, bookId: String,
) {
    try {
        val downloadsFolder =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsFolder.mkdirs()
        val filePath = downloadsFolder.path + "/" + nameWithExtension
        val out = FileOutputStream(filePath)
        out.write(bytes)
        out.close()
        Toast.makeText(this, R.string.saved_to_download_folder, Toast.LENGTH_SHORT).show()
        progressDialog.dismiss()
        FirebaseUtils.incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
    } catch (_: Exception) {
        Toast.makeText(
            this, R.string.failed_saving_to_download_folder, Toast.LENGTH_SHORT
        ).show()
        progressDialog.dismiss()
    }
}

fun ImageView.loadImage(isUser: Boolean, url: String) {
    try {
        if (url == DATA.BASIC) {
            if (isUser) {
                this.setImageResource(R.drawable.basic_user)
            } else {
                this.setImageResource(R.drawable.basic_book)
            }
        } else {
            this.load(url) {
                placeholder(R.color.image_profile)
                crossfade(true)
            }
        }
    } catch (_: Exception) {
        this.setImageResource(R.drawable.basic_book)
    }
}

fun ImageView.isFavorite(id: String?, userId: String?) {
    val reference = FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(userId!!)
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.child(id!!).exists()) {
                this@isFavorite.setImageResource(R.drawable.ic_star_selected)
                this@isFavorite.tag = "added"
            } else {
                this@isFavorite.setImageResource(R.drawable.ic_star_unselected)
                this@isFavorite.tag = "add"
            }
        }

        override fun onCancelled(error: DatabaseError) {}
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
        FirebaseUtils.incrementItemCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    } else {
        FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
            .child(DATA.FirebaseUserUid).removeValue()
        FirebaseUtils.incrementItemRemoveCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    }
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

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun TextView.nrLoves(bookId: String?) {
    val reference = FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            this@nrLoves.text = context.getString(
                R.string.number_placeholder_no_parentheses, dataSnapshot.childrenCount
            )
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}
