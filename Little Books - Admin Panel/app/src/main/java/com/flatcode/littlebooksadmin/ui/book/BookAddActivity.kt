package com.flatcode.littlebooksadmin.ui.book

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.utils.getFileExtension
import com.flatcode.littlebooksadmin.databinding.ActivityBookAddBinding
import com.flatcode.littlebooksadmin.ui.book.BookAddViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookAddActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookAddBinding
    var context: Context = this@BookAddActivity
    private var uri: Uri? = null
    private var imageUri: Uri? = null
    private var categoriesList: List<Category> = emptyList()
    private var dialog: ProgressDialog? = null
    
    private val viewModel: BookAddViewModel by viewModels()

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
        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding.toolbar.nameSpace.setText(R.string.add_new_book)
        binding.toolbar.back.setOnClickListener { onBackPressed() }

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
                            is Resource.Loading -> { /* Show some loading for categories if needed */ }
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
                                dialog!!.setMessage("Uploading Book...")
                                dialog!!.show()
                            }
                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
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
                                dialog!!.setMessage("Updating Image Book...")
                                dialog!!.show()
                            }
                            is Resource.Success -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, "Image updated...", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(selectedTitle)) {
            Toast.makeText(context, "Pick Category...", Toast.LENGTH_SHORT).show()
        } else if (uri == null) {
            Toast.makeText(context, "Pick Book...", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.uploadBook(
                uri!!,
                title,
                description,
                selectedId ?: "",
                imageUri
            )
        }
    }

    private var selectedId: String? = null
    private var selectedTitle: String? = null

    private fun categoryPickDialog() {
        if (categoriesList.isEmpty()) {
            Toast.makeText(context, "Loading categories...", Toast.LENGTH_SHORT).show()
            return
        }
        
        val categories = categoriesList.map { it.category }.toTypedArray()

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Pick Category")
            .setItems(categories) { _, which ->
                selectedTitle = categoriesList[which].category
                selectedId = categoriesList[which].id
                binding.category.text = selectedTitle
            }.show()
    }

    private fun bookPickIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        startActivityForResult(intent, BOOK_PICK_CODE)
    }

    private val galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data!!
                imageUri = data.data
                binding.image.setImageURI(imageUri)
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BOOK_PICK_CODE) {
                assert(data != null)
                uri = data!!.data
                binding.book.setBackgroundResource(R.color.green)
                binding.choose.setText(R.string.ok)
            }
        } else {
            binding.book.setBackgroundResource(R.color.red)
            binding.choose.setText(R.string.choose_book)
            Toast.makeText(context, "Cancelled picking book", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    companion object {
        private const val BOOK_PICK_CODE = 1000
    }
}


