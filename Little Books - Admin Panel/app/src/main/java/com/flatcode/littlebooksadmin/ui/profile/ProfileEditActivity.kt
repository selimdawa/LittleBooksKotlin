package com.flatcode.littlebooksadmin.ui.profile

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityProfileEditBinding
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.utils.cropImage
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.pickImage
import com.flatcode.littlebooksadmin.utils.requestStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileEditActivity : BaseActivity() {

    private lateinit var binding: ActivityProfileEditBinding
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        observeViewModel()

        viewModel.loadProfile(DATA.FirebaseUserUid)
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.edit_profile)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.image.setOnClickListener {
            requestStorage(DATA.MIX_SQUARE) {
                pickImage(DATA.MIX_SQUARE)
            }
        }
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
                                    if (imageUri == null) {
                                        binding.profileImage.loadImage(
                                            isUser = true, url = user.profileImage ?: DATA.BASIC
                                        )
                                    }
                                }
                            }

                            is Resource.Error -> {
                                Toast.makeText(
                                    this@ProfileEditActivity, resource.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.updateState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                dialog = AlertDialog.Builder(this@ProfileEditActivity).apply {
                                    setMessage(getString(R.string.updating_user_profile))
                                }.show()
                            }

                            is Resource.Success -> {
                                dialog?.dismiss()
                                Toast.makeText(
                                    this@ProfileEditActivity,
                                    R.string.profile_updated,
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            is Resource.Error -> {
                                dialog?.dismiss()
                                Toast.makeText(
                                    this@ProfileEditActivity, resource.message, Toast.LENGTH_SHORT
                                ).show()
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
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.updateProfile(username, imageUri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == DATA.MIX_SQUARE) {
                pickImage(DATA.MIX_SQUARE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DATA.MIX_SQUARE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                cropImage(
                    uri = uri,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    isOval = true,
                    minWidth = DATA.MIX_SQUARE,
                    minHeight = DATA.MIX_SQUARE,
                    requestCode = DATA.MIX_SQUARE
                )
            } else {
                val resultUri =
                    IntentCompat.getParcelableExtra(data, "CROP_RESULT_URI", Uri::class.java)
                if (resultUri != null) {
                    imageUri = resultUri
                    binding.profileImage.setImageURI(imageUri)
                }
            }
        }
    }
}
