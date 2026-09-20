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
import android.text.format.DateFormat
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
import androidx.viewbinding.ViewBinding
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
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import java.io.File
import java.io.FileOutputStream
import java.io.Serializable
import java.text.MessageFormat
import java.util.Calendar
import java.util.Locale

inline fun <reified T : Activity> Context.openActivity(
    clear: Boolean = false, vararg extras: Pair<String, Any?>
) {
    val intent = Intent(this, T::class.java).apply {
        if (clear) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        extras.forEach { (key, value) ->
            when (value) {
                is String -> putExtra(key, value)
                is Int -> putExtra(key, value)
                is Boolean -> putExtra(key, value)
                is Serializable -> putExtra(key, value)
            }
        }
    }
    startActivity(intent)
}

private fun Context.showCustomDialog(binding: ViewBinding, setup: (Dialog) -> Unit) {
    Dialog(this).apply {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(binding.root)
        setCancelable(true)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

fun Context.deleteBook(
    dialogDelete: Dialog,
    publisher: String?,
    bookId: String?,
    bookUrl: String?,
    bookTitle: String,
) {
    val dialog = ProgressDialog(this).apply {
        setTitle("Please wait")
        setMessage("Deleting $bookTitle ...")
        show()
    }
    FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!).delete().addOnSuccessListener {
        FirebaseDatabase.getInstance().getReference(DATA.BOOKS).child(bookId!!).removeValue()
            .addOnSuccessListener {
                dialog.dismiss()
                Toast.makeText(this, "Books Deleted Successfully...", Toast.LENGTH_SHORT).show()
                dialogDelete.dismiss()
                incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
            }.addOnFailureListener { e ->
                dialog.dismiss()
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }.addOnFailureListener { e ->
        dialog.dismiss()
        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
    }
}

fun Context.downloadBook(bookId: String, bookTitle: String, bookUrl: String?) {
    val nameWithExtension = "$bookTitle.pdf"
    val progressDialog = ProgressDialog(this).apply {
        setTitle("Please wait")
        setMessage("Downloading $nameWithExtension...")
        setCanceledOnTouchOutside(false)
        show()
    }

    FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl!!)
        .getBytes(DATA.MAX_BYTES_PDF.toLong()).addOnSuccessListener { bytes ->
            saveDownloadedBook(this, progressDialog, bytes, nameWithExtension, bookId)
            incrementItemCount(DATA.BOOKS, bookId, DATA.DOWNLOADS_COUNT)
        }.addOnFailureListener { e ->
            progressDialog.dismiss()
            Toast.makeText(this, "Failed to download due to ${e.message}", Toast.LENGTH_SHORT)
                .show()
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
    val binding = DialogAboutAppBinding.inflate(LayoutInflater.from(this))
    showCustomDialog(binding) {
        binding.website.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(DATA.WEB_SITE)))
        }
        binding.facebook.setOnClickListener {
            val fbUri = try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                Uri.parse("fb://profile/${DATA.FB_ID}")
            } catch (e: Exception) {
                Uri.parse("https://www.facebook.com/${DATA.FB_ID}")
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
        else if (which == 1) dialogOptionDelete(item.publisher, item.id, item.url, item.title!!)
    }.show()
}

fun Context.dialogOptionDelete(
    publisher: String?, bookId: String?, bookUrl: String?, bookTitle: String,
) {
    val binding = DialogLogoutBinding.inflate(LayoutInflater.from(this))
    showCustomDialog(binding) { dialog ->
        binding.title.setText(R.string.do_you_want_to_delete_the_book)
        binding.yes.setOnClickListener {
            deleteBook(dialog, publisher, bookId, bookUrl, bookTitle)
        }
        binding.no.setOnClickListener { dialog.dismiss() }
    }
}

fun Activity.cropImageSquare() {
    CropImage.activity().setGuidelines(CropImageView.Guidelines.ON).setMultiTouchEnabled(true)
        .setMinCropResultSize(DATA.MIN_SQUARE, DATA.MIN_SQUARE).setAspectRatio(1, 1)
        .setCropShape(CropImageView.CropShape.OVAL).start(this)
}

fun ImageView.glide(isUser: Boolean, url: String?) {
    val placeholder = if (isUser) R.drawable.basic_user else R.drawable.basic_book
    if (url == DATA.BASIC || url == null) {
        setImageResource(placeholder)
    } else {
        load(url) {
            placeholder(R.color.image_profile)
            error(placeholder)
            crossfade(true)
        }
    }
}

fun ImageView.glideBlur(isUser: Boolean, url: String, level: Int) {
    val placeholder = if (isUser) R.drawable.basic_user else R.drawable.basic_book
    if (url == DATA.BASIC) {
        setImageResource(placeholder)
    } else {
        load(url) {
            placeholder(R.color.image_profile)
            error(placeholder)
            transformations(SimpleBlurTransformation(level.toFloat()))
        }
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
        FirebaseDatabase.getInstance().getReference(DATA.FAVORITES).child(DATA.FirebaseUserUid)
            .child(bookId!!).setValue(true)
    } else {
        FirebaseDatabase.getInstance().getReference(DATA.FAVORITES).child(DATA.FirebaseUserUid)
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

fun TextView.loadPdfInfo(pdfUrl: String?) {
    pdfUrl ?: return
    FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl).metadata.addOnSuccessListener {
        val bytes = it.sizeBytes.toDouble()
        val kb = bytes / 1024
        val mb = kb / 1024
        text = if (mb > 1) "%.2f MB".format(mb)
        else if (kb > 1) "%.2f KB".format(kb)
        else "$bytes bytes"
    }
}

fun TextView.loadCategory(categoryId: String?) {
    categoryId ?: return
    FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(categoryId)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                text = snapshot.child(DATA.CATEGORY).value.toString()
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

fun incrementItemCount(database: String?, id: String?, childDB: String?) =
    updateItemCount(database, id, childDB, 1)

fun incrementItemRemoveCount(database: String?, id: String?, childDB: String?) =
    updateItemCount(database, id, childDB, -1)

private fun updateItemCount(database: String?, id: String?, childDB: String?, increment: Int) {
    if (database == null || id == null || childDB == null) return
    val ref = FirebaseDatabase.getInstance().getReference(database).child(id)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.child(childDB).value.toString().toIntOrNull() ?: 0
            ref.updateChildren(mapOf(childDB to (count + increment).coerceAtLeast(0)))
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
            context, "Failed saving to Download Folder due to " + e.message, Toast.LENGTH_SHORT
        ).show()
        progressDialog.dismiss()
    }
}

fun AdCount(userId: String?, bannerName: String?, key: String?) {
    if (userId == null || bannerName == null || key == null) return
    val ref = FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId).child(bannerName)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.child(key).value.toString().toLongOrNull() ?: 0L
            ref.updateChildren(mapOf(key to count + 1)).addOnCompleteListener {
                AdName(DATA.FirebaseUserUid, bannerName)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun AdUserCount(userId: String?, key: String?, number: Int) {
    if (userId == null || key == null) return
    val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS).child(userId)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.child(key).value.toString().toLongOrNull() ?: 0L
            ref.updateChildren(mapOf(key to count + number))
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun AdName(userId: String?, bannerName: String?) {
    if (userId == null || bannerName == null) return
    val ref = FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId).child(bannerName)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.getValue(ADs::class.java)?.name == null) {
                ref.updateChildren(mapOf(DATA.NAME to bannerName))
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

fun Long.formatTimestamp(): String {
    val calendar = Calendar.getInstance(Locale.ENGLISH)
    calendar.timeInMillis = this
    return DateFormat.format("dd/MM/yyyy", calendar).toString()
}

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