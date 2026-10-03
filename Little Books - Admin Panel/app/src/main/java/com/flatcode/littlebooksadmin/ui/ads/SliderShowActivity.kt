package com.flatcode.littlebooksadmin.ui.ads

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.IntentCompat
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivitySliderShowBinding
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.cropImage
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.pickImage
import com.flatcode.littlebooksadmin.utils.requestStorage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SliderShowActivity : BaseActivity() {

    private lateinit var binding: ActivitySliderShowBinding
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null
    private var imageNumber = 0
    private var item = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySliderShowBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.nameSpace.setText(R.string.slider_show)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        val addButtons = listOf(
            binding.addOne,
            binding.addTwo,
            binding.addThree,
            binding.addFour,
            binding.addFive,
            binding.addSix,
            binding.addSeven,
            binding.addEight,
            binding.addNine,
            binding.addTeen,
            binding.addEleven,
            binding.addTwelfth,
            binding.addThirteen,
            binding.addFourteenth,
            binding.addFifteenth,
            binding.addSixteen,
            binding.addSeventeen,
            binding.addEighteen,
            binding.addNineteen,
            binding.addTwenty
        )

        addButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                requestStorage(DATA.MIX_SLIDER_X) {
                    pickImage(DATA.MIX_SLIDER_X)
                    imageNumber = index + 1
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == DATA.MIX_SLIDER_X) {
                pickImage(DATA.MIX_SLIDER_X)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DATA.MIX_SLIDER_X && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                cropImage(
                    uri = uri,
                    aspectRatioX = 16,
                    aspectRatioY = 9,
                    isOval = true,
                    minWidth = DATA.MIX_SLIDER_X,
                    minHeight = DATA.MIX_SLIDER_Y,
                    requestCode = DATA.MIX_SLIDER_X
                )
            } else {
                val resultUri =
                    IntentCompat.getParcelableExtra(data, "CROP_RESULT_URI", Uri::class.java)
                if (resultUri != null) {
                    imageUri = resultUri
                    uploadImage(DATA.EMPTY + imageNumber)
                }
            }
        }
    }

    private val nrSliderShow: Unit
        get() {
            val reference = FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
            reference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    item = dataSnapshot.childrenCount.toInt()
                    binding.toolbar.nameSpace.text = getString(R.string.slider_show_count, item)

                    val linearLayouts = listOf(
                        binding.linearOne,
                        binding.linearTwo,
                        binding.linearThree,
                        binding.linearFour,
                        binding.linearFive,
                        binding.linearSix,
                        binding.linearSeven,
                        binding.linearEight,
                        binding.linearNine,
                        binding.linearTeen,
                        binding.linearEleven,
                        binding.linearTwelfth,
                        binding.linearThirteen,
                        binding.linearFourteenth,
                        binding.linearFifteenth,
                        binding.linearSixteen,
                        binding.linearSeventeen,
                        binding.linearEighteen,
                        binding.linearNineteen,
                        binding.linearTwenty
                    )

                    linearLayouts.forEachIndexed { index, linearLayout ->
                        linearLayout.visibility = if (item >= index) View.VISIBLE else View.GONE
                    }
                    binding.bar.visibility = View.GONE
                }

                override fun onCancelled(databaseError: DatabaseError) {}
            })
        }

    private fun sliderShow() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val imageViews = listOf(
                    binding.imageOne,
                    binding.imageTwo,
                    binding.imageThree,
                    binding.imageFour,
                    binding.imageFive,
                    binding.imageSix,
                    binding.imageSeven,
                    binding.imageEight,
                    binding.imageNine,
                    binding.imageTeen,
                    binding.imageEleven,
                    binding.imageTwelfth,
                    binding.imageThirteen,
                    binding.imageFourteenth,
                    binding.imageFifteenth,
                    binding.imageSixteen,
                    binding.imageSeventeen,
                    binding.imageEighteen,
                    binding.imageNineteen,
                    binding.imageTwenty
                )

                imageViews.forEachIndexed { index, imageView ->
                    val url = DATA.EMPTY + dataSnapshot.child((index + 1).toString()).value
                    imageView.loadImage(isUser = false, url = url)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun uploadImage(name: String) {
        dialog = AlertDialog.Builder(this).apply {
            setMessage(getString(R.string.posting_photo))
        }.show()
        MediaManager.get().upload(imageUri).unsigned(DATA.CLOUDINARY_UPLOAD_PRESET)
            .option("folder", "Images/SliderShow/").option("public_id", name)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val uploadedImageUrl = resultData?.get("secure_url") as? String
                    updateImage(uploadedImageUrl!!, DATA.EMPTY + name)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    dialog?.dismiss()
                    Toast.makeText(
                        this@SliderShowActivity,
                        getString(R.string.error_message, error?.description),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun updateImage(imageUrl: String, name: String) {
        val hashMap = HashMap<String, Any>()
        if (imageUri != null) {
            hashMap[DATA.EMPTY + name] = DATA.EMPTY + imageUrl
        }
        val reference = FirebaseDatabase.getInstance().getReference(DATA.SLIDER_SHOW)
        reference.updateChildren(hashMap).addOnSuccessListener {
            dialog?.dismiss()
            Toast.makeText(this, R.string.photo_posted, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            dialog?.dismiss()
            Toast.makeText(
                this, getString(R.string.error_message, e.message), Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        nrSliderShow
        sliderShow()
        super.onResume()
    }

    override fun onRestart() {
        nrSliderShow
        sliderShow()
        super.onRestart()
    }
}