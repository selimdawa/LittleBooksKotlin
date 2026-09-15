package com.flatcode.littlebooksadmin.Activity

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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.data.util.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityProfileEditBinding
import com.flatcode.littlebooksadmin.ui.viewmodel.ProfileViewModel
import com.theartofdev.edmodo.cropper.CropImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditActivity : AppCompatActivity() {

    private var binding: ActivityProfileEditBinding? = null
    private var activity: Activity? = null
    private val context: Context = also { activity = it as Activity }
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null
    
    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        initUI()
        observeViewModel()
        
        viewModel.loadProfile(DATA.FirebaseUserUid)
    }

    private fun initUI() {
        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.nameSpace.setText(R.string.edit_profile)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.image.setOnClickListener { VOID.cropImageSquare(activity) }
        binding!!.go.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    binding!!.nameEt.setText(user.username)
                                    VOID.Glide(true, context, user.profileImage ?: DATA.BASIC, binding!!.profileImage)
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.updateState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                dialog!!.setMessage("Updating user profile...")
                                dialog!!.show()
                            }
                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, "Profile updated...", Toast.LENGTH_SHORT).show()
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
    }

    private fun validateData() {
        val username = binding!!.nameEt.text.toString().trim()
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateProfile(
                username,
                imageUri,
                if (imageUri != null) VOID.getFileExtension(imageUri, context) else null
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
                VOID.cropImageSquare(activity)
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri
                binding!!.profileImage.setImageURI(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this, "Error! $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
