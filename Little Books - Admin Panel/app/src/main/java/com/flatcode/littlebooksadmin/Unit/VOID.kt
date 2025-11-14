package com.flatcode.littlebooksadmin.Unit

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
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.Modelimport.Category
import com.flatcode.littlebooksadmin.R
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

object VOID {
    fun IntentClear(context: Context, c: Class<*>?) {
        val intent = Intent(context, c)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun Intent1(context: Context, c: Class<*>?) {
        val intent = Intent(context, c)
        context.startActivity(intent)
    }

    fun IntentExtra(context: Context, c: Class<*>?, key: String?, value: String?) {
        val intent = Intent(context, c)
        intent.putExtra(key, value)
        context.startActivity(intent)
    }

    fun IntentExtra2(
        context: Context, c: Class<*>?, key: String?, value: String?,
        key2: String?, value2: String?,
    ) {
        val intent = Intent(context, c)
        intent.putExtra(key, value)
        intent.putExtra(key2, value2)
        context.startActivity(intent)
    }

    fun deleteBook(
        dialogDelete: Dialog, context: Context?, publisher: String?, bookId: String?,
        bookUrl: String?, bookTitle: String?,
    ) {
        val dialog = ProgressDialog(context)
        dialog.setTitle("Please wait")
        dialog.setMessage("Deleting $bookTitle ...")
        dialog.show()
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
        storageReference.delete().addOnSuccessListener {
            val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
            reference.child(bookId!!).removeValue().addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(context, "Books Deleted Successfully...", Toast.LENGTH_SHORT).show()
                dialogDelete.dismiss()
                incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
            }.addOnFailureListener { e: Exception ->
                dialog.dismiss()
                dialogDelete.dismiss()
                Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { e: Exception ->
            dialog.dismiss()
            dialogDelete.dismiss()
            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun deleteCategory(dialogDelete: Dialog, context: Context?, id: String?, name: String?) {
        val dialog = ProgressDialog(context)
        dialog.setTitle("Please wait")
        dialog.setMessage("Deleting $name ...")
        dialog.show()
        val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        reference.child(id!!).removeValue().addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(context, "category Deleted Successfully...", Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
        }.addOnFailureListener { e: Exception ->
            dialog.dismiss()
            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun loadPdfInfo(pdfUrl: String?, size: TextView) {
        val ref = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl!!)
        ref.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
            //get size in bytes
            val bytes = storageMetadata.sizeBytes.toDouble()
            //convert bytes to KB, MB
            val kb = bytes / 1024
            val mb = kb / 1024
            if (mb > 1) size.text = String.format("%.2f", mb) + " MB" else if (kb > 1) size.text =
                String.format("%.2f", mb) + " MB" else size.text =
                String.format("%.2f", bytes) + " bytes"
        }
    }

    fun loadCategory(categoryId: String?, category: TextView) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        ref.child(categoryId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val Category = DATA.EMPTY + snapshot.child(DATA.CATEGORY).value
                category.text = Category
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }


    fun incrementItemCount(database: String?, id: String?, childDB: String?) {
        val ref = FirebaseDatabase.getInstance().getReference(database!!)
        ref.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get views count
                var itemsCount = DATA.EMPTY + snapshot.child(childDB!!).value
                if (itemsCount == DATA.EMPTY || itemsCount == DATA.NULL)
                    itemsCount = DATA.EMPTY + DATA.ZERO

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
                //get views count
                var lovesCount = DATA.EMPTY + snapshot.child(childDB!!).value
                if (lovesCount == DATA.EMPTY || lovesCount == DATA.NULL)
                    lovesCount = DATA.EMPTY + DATA.ZERO

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

    fun downloadBook(context: Context, bookId: String, bookTitle: String, bookUrl: String?) {
        val nameWithExtension = "$bookTitle.pdf"

        //progress dialog
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please wait")
        progressDialog.setMessage("Downloading $nameWithExtension...") //e-9. Downloading ABC_Book.paf
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.show()

        //download from firebase storage using url
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
        storageReference.getBytes(DATA.MAX_BYTES_PDF.toLong())
            .addOnSuccessListener { bytes: ByteArray ->
                saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId)
                incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
            }.addOnFailureListener { e: Exception ->
                progressDialog.dismiss()
                Toast.makeText(
                    context, "Failed to download due to " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveDownloadedBook(
        context: Context, progressDialog: ProgressDialog, bytes: ByteArray,
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
            Toast.makeText(context, "Saved to Download Folder", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
            incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
        } catch (e: Exception) {
            Toast.makeText(
                context, "Failed saving to Download Folder due to " + e.message, Toast.LENGTH_SHORT
            ).show()
            progressDialog.dismiss()
        }
    }

    fun Glide(isUser: Boolean, context: Context?, Url: String, Image: ImageView) {
        try {
            if (Url == DATA.BASIC) {
                if (isUser) {
                    Image.setImageResource(R.drawable.basic_user)
                } else {
                    Image.setImageResource(R.drawable.basic_book)
                }
            } else {
                Glide.with(context!!).load(Url).placeholder(R.color.image_profile).into(Image)
            }
        } catch (e: Exception) {
            Image.setImageResource(R.drawable.basic_book)
        }
    }

    fun isFavorite(add: ImageView, Id: String?, UserId: String?) {
        val reference =
            FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(UserId!!)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(Id!!).exists()) {
                    add.setImageResource(R.drawable.ic_star_selected)
                    add.tag = "added"
                } else {
                    add.setImageResource(R.drawable.ic_star_unselected)
                    add.tag = "add"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun checkFavorite(image: ImageView, bookId: String?) {
        if (image.tag == "add") {
            FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES)
                .child(DATA.FirebaseUserUid).child(bookId!!).setValue(true)
        } else {
            FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES)
                .child(DATA.FirebaseUserUid).child(bookId!!).removeValue()
        }
    }

    fun checkLove(image: ImageView, bookId: String?) {
        if (image.tag == "love") {
            FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
                .child(DATA.FirebaseUserUid).setValue(true)
            incrementItemCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
        } else {
            FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
                .child(DATA.FirebaseUserUid).removeValue()
            incrementItemRemoveCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
        }
    }

    fun moreOptionDialog(context: Context, item: Book?) {
        val bookId = item!!.id
        val bookUrl = item.url
        val bookTitle = item.title
        val publisher = item.publisher

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Options")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                //handle dialog option click
                if (which == 0) {
                    //Edit clicked ,Open new activity to edit the book info
                    IntentExtra(context, CLASS.BOOK_EDIT, DATA.BOOK_ID, bookId)
                } else if (which == 1) {
                    //Delete Clicked
                    dialogOptionDelete(
                        context, DATA.EMPTY + publisher, DATA.EMPTY + bookId,
                        DATA.EMPTY + bookUrl, DATA.EMPTY + bookTitle,
                        false, false, null, null
                    )
                }
            }.show()
    }

    fun moreCategories(context: Context, item: Category) {
        val id = item.id
        val name = item.category
        val publisher = item.publisher

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Options")
            .setItems(options) { dialog: DialogInterface?, which: Int ->
                //handle dialog option click
                if (which == 0) {
                    //Edit clicked ,Open new activity to edit the book info
                    IntentExtra(context, CLASS.CATEGORY_EDIT, DATA.CATEGORY_ID, id)
                } else if (which == 1) {
                    //Delete Clicked
                    dialogOptionDelete(
                        context, DATA.EMPTY + publisher, null,
                        null, null, true, false,
                        DATA.EMPTY + id, DATA.EMPTY + name
                    )
                }
            }.show()
    }

    fun isLoves(image: ImageView, bookId: String?) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(
            bookId!!
        )
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.child(DATA.FirebaseUserUid).exists()) {
                    image.setImageResource(R.drawable.ic_heart_selected)
                    image.tag = "loved"
                } else {
                    image.setImageResource(R.drawable.ic_heart_unselected)
                    image.tag = "love"
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun nrLoves(number: TextView, bookId: String?) {
        val reference = FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                number.text = MessageFormat.format(" {0} ", dataSnapshot.childrenCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun dialogOptionDelete(
        context: Context?, publisher: String?, bookId: String?, bookUrl: String?,
        bookTitle: String?, isCategory: Boolean, isEditorsChoice: Boolean,
        categoryId: String?, categoryName: String?,
    ) {
        val dialog = Dialog(context!!)
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
                deleteCategory(dialog, context, categoryId, categoryName)
            } else if (isEditorsChoice) {
                dialogUpdateEditorChoice(dialog, context, bookId)
            } else {
                deleteBook(dialog, context, publisher, bookId, bookUrl, bookTitle)
            }
        }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun cropImageSquare(activity: Activity?) {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .setMinCropResultSize(DATA.MIX_SQUARE, DATA.MIX_SQUARE)
            .setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(activity!!)
    }

    fun cropImageSlider(activity: Activity?) {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .setMinCropResultSize(DATA.MIX_SLIDER_X, DATA.MIX_SLIDER_Y)
            .setAspectRatio(16, 9)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(activity!!)
    }

    fun dialogUpdateEditorChoice(dialogDelete: Dialog, context: Context?, bookId: String?) {
        val dialog = ProgressDialog(context)
        dialog.setMessage("Updating Editors Choice...")
        dialog.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.EDITORS_CHOICE] = 0
        val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(context, "Editors Choice updated...", Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
        }.addOnFailureListener { e: Exception ->
            dialog.dismiss()
            Toast.makeText(context, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT)
                .show()
            dialogDelete.dismiss()
        }
    }

    fun addToEditorsChoice(context: Context?, activity: Activity?, bookId: String?, number: Int) {
        val dialog = ProgressDialog(context)
        dialog.setMessage("Updating Editors Choice...")
        dialog.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.EDITORS_CHOICE] = number
        val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(context, "Editors Choice updated...", Toast.LENGTH_SHORT).show()
            activity!!.finish()
        }.addOnFailureListener { e: Exception ->
            dialog.dismiss()
            Toast.makeText(context, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    fun intro(context: Context?, background: ImageView, backWhite: ImageView, backDark: ImageView) {
        val sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context!!)
        if (sharedPreferences.getString("color_option", "ONE") == "ONE") {
            background.setImageResource(R.drawable.background_day)
            backWhite.visibility = View.VISIBLE
            backDark.visibility = View.GONE
        } else if (sharedPreferences.getString("color_option", "NIGHT_ONE") == "NIGHT_ONE") {
            background.setImageResource(R.drawable.background_night)
            backWhite.visibility = View.GONE
            backDark.visibility = View.VISIBLE
        }
    }

    fun logo(context: Context?, background: ImageView) {
        val sharedPreferences = PreferenceManager
            .getDefaultSharedPreferences(context!!)
        if (sharedPreferences.getString("color_option", "ONE") == "ONE") {
            background.setImageResource(R.drawable.logo)
        } else if (sharedPreferences.getString("color_option", "NIGHT_ONE") == "NIGHT_ONE") {
            background.setImageResource(R.drawable.logo_night)
        }
    }

    fun getFileExtension(uri: Uri?, context: Context): String? {
        val cR = context.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri!!))
    }
}