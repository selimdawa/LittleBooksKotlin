package com.flatcode.littlebooksadmin.ui.book

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityBookAddBinding
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.Dialogs
import com.flatcode.littlebooksadmin.utils.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookAddBinding
    var context: Context = this@BookAddActivity
    private var uri: Uri? = null
    private var imageUri: Uri? = null
    private var categoriesList: List<Category> = emptyList()
    private var dialog: AlertDialog? = null

    private val viewModel: BookAddViewModel by viewModels()

    private val bookPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                uri = result.data?.data
                binding.book.setBackgroundResource(R.color.green)
                binding.choose.setText(R.string.ok)
            } else {
                binding.book.setBackgroundResource(R.color.red)
                binding.choose.setText(R.string.choose_book)
                Toast.makeText(context, R.string.cancelled_picking_book, Toast.LENGTH_SHORT).show()
            }
        }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                binding.image.setImageURI(imageUri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookAddBinding.inflate(layoutInflater)
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
        dialog = Dialogs.createProgressDialog(context, getString(R.string.please_wait))

        binding.toolbar.nameSpace.setText(R.string.add_new_book)
        binding.toolbar.back.setOnClickListener { finish() }

        binding.image.setOnClickListener { pickImageGallery() }
        binding.chooseBook.setOnClickListener { bookPickIntent() }
        binding.category.setOnClickListener { categoryPickDialog() }
        binding.toolbar.ok.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {}
                            is Resource.Success -> {
                                categoriesList = resource.data ?: emptyList()
                            }

                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.uploadState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                dialog = Dialogs.createProgressDialog(
                                    context, getString(R.string.uploading_book)
                                )
                                dialog!!.show()
                            }

                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(
                                    context, R.string.successfully_uploaded, Toast.LENGTH_SHORT
                                ).show()
                            }

                            is Resource.Error -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }

                            null -> {}
                        }
                    }
                }
                launch {
                    viewModel.imageUploadState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                dialog = Dialogs.createProgressDialog(
                                    context, getString(R.string.updating_image_book)
                                )
                                dialog!!.show()
                            }

                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, R.string.image_updated, Toast.LENGTH_SHORT)
                                    .show()
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
        val title = binding.titleEt.text.toString().trim()
        val description = binding.descriptionEt.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, R.string.enter_title, Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, R.string.enter_description, Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(selectedTitle)) {
            Toast.makeText(context, R.string.pick_category, Toast.LENGTH_SHORT).show()
        } else if (uri == null) {
            Toast.makeText(context, R.string.pick_book, Toast.LENGTH_SHORT).show()
        } else {
            viewModel.uploadBook(uri!!, title, description, selectedId ?: "", imageUri)
        }
    }

    private var selectedId: String? = null
    private var selectedTitle: String? = null

    private fun categoryPickDialog() {
        if (categoriesList.isEmpty()) {
            Toast.makeText(context, R.string.loading_categories, Toast.LENGTH_SHORT).show()
            return
        }

        val categories = categoriesList.map { it.category }.toTypedArray()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.pick_category).setItems(categories) { _, which ->
            selectedTitle = categoriesList[which].category
            selectedId = categoriesList[which].id
            binding.category.text = selectedTitle
        }.show()
    }

    private fun bookPickIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        bookPickerLauncher.launch(intent)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                launchGallery()
            } else {
                Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show()
            }
        }

    private fun launchGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(
                context, permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            launchGallery()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }
}
