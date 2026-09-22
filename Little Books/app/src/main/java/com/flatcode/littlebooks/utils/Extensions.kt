package com.flatcode.littlebooks.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.text.format.DateFormat
import android.widget.ImageView
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import coil.load
import coil.size.Size
import coil.transform.Transformation
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooks.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Locale
import kotlin.coroutines.resume

inline fun <reified T : Activity> Context.openActivity(
    clear: Boolean = false, vararg extras: Pair<String, Any?>
) = openActivity(T::class.java, clear, *extras)

fun Context.openActivity(
    activity: Class<*>, clear: Boolean = false, vararg extras: Pair<String, Any?>
) {
    val intent = Intent(this, activity).apply {
        if (clear) addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        extras.forEach { (k, v) ->
            when (v) {
                is String -> putExtra(k, v)
                is Int -> putExtra(k, v)
                is Boolean -> putExtra(k, v)
                is Long -> putExtra(k, v)
                is Double -> putExtra(k, v)
                is Serializable -> putExtra(k, v)
            }
        }
    }
    startActivity(intent)
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
    } catch (_: ActivityNotFoundException) {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("http://play.google.com/store/apps/details?id=" + this.packageName)
            )
        )
    }
}

suspend fun cloudinaryUpload(uri: Uri): Resource<String> =
    suspendCancellableCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .option("upload_preset", DATA.CLOUDINARY_UPLOAD_PRESET)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String ?: resultData["url"] as String
                    continuation.resume(Resource.Success(url))
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resume(Resource.Error(error.description))
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }

fun ImageView.loadImage(isUser: Boolean, url: String?) {
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

fun ImageView.loadImageBlur(isUser: Boolean, url: String, level: Int) {
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

fun TextView.loadPdfInfo(pdfUrl: String?) {
    pdfUrl ?: return
    text = "PDF Book"
    @Suppress("DEPRECATION")
    GlobalScope.launch(Dispatchers.IO) {
        try {
            val url = URL(pdfUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "HEAD"
            val bytes = connection.contentLengthLong.toDouble()
            if (bytes > 0) {
                val kb = bytes / 1024
                val mb = kb / 1024
                withContext(Dispatchers.Main) {
                    text = if (mb > 1) "%.2f MB".format(mb)
                    else if (kb > 1) "%.2f KB".format(kb)
                    else "$bytes bytes"
                }
            }
        } catch (_: Exception) {
            // ignore
        }
    }
}

fun AdView.loadBannerAd(context: Context, bannerName: String?) {
    MobileAds.initialize(context) { }
    val adRequest = AdRequest.Builder().build()
    this.loadAd(adRequest)
    this.adListener = object : AdListener() {
        override fun onAdLoaded() {
            adUserCount(DATA.FirebaseUserUid, DATA.AD_LOAD, 1)
            adCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_LOADED_COUNT)
        }

        override fun onAdOpened() {
            adUserCount(DATA.FirebaseUserUid, DATA.AD_CLICK, 1)
            adCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_CLICKED_COUNT)
        }
    }
}

fun Context.loadBannerAdTwo(
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
            adUserCount(DATA.FirebaseUserUid, DATA.AD_CLICK, 1)
            adCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_CLICKED_COUNT)
        }
    }
    adView2.loadAd(adRequest)
    adView2.adListener = object : AdListener() {
        override fun onAdLoaded() {
            adUserCount(DATA.FirebaseUserUid, DATA.AD_LOAD, 2)
            adCount(DATA.FirebaseUserUid, bannerName, DATA.ADS_LOADED_COUNT)
            adCount(DATA.FirebaseUserUid, bannerName2, DATA.ADS_LOADED_COUNT)
        }

        override fun onAdOpened() {
            adUserCount(DATA.FirebaseUserUid, DATA.AD_CLICK, 1)
            adCount(DATA.FirebaseUserUid, bannerName2, DATA.ADS_CLICKED_COUNT)
        }
    }
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
