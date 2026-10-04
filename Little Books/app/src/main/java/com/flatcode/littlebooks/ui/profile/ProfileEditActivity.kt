package com.flatcode.littlebooks.ui.profile

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityProfileEditBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.PermissionUtils
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.isNetworkAvailable
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.startCropActivity
import com.flatcode.littlebooks.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditActivity : BaseActivity() {

    private var binding: ActivityProfileEditBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val viewModel: ProfileViewModel by viewModels()

    private val cropImageLauncher =
        registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                imageUri = result.data?.let { intent ->
                    IntentCompat.getParcelableExtra(intent, "CROP_RESULT_URI", Uri::class.java)
                }
                binding!!.image.setImageURI(null)
                binding!!.image.setImageURI(imageUri)
            }
        }

    private val pickImageLauncher =
        registerForActivityResult(GetContent()) { uri: Uri? ->
            uri?.let {
                cropImageLauncher.launch(context.startCropActivity(it, 1, 1, true))
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickImageLauncher.launch("image/*")
            } else {
                Toast.makeText(context, "Permission denied...", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        dialog = AlertDialog.Builder(context)
            .setTitle("Please wait")
            .setMessage("...")
            .setCancelable(false)
            .create()

        binding!!.toolbar.nameSpace.setText(R.string.edit_profile)
        binding!!.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding!!.image.setOnClickListener { pickImageGallery() }
        binding!!.go.setOnClickListener { validateData() }

        observeViewModel()
        loadUserInfo()
    }

    private fun pickImageGallery() {
        if (PermissionUtils.checkStoragePermission(context)) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(PermissionUtils.storagePermission)
        }
    }

    private fun loadUserInfo() {
        viewModel.loadProfileData(DATA.FirebaseUserUid, DATA.FirebaseUserUid, DATA.FOLLOWERS, DATA.FOLLOWING)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        if (resource is Resource.Success) {
                            val user = resource.data
                            if (imageUri == null) {
                                binding!!.image.loadImage(true, user?.profileImage)
                            }
                            binding!!.nameEt.setText(user?.username)
                        }
                    }
                }
                launch {
                    viewModel.uploadStatus.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                updateProfile(resource.data)
                            }
                            is Resource.Error -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            is Resource.Loading -> {
                                dialog!!.setMessage("Uploading Image...")
                                dialog!!.show()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }

    private var username = DATA.EMPTY
    private fun validateData() {
        username = binding!!.nameEt.text.toString().trim()
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else if (!isNetworkAvailable()) {
            Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateProfile(null)
            } else {
                viewModel.uploadProfileImage(imageUri!!)
            }
        }
    }

    private fun updateProfile(imageUrl: String?) {
        dialog!!.setMessage("Updating user profile...")
        dialog!!.show()
        val hashMap = HashMap<String, Any>()
        hashMap[DATA.USER_NAME] = DATA.EMPTY + username
        if (imageUrl != null) {
            hashMap[DATA.PROFILE_IMAGE] = DATA.EMPTY + imageUrl
        }
        
        lifecycleScope.launch {
            viewModel.updateUserInfo(DATA.FirebaseUserUid, hashMap)
            dialog!!.dismiss()
            Toast.makeText(context, "Profile updated...", Toast.LENGTH_SHORT).show()
        }
    }
}