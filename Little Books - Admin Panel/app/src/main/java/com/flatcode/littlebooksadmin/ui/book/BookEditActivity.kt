package com.flatcode.littlebooksadmin.ui.book

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
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
import com.flatcode.littlebooksadmin.utils.VOID
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.Resource
import com.flatcode.littlebooksadmin.databinding.ActivityBookEditBinding
import com.flatcode.littlebooksadmin.ui.book.BookEditViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookEditActivity : AppCompatActivity() {

    private var binding: ActivityBookEditBinding? = null
    private val context: Context = this@BookEditActivity
    private var bookId: String? = null
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null
    private var categoriesList: List<Category> = emptyList()
    
    private val viewModel: BookEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBookEditBinding.inflate(layoutInflater)
        setContentView(binding!!.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding!!.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        initUI()
        observeViewModel()
        
        bookId?.let { viewModel.loadBook(it) }
    }

    private fun initUI() {
        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.nameSpace.setText(R.string.edit_book)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.image.setOnClickListener { pickImageGallery() }
        binding!!.category.setOnClickListener { categoryDialog() }
        binding!!.toolbar.ok.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.book.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                resource.data?.let { book ->
                                    binding!!.titleEt.setText(book.title)
                                    binding!!.descriptionEt.setText(book.description)
                                    selectedId = book.categoryId ?: DATA.EMPTY
                                    VOID.Glide(false, context, book.image ?: DATA.BASIC, binding!!.image)
                                    
                                    // Set category name
                                    categoriesList.find { it.id == selectedId }?.let {
                                        binding!!.category.text = it.category
                                    }
                                }
                            }
                            is Resource.Error -> {
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
                launch {
                    viewModel.categories.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> { }
                            is Resource.Success -> {
                                categoriesList = resource.data ?: emptyList()
                                // Re-set category name if book was already loaded
                                categoriesList.find { it.id == selectedId }?.let {
                                    binding!!.category.text = it.category
                                }
                            }
                            is Resource.Error -> { }
                        }
                    }
                }
                launch {
                    viewModel.updateState.collect { resource ->
                        when (resource) {
                            is Resource.Loading -> {
                                dialog!!.setMessage("Updating book info...")
                                dialog!!.show()
                            }
                            is Resource.Success -> {
                                if (imageUri == null) {
                                    dialog!!.dismiss()
                                    Toast.makeText(context, "Book info updated...", Toast.LENGTH_SHORT).show()
                                    finish()
                                } else {
                                    // Handle image upload if needed, or if it's already triggered by ViewModel
                                }
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

    private var selectedId = DATA.EMPTY

    private fun validateData() {
        val title = binding!!.titleEt.text.toString().trim()
        val description = binding!!.descriptionEt.text.toString().trim()
        
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(selectedId)) {
            Toast.makeText(context, "Pick Category", Toast.LENGTH_SHORT).show()
        } else {
            bookId?.let {
                viewModel.updateBook(
                    it, title, description, selectedId,
                    imageUri,
                    if (imageUri != null) VOID.getFileExtension(imageUri, context) else null
                )
            }
        }
    }

    private fun categoryDialog() {
        if (categoriesList.isEmpty()) return
        
        val categoriesArray = categoriesList.map { it.category }.toTypedArray()
        
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) { _, which ->
                selectedId = categoriesList[which].id ?: DATA.EMPTY
                binding!!.category.text = categoriesList[which].category
            }.show()
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data!!
                imageUri = data.data
                binding!!.image.setImageURI(imageUri)
            }
        }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }
}


