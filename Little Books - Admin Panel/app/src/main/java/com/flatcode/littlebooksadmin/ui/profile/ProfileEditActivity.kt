package com.flatcode.littlebooksadmin.ui.profile

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityProfileEditBinding
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Dialogs
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.utils.loadImage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private var activity: Activity? = null
    private val context: Context = also { activity = it as Activity }
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val viewModel: ProfileViewModel by viewModels()

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            binding.profileImage.setImageURI(imageUri)
        } else {
            val exception = result.error
            exception?.let {
                Toast.makeText(
                    context, getString(R.string.error_message, it.message), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startCrop() {
        cropImage.launch(
            CropImageContractOptions(
                uri = null, cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    fixAspectRatio = true,
                    minCropResultWidth = DATA.MIX_SQUARE,
                    minCropResultHeight = DATA.MIX_SQUARE,
                    cropShape = CropImageView.CropShape.RECTANGLE
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initUI()
        observeViewModel()

        viewModel.loadProfile(DATA.FirebaseUserUid)
    }

    private fun initUI() {
        dialog = Dialogs.createProgressDialog(context, getString(R.string.please_wait))

        binding.toolbar.nameSpace.setText(R.string.edit_profile)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.image.setOnClickListener { startCrop() }
        binding.go.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.user.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                resource.data?.let { user ->
                                    binding.nameEt.setText(user.username)
                                    binding.profileImage.loadImage(
                                        isUser = true, url = user.profileImage ?: DATA.BASIC
                                    )
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
                                dialog = Dialogs.createProgressDialog(
                                    context, getString(R.string.updating_user_profile)
                                )
                                dialog!!.show()
                            }

                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(
                                    context, R.string.profile_updated, Toast.LENGTH_SHORT
                                ).show()
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
        val username = binding.nameEt.text.toString().trim()
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, R.string.enter_name, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateProfile(
                username, imageUri
            )
        }
    }
}




