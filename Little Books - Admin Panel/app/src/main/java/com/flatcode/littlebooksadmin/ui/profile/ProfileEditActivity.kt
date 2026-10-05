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
                viewModel.user.collect { user ->
                    user?.let {
                        binding.nameEt.setText(it.username)
                        if (imageUri == null) {
                            binding.profileImage.loadImage(
                                isUser = true, data = it.profileImage ?: DATA.BASIC
                            )
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
            dialog = AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.updating_user_profile))
            }.show()
            viewModel.updateProfile(username, imageUri) { success, message ->
                dialog?.dismiss()
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                if (success) finish()
            }
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
                    binding.profileImage.loadImage(isUser = true, data = imageUri)
                }
            }
        }
    }
}