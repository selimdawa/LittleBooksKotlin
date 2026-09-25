package com.flatcode.littlebooksadmin.utils

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.IntentCompat
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityCropBinding

class CropActivity : BaseActivity() {

    private lateinit var binding: ActivityCropBinding
    private var imageUri: Uri? = null
    private var aspectRatioX = 1
    private var aspectRatioY = 1
    private var isOval = false
    private var minWidth = 500
    private var minHeight = 500

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageUri = IntentCompat.getParcelableExtra(intent, "IMAGE_URI", Uri::class.java)
        aspectRatioX = intent.getIntExtra("ASPECT_RATIO_X", 1)
        aspectRatioY = intent.getIntExtra("ASPECT_RATIO_Y", 1)
        isOval = intent.getBooleanExtra("IS_OVAL", false)
        minWidth = intent.getIntExtra("MIN_WIDTH", 500)
        minHeight = intent.getIntExtra("MIN_HEIGHT", 500)

        if (imageUri == null) {
            Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupCropView()

        binding.close.setOnClickListener { finish() }
        binding.done.setOnClickListener { cropImage() }
    }

    private fun setupCropView() {
        val options = CropImageOptions(
            guidelines = CropImageView.Guidelines.ON,
            multiTouchEnabled = true,
            aspectRatioX = aspectRatioX,
            aspectRatioY = aspectRatioY,
            fixAspectRatio = true,
            cropShape = if (isOval) CropImageView.CropShape.OVAL else CropImageView.CropShape.RECTANGLE,
            minCropResultWidth = minWidth,
            minCropResultHeight = minHeight
        )
        binding.cropImageView.setImageCropOptions(options)
        binding.cropImageView.setImageUriAsync(imageUri)
    }

    private fun cropImage() {
        binding.cropImageView.setOnCropImageCompleteListener { _, result ->
            if (result.isSuccessful) {
                val resultUri = result.uriContent
                val intent = Intent()
                intent.putExtra("CROP_RESULT_URI", resultUri)
                setResult(RESULT_OK, intent)
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.crop_failed, result.error?.message),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
        binding.cropImageView.croppedImageAsync()
    }
}