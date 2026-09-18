package com.flatcode.littlebooks.utils

import android.app.Activity
import android.app.Dialog
import android.app.ProgressDialog
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import coil.load
import coil.size.Size
import coil.transform.Transformation
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.DialogAboutAppBinding
import com.flatcode.littlebooks.databinding.DialogCloseAppBinding
import com.flatcode.littlebooks.databinding.DialogLogoutBinding
import com.flatcode.littlebooks.model.ADs
import com.flatcode.littlebooks.model.Book
import com.flatcode.littlebooks.ui.auth.AuthActivity
import com.flatcode.littlebooks.ui.book.BookEditActivity
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
import java.io.File
import java.io.FileOutputStream
import java.text.MessageFormat

// Context Extensions
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
    c: Class<*>?,
    key: String?,
    value: String?,
    key2: String?,
    value2: String?,
) {
    val intent = Intent(this, c)
    intent.putExtra(key, value)
    intent.putExtra(key2, value2)
    this.startActivity(intent)
}

fun Context.intentExtra3(
    c: Class<*>?, key: String?, value: String?,
    key2: String?, value2: String?, key3: String?, value3: String?,
) {
    val intent = Intent(this, c)
    intent.putExtra(key, value)
    intent.putExtra(key2, value2)
    intent.putExtra(key3, value3)
    this.startActivity(intent)
}

fun Context.deleteBook(
    dialogDelete: Dialog,
    publisher: String?,
    bookId: String?,
    bookUrl: String?,
    bookTitle: String,
) {
    val dialog = ProgressDialog(this)
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
                Toast.makeText(this, "Books Deleted Successfully...", Toast.LENGTH_SHORT)
                    .show()
                dialogDelete.dismiss()
                incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
            }.addOnFailureListener { e: Exception ->
                dialog.dismiss()
                Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
            }
    }.addOnFailureListener { e: Exception ->
        dialog.dismiss()
        Toast.makeText(this, "" + e.message, Toast.LENGTH_SHORT).show()
    }
}

fun Context.downloadBook(bookId: String, bookTitle: String, bookUrl: String?) {
    val nameWithExtension = "$bookTitle.pdf"

    val progressDialog = ProgressDialog(this)
    progressDialog.setTitle("Please wait")
    progressDialog.setMessage("Downloading $nameWithExtension...")
    progressDialog.setCanceledOnTouchOutside(false)
    progressDialog.show()

    val storageReference: StorageReference =
        FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
    storageReference.getBytes(DATA.MAX_BYTES_PDF.toLong())
        .addOnSuccessListener { bytes: ByteArray ->
            saveDownloadedBook(this, progressDialog, bytes, nameWithExtension, bookId)
            incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
        }.addOnFailureListener { e: Exception ->
            progressDialog.dismiss()
            Toast.makeText(
                this,
                "Failed to download due to " + e.message,
                Toast.LENGTH_SHORT
            ).show()
        }
}

fun Context.closeApp(a: Activity?) {
    val dialog = Dialog(this)
    val binding = DialogCloseAppBinding.inflate(LayoutInflater.from(this))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    binding.yes.setOnClickListener { a!!.finish() }
    binding.no.setOnClickListener { dialog.cancel() }
    dialog.show()
    dialog.window!!.attributes = lp
}

fun Context.dialogLogout() {
    val dialog = Dialog(this)
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    binding.yes.setOnClickListener {
        FirebaseAuth.getInstance().signOut()
        this.intentClear(AuthActivity::class.java)
    }
    binding.no.setOnClickListener { dialog.cancel() }
    dialog.show()
    dialog.window!!.attributes = lp
}

fun Context.shareApp() {
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "share app")
    shareIntent.putExtra(
        Intent.EXTRA_TEXT,
        " Download the app now from Google Play " + " https://play.google.com/store/apps/details?id=" + this.packageName
    )
    this.startActivity(Intent.createChooser(shareIntent, "Choose how to share"))
}

fun Context.rateApp() {
    val uri = Uri.parse("market://details?id=" + this.packageName)
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
        this.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + this.packageName)
            )
        )
    }
}

fun Context.dialogAboutApp() {
    val dialog = Dialog(this)
    val binding = DialogAboutAppBinding.inflate(LayoutInflater.from(this))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    binding.website.setOnClickListener {
        this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DATA.WEB_SITE)))
    }
    binding.facebook.setOnClickListener {
        val openFacebookIntent = try {
            this.packageManager.getPackageInfo("com.facebook.katana", 0)
            Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/" + DATA.FB_ID))
        } catch (e: Exception) {
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/" + DATA.FB_ID))
        }
        this.startActivity(openFacebookIntent)
    }
    dialog.show()
    dialog.window!!.attributes = lp
}

fun Context.moreOptionDialog(item: Book?) {
    val bookId = item!!.id
    val bookUrl = item.url
    val bookTitle = item.title
    val publisher = item.publisher

    val options = arrayOf("Edit", "Delete")

    val builder = AlertDialog.Builder(this)
    builder.setTitle("Choose Options").setItems(options) { _, which ->
        if (which == 0) {
            this.intentExtra(BookEditActivity::class.java, DATA.BOOK_ID, bookId)
        } else if (which == 1) {
            this.dialogOptionDelete(
                DATA.EMPTY + publisher, DATA.EMPTY + bookId,
                DATA.EMPTY + bookUrl, DATA.EMPTY + bookTitle
            )
        }
    }.show()
}

fun Context.dialogOptionDelete(
    publisher: String?, bookId: String?, bookUrl: String?, bookTitle: String,
) {
    val dialog = Dialog(this)
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(binding.root)
    dialog.setCancelable(true)
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    val lp: WindowManager.LayoutParams = WindowManager.LayoutParams()
    lp.copyFrom(dialog.window!!.attributes)
    lp.width = WindowManager.LayoutParams.WRAP_CONTENT
    lp.height = WindowManager.LayoutParams.WRAP_CONTENT
    binding.title.setText(R.string.do_you_want_to_delete_the_book)
    binding.yes.setOnClickListener {
        this.deleteBook(dialog, publisher, bookId, bookUrl, bookTitle)
    }
    binding.no.setOnClickListener { dialog.dismiss() }
    dialog.show()
    dialog.window!!.attributes = lp
}

// Activity Extensions
fun Activity.cropImageSquare() {
    CropImage.activity()
        .setGuidelines(CropImageView.Guidelines.ON)
        .setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIN_SQUARE, DATA.MIN_SQUARE)
        .setAspectRatio(1, 1)
        .setCropShape(CropImageView.CropShape.OVAL)
        .start(this)
}

// ImageView Extensions
fun ImageView.glide(isUser: Boolean, url: String?) {
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
    } catch (e: Exception) {
        this.setImageResource(R.drawable.basic_book)
    }
}

fun ImageView.glideBlur(isUser: Boolean, url: String, level: Int) {
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
                transformations(SimpleBlurTransformation(level.toFloat()))
            }
        }
    } catch (e: Exception) {
        this.setImageResource(R.drawable.basic_book)
    }
}

fun ImageView.isFavorite(id: String?, userId: String?) {
    val reference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(userId!!)
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

        override fun onCancelled(databaseError: DatabaseError) {}
    })
}

fun ImageView.checkFavorite(bookId: String?) {
    if (this.tag == "add") {
        FirebaseDatabase.getInstance().getReference(DATA.FAVORITES)
            .child(DATA.FirebaseUserUid)
            .child(bookId!!).setValue(true)
    } else {
        FirebaseDatabase.getInstance()
            .getReference(DATA.FAVORITES).child(DATA.FirebaseUserUid)
            .child(bookId!!).removeValue()
    }
}

fun ImageView.checkLove(bookId: String?) {
    if (this.tag == "love") {
        FirebaseDatabase.getInstance().getReference(DATA.LOVES).child(bookId!!)
            .child(DATA.FirebaseUserUid).setValue(true)
        incrementItemCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    } else {
        FirebaseDatabase.getInstance().getReference(DATA.LOVES).child(bookId!!)
            .child(DATA.FirebaseUserUid).removeValue()
        incrementItemRemoveCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    }
}

fun ImageView.isLoves(bookId: String?) {
    val reference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
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

// TextView Extensions
fun TextView.loadPdfInfo(pdfUrl: String?) {
    val ref: StorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl!!)
    ref.metadata.addOnSuccessListener { storageMetadata: StorageMetadata ->
        val bytes: Double = storageMetadata.sizeBytes.toDouble()
        val kb = bytes / 1024
        val mb = kb / 1024
        if (mb > 1) this.text = String.format("%.2f", mb) + " MB"
        else if (kb > 1) this.text = String.format("%.2f", mb) + " MB"
        else this.text = String.format("%.2f", bytes) + " bytes"
    }
}

fun TextView.loadCategory(categoryId: String?) {
    val ref: DatabaseReference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
    ref.child(categoryId!!).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val categoryName = DATA.EMPTY + snapshot.child(DATA.CATEGORY).value
            this@loadCategory.text = categoryName
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun TextView.nrLoves(bookId: String?) {
    val reference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            this@nrLoves.text = MessageFormat.format(" {0} ", dataSnapshot.childrenCount)
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    })
}

// AdView Extensions
fun AdView.bannerAd(context: Context, bannerName: String?) {
    MobileAds.initialize(context) { }
    val adRequest = AdRequest.Builder().build()
    this.loadAd(adRequest)
    this.adListener = object : AdListener() {
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

fun Context.bannerAdTwo(
    adView: AdView,
    bannerName: String?,
    adView2: AdView,
    bannerName2: String?,
) {
    MobileAds.initialize(this) { }
    val adRequest = AdRequest.Builder().build()
    adView.loadAd(adRequest)
    adView.adListener = object : AdListener() {
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

// Other Top-level Utilities
fun incrementItemCount(database: String?, id: String?, childDB: String?) {
    val ref = FirebaseDatabase.getInstance().getReference(database!!)
    ref.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
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
        val filePath = downloadsFolder.path + "/" + nameWithExtension
        val out = FileOutputStream(filePath)
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

fun AdCount(userId: String?, bannerName: String?, key: String?) {
    val ref: DatabaseReference =
        FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId!!)
    ref.child(bannerName!!).addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
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
                AdName(DATA.FirebaseUserUid, bannerName)
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

fun Uri.getFileExtension(context: Context): String {
    val cR: ContentResolver = context.contentResolver
    val mime: MimeTypeMap = MimeTypeMap.getSingleton()
    return mime.getExtensionFromMimeType(cR.getType(this))!!
}

// Transformation
class SimpleBlurTransformation(private val radius: Float) : Transformation {
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