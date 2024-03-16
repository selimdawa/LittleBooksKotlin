package com.flatcode.littlebooks.Unit

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.flatcode.littlebooks.Model.ADs
import com.flatcode.littlebooks.Model.Book
import com.flatcode.littlebooks.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.StorageReference
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import jp.wasabeef.glide.transformations.BlurTransformation
import java.io.File
import java.io.FileOutputStream
import java.text.MessageFormat

object VOID {
    fun IntentClear(context: Context?, c: Class<*>?) {
        val intent = Intent(context, c)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        context!!.startActivity(intent)
    }

    fun Intent1(context: Context?, c: Class<*>?) {
        val intent = Intent(context, c)
        context!!.startActivity(intent)
    }

    fun IntentExtra(context: Context?, c: Class<*>?, key: String?, value: String?) {
        val intent = Intent(context, c)
        intent.putExtra(key, value)
        context!!.startActivity(intent)
    }

    fun IntentExtra2(
        context: Context?,
        c: Class<*>?,
        key: String?,
        value: String?,
        key2: String?,
        value2: String?,
    ) {
        val intent = Intent(context, c)
        intent.putExtra(key, value)
        intent.putExtra(key2, value2)
        context!!.startActivity(intent)
    }

    fun IntentExtra3(
        context: Context?, c: Class<*>?, key: String?, value: String?,
        key2: String?, value2: String?, key3: String?, value3: String?,
    ) {
        val intent = Intent(context, c)
        intent.putExtra(key, value)
        intent.putExtra(key2, value2)
        intent.putExtra(key3, value3)
        context!!.startActivity(intent)
    }

    fun deleteBook(
        dialogDelete: Dialog,
        context: Context?,
        publisher: String?,
        bookId: String?,
        bookUrl: String?,
        bookTitle: String,
    ) {
        val dialog = ProgressDialog(context)
        dialog.setTitle("Please wait")
        dialog.setMessage("Deleting $bookTitle ...")
        dialog.show()
        val storageReference: StorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
        storageReference.delete().addOnSuccessListener {
            val reference: DatabaseReference =
                FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
            reference.child(bookId!!).removeValue()
                .addOnSuccessListener {
                    dialog.dismiss()
                    Toast.makeText(context, "Books Deleted Successfully...", Toast.LENGTH_SHORT)
                        .show()
                    dialogDelete.dismiss()
                    incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
                }.addOnFailureListener { e: Exception ->
                    dialog.dismiss()
                    Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener { e: Exception ->
            dialog.dismiss()
            Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    fun loadPdfInfo(pdfUrl: String?, size: TextView) {
        val ref: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl!!)
        ref.metadata
            .addOnSuccessListener { storageMetadata: StorageMetadata ->
                //get size in bytes
                val bytes: Double = storageMetadata.sizeBytes.toDouble()
                //convert bytes to KB, MB
                val kb = bytes / 1024
                val mb = kb / 1024
                if (mb > 1) size.text = String.format("%.2f", mb) + " MB"
                else if (kb > 1) size.text =
                    String.format("%.2f", mb) + " MB" else size.text =
                    String.format("%.2f", bytes) + " bytes"
            }
    }

    fun loadCategory(categoryId: String?, category: TextView) {
        val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
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
        val storageReference: StorageReference =
            FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
        storageReference.getBytes(DATA.MAX_BYTES_PDF.toLong())
            .addOnSuccessListener { bytes: ByteArray ->
                saveDownloadedBook(context, progressDialog, bytes, nameWithExtension, bookId)
                incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
            }.addOnFailureListener { e: Exception ->
                progressDialog.dismiss()
                Toast.makeText(
                    context,
                    "Failed to download due to " + e.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun saveDownloadedBook(
        context: Context,
        progressDialog: ProgressDialog,
        bytes: ByteArray,
        nameWithExtension: String,
        bookId: String,
    ) {
        try {
            val downloadsFolder: File =
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
                context,
                "Failed saving to Download Folder due to " + e.message,
                Toast.LENGTH_SHORT
            ).show()
            progressDialog.dismiss()
        }
    }

    fun Glide_(isUser: Boolean, context: Context?, Url: String?, Image: ImageView) {
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

    fun GlideBlur(isUser: Boolean, context: Context?, Url: String, Image: ImageView, level: Int) {
        try {
            if (Url == DATA.BASIC) {
                if (isUser) {
                    Image.setImageResource(R.drawable.basic_user)
                } else {
                    Image.setImageResource(R.drawable.basic_book)
                }
            } else {
                Glide.with(context!!).load(Url).placeholder(R.color.image_profile)
                    .apply(RequestOptions.bitmapTransform(BlurTransformation(level))).into(Image)
            }
        } catch (e: Exception) {
            Image.setImageResource(R.drawable.basic_book)
        }
    }

    fun closeApp(context: Context?, a: Activity?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_close_app)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.findViewById<View>(R.id.yes).setOnClickListener { a!!.finish() }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.cancel() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun dialogLogout(context: Context?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.findViewById<View>(R.id.yes).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            IntentClear(context, CLASS.AUTH)
        }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.cancel() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun shareApp(context: Context?) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "share app")
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            " Download the app now from Google Play " + " https://play.google.com/store/apps/details?id=" + context!!.packageName
        )
        context.startActivity(Intent.createChooser(shareIntent, "Choose how to share"))
    }

    fun rateApp(context: Context?) {
        val uri = Uri.parse("market://details?id=" + context!!.packageName)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + context.packageName)
                )
            )
        }
    }

    fun dialogAboutApp(context: Context?) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_about_app)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.findViewById<View>(R.id.website).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                context.startActivity(websiteIntent)
            }

            val websiteIntent: Intent
                get() = Intent(Intent.ACTION_VIEW, Uri.parse(DATA.WEB_SITE))
        })
        dialog.findViewById<View>(R.id.facebook).setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                context.startActivity(openFacebookIntent)
            }

            val openFacebookIntent: Intent
                get() = try {
                    context.packageManager.getPackageInfo("com.facebook.katana", 0)
                    Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + DATA.FB_ID))
                } catch (e: Exception) {
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://www.facebook.com/" + DATA.FB_ID)
                    )
                }
        })
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun isFavorite(add: ImageView, Id: String?, UserId: String?) {
        val reference: DatabaseReference =
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
        if (image.tag == "add") FirebaseDatabase.getInstance().getReference(DATA.FAVORITES)
            .child(DATA.FirebaseUserUid)
            .child(bookId!!).setValue(true) else FirebaseDatabase.getInstance()
            .getReference(DATA.FAVORITES).child(DATA.FirebaseUserUid)
            .child(bookId!!).removeValue()
    }

    fun checkLove(image: ImageView, bookId: String?) {
        if (image.tag == "love") {
            FirebaseDatabase.getInstance().getReference(DATA.LOVES).child(bookId!!)
                .child(DATA.FirebaseUserUid).setValue(true)
            incrementItemCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
        } else {
            FirebaseDatabase.getInstance().getReference(DATA.LOVES).child(bookId!!)
                .child(DATA.FirebaseUserUid).removeValue()
            incrementItemRemoveCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
        }
    }

    fun moreOptionDialog(context: Context?, item: Book?) {
        val bookId = item!!.id
        val bookUrl = item.url
        val bookTitle = item.title
        val publisher = item.publisher

        //options to show in dialog
        val options = arrayOf("Edit", "Delete")

        //alert dialog
        val builder = AlertDialog.Builder(context!!)
        builder.setTitle("Choose Options").setItems(
            options
        ) { _: DialogInterface?, which: Int ->
            //handle dialog option click
            if (which == 0) {
                //Edit clicked ,Open new activity to edit the book info
                IntentExtra(context, CLASS.BOOK_EDIT, DATA.BOOK_ID, bookId)
            } else if (which == 1) {
                //Delete Clicked
                dialogOptionDelete(
                    context, DATA.EMPTY + publisher, DATA.EMPTY + bookId,
                    DATA.EMPTY + bookUrl, DATA.EMPTY + bookTitle
                )
            }
        }.show()
    }

    fun isLoves(image: ImageView, bookId: String?) {
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
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
        val reference: DatabaseReference =
            FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                number.text = MessageFormat.format(" {0} ", dataSnapshot.childrenCount)
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    fun dialogOptionDelete(
        context: Context?, publisher: String?, bookId: String?, bookUrl: String?, bookTitle: String,
    ) {
        val dialog = Dialog(context!!)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(true)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        val title: TextView = dialog.findViewById(R.id.title)
        title.setText(R.string.do_you_want_to_delete_the_book)
        dialog.findViewById<View>(R.id.yes).setOnClickListener {
            deleteBook(dialog, context, publisher, bookId, bookUrl, bookTitle)
        }
        dialog.findViewById<View>(R.id.no).setOnClickListener { dialog.dismiss() }
        dialog.show()
        dialog.window!!.attributes = lp
    }

    fun CropImageSquare(activity: Activity?) {
        CropImage.activity()
            .setGuidelines(CropImageView.Guidelines.ON)
            .setMultiTouchEnabled(true)
            .setMinCropResultSize(DATA.MIN_SQUARE, DATA.MIN_SQUARE)
            .setAspectRatio(1, 1)
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(activity!!)
    }

    fun BannerAd(context: Context?, adView: AdView, bannerName: String?) {
        MobileAds.initialize(context!!) { }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                AdUserCount(DATA.FirebaseUserUid, DATA.AD_LOAD, 1)
                AdCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_LOADED_COUNT)
            }

            override fun onAdOpened() {
                AdUserCount(DATA.FirebaseUserUid, DATA.AD_CLICK, 1)
                AdCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_CLICKED_COUNT)
            }
        }
    }

    fun BannerAdTwo(
        context: Context?,
        adView: AdView,
        bannerName: String?,
        adView2: AdView,
        bannerName2: String?,
    ) {
        MobileAds.initialize(context!!) { }

        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {}
            override fun onAdOpened() {
                AdUserCount(DATA.FirebaseUserUid, DATA.AD_CLICK, 1)
                AdCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_CLICKED_COUNT)
            }
        }
        adView2.loadAd(adRequest)
        adView2.adListener = object : AdListener() {
            override fun onAdLoaded() {
                AdUserCount(DATA.FirebaseUserUid, DATA.AD_LOAD, 2)
                AdCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_LOADED_COUNT)
                AdCount(DATA.FirebaseUserUid, bannerName2, DATA.ADS_LOADED_COUNT)
            }

            override fun onAdOpened() {
                AdUserCount(DATA.FirebaseUserUid, DATA.AD_CLICK, 1)
                AdCount(DATA.FirebaseUserUid, bannerName2, DATA.ADS_CLICKED_COUNT)
            }
        }
    }

    fun AdCount(userId: String?, bannerName: String?, key: String?) {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId!!)
        ref.child(bannerName!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get views count
                var adCount = DATA.EMPTY + snapshot.child(key!!).value
                if (adCount == DATA.EMPTY || adCount == DATA.NULL) {
                    adCount = "0"
                }
                val newAdCount = adCount.toLong() + 1
                val hashMap = HashMap<String?, Any>()
                hashMap[key] = newAdCount
                val reference: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId)
                reference.child(bannerName).updateChildren(hashMap).addOnCompleteListener {
                    AdName(
                        DATA.FirebaseUserUid,
                        bannerName
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun AdUserCount(userId: String?, key: String?, number: Int) {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().getReference(DATA.USERS).child(userId!!)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get views count
                var adCount = DATA.EMPTY + snapshot.child(key!!).value
                if (adCount == DATA.EMPTY || adCount == DATA.NULL) {
                    adCount = "0"
                }
                val newAdCount = adCount.toLong() + number
                val hashMap = HashMap<String?, Any>()
                hashMap[key] = newAdCount
                val reference: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference(DATA.USERS).child(userId)
                reference.updateChildren(hashMap)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun AdName(userId: String?, bannerName: String?) {
        val ref: DatabaseReference =
            FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId!!)
        ref.child(bannerName!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get views count
                val item: ADs = snapshot.getValue(ADs::class.java)!!
                if (item.name == null) {
                    val hashMap = HashMap<String?, Any?>()
                    hashMap[DATA.NAME] = bannerName
                    val reference: DatabaseReference =
                        FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId)
                    reference.child(bannerName).updateChildren(hashMap)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun getFileExtension(uri: Uri?, context: Context): String {
        val cR: ContentResolver = context.contentResolver
        val mime: MimeTypeMap = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR.getType(uri!!))!!
    }
}