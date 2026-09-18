package com.flatcode.littlebooksadmin.ui.category

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.utils.*
import com.flatcode.littlebooksadmin.databinding.ActivityCategoryAddBinding
import com.flatcode.littlebooksadmin.ui.category.CategoryAddViewModel
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    private var activity: Activity? = null
    private var context: Context = also { activity = it }
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null
    
    private val viewModel: CategoryAddViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding.toolbar.nameSpace.setText(R.string.add_new_category)
        binding.toolbar.back.setOnClickListener { onBackPressed() }

        binding.image.setOnClickListener { activity?.cropImageSquare() }
        binding.toolbar.ok.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            dialog!!.setMessage("Uploading Category...")
                            dialog!!.show()
                        }
                        is Resource.Success -> {
                            dialog!!.dismiss()
                            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        is Resource.Error -> {
                            dialog!!.dismiss()
                            Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    private fun validateData() {
        val title = binding.categoryEt.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(context, "Pick Image...", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addCategory(
                title,
                imageUri,
                context.getFileExtension(imageUri)
            )
        }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = CropImage.getPickImageResultUri(context, data)
            if (CropImage.isReadExternalStoragePermissionsRequired(context, uri)) {
                imageUri = uri
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            } else {
                activity?.cropImageSquare()
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri
                binding.image.setImageURI(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this, "Error! $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}


