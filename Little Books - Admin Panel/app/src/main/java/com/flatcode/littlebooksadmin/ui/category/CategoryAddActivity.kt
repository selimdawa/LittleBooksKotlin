package com.flatcode.littlebooksadmin.ui.category

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
import com.flatcode.littlebooksadmin.databinding.ActivityCategoryAddBinding
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.utils.cropImage
import com.flatcode.littlebooksadmin.utils.pickImage
import com.flatcode.littlebooksadmin.utils.requestStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryAddActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val viewModel: CategoryAddViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.add_new_category)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.image.setOnClickListener {
            requestStorage(DATA.MIX_SQUARE) {
                pickImage(DATA.MIX_SQUARE)
            }
        }
        binding.toolbar.ok.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.addState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            dialog = AlertDialog.Builder(this@CategoryAddActivity).apply {
                                setMessage(getString(R.string.uploading_category))
                            }.show()
                        }

                        is Resource.Success -> {
                            dialog?.dismiss()
                            Toast.makeText(
                                this@CategoryAddActivity,
                                R.string.successfully_uploaded,
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }

                        is Resource.Error -> {
                            dialog?.dismiss()
                            Toast.makeText(
                                this@CategoryAddActivity, resource.message, Toast.LENGTH_SHORT
                            ).show()
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
            Toast.makeText(this, R.string.enter_title, Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(this, R.string.pick_image, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.addCategory(title, imageUri)
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
                    binding.image.setImageURI(imageUri)
                }
            }
        }
    }
}
