package com.flatcode.littlebooks.ui.profile

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import com.flatcode.littlebooks.utils.PermissionUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityProfileEditBinding
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.glide
import com.flatcode.littlebooks.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditActivity : AppCompatActivity() {

    private var binding: ActivityProfileEditBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null

    private val viewModel: ProfileViewModel by viewModels()

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            binding!!.image.setImageURI(imageUri)
        } else {
            val error = result.error
            Toast.makeText(this, "Error! $error", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 10
            }
            insets
        }

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.nameSpace.setText(R.string.edit_profile)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.image.setOnClickListener { startCrop() }
        binding!!.go.setOnClickListener { validateData() }

        observeViewModel()
        loadUserInfo()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                startCrop()
            } else {
                Toast.makeText(context, "Permission denied...", Toast.LENGTH_SHORT).show()
            }
        }

    private fun startCrop() {
        if (PermissionUtils.checkStoragePermission(context)) {
            cropImage.launch(
                CropImageContractOptions(
                    uri = null,
                    cropImageOptions = CropImageOptions(
                        guidelines = CropImageView.Guidelines.ON,
                        multiTouchEnabled = true,
                        minCropResultWidth = DATA.MIN_SQUARE,
                        minCropResultHeight = DATA.MIN_SQUARE,
                        aspectRatioX = 1,
                        aspectRatioY = 1,
                        fixAspectRatio = true,
                        cropShape = CropImageView.CropShape.OVAL
                    )
                )
            )
        } else {
            requestPermissionLauncher.launch(PermissionUtils.storagePermission)
        }
    }

    private fun loadUserInfo() {
        DATA.FirebaseUserUid?.let {
            viewModel.loadProfileData(it, it, DATA.FOLLOWERS, DATA.FOLLOWING)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        if (resource is Resource.Success) {
                            val user = resource.data
                            binding!!.image.glide(true, user?.profileImage)
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
        username = binding!!.nameEt.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateProfile(null)
            } else {
                viewModel.uploadProfileImage(DATA.FirebaseUserUid, imageUri!!, context)
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
