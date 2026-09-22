package com.flatcode.littlebooks.ui.book

import androidx.appcompat.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import com.flatcode.littlebooks.utils.PermissionUtils
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.loadCategory
import com.flatcode.littlebooks.databinding.ActivityBookEditBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.BookViewModel
import com.flatcode.littlebooks.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookEditActivity : AppCompatActivity() {

    private var binding: ActivityBookEditBinding? = null
    var context: Context = this@BookEditActivity
    private var bookId: String? = null
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null
    
    private var categoryTitle = ArrayList<String>()
    private var categoryId = ArrayList<String>()

    private val bookViewModel: BookViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityBookEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 10 // Original margin was 10dp
            }
            insets
        }

        bookId = intent.getStringExtra(DATA.BOOK_ID)
        dialog = AlertDialog.Builder(context)
            .setTitle("Please wait")
            .setMessage("...")
            .setCancelable(false)
            .create()

        binding!!.toolbar.nameSpace.setText(R.string.edit_book)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.image.setOnClickListener { pickImageGallery() }
        binding!!.category.setOnClickListener { categoryDialog() }
        binding!!.toolbar.ok.setOnClickListener { validateData() }

        observeViewModel()
        loadBookInfo()
    }

    private fun loadBookInfo() {
        bookId?.let { bookViewModel.loadBookDetails(it, DATA.FirebaseUserUid) }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    categoryViewModel.categories.collect { resource ->
                        if (resource is Resource.Success) {
                            categoryTitle.clear()
                            categoryId.clear()
                            resource.data?.forEach {
                                categoryTitle.add(it.category ?: "")
                                categoryId.add(it.id ?: "")
                            }
                        }
                    }
                }
                launch {
                    bookViewModel.bookDetails.collect { resource ->
                        if (resource is Resource.Success) {
                            val book = resource.data
                            binding!!.titleEt.setText(book?.title)
                            binding!!.descriptionEt.setText(book?.description)
                            binding!!.image.loadImage(false, book?.image)
                            selectedId = book?.categoryId ?: ""
                            binding!!.category.loadCategory(selectedId)
                        }
                    }
                }
                launch {
                    bookViewModel.uploadImageStatus.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                updateImageBook(resource.data!!)
                            }
                            is Resource.Error -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            is Resource.Loading -> {
                                dialog!!.setMessage("Updating Image Book...")
                                dialog!!.show()
                            }
                            null -> {}
                        }
                    }
                }
            }
        }
    }

    private var selectedId = DATA.EMPTY
    private var selectedTitle = DATA.EMPTY
    private var title = DATA.EMPTY
    private var description = DATA.EMPTY

    private fun validateData() {
        title = binding!!.titleEt.text.toString().trim { it <= ' ' }
        description = binding!!.descriptionEt.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(selectedId)) {
            Toast.makeText(context, "Pick Category", Toast.LENGTH_SHORT).show()
        } else {
            updateBook()
        }
    }

    private fun updateBook() {
        dialog!!.setMessage("Updating book info...")
        dialog!!.show()
        val hashMap = HashMap<String, Any?>()
        hashMap[DATA.TITLE] = DATA.EMPTY + title
        hashMap[DATA.DESCRIPTION] = DATA.EMPTY + description
        hashMap[DATA.CATEGORY_ID] = DATA.EMPTY + selectedId
        
        lifecycleScope.launch {
            bookViewModel.updateBook(bookId!!, hashMap)
            if (imageUri != null) {
                bookViewModel.uploadBookImage(DATA.FirebaseUserUid, imageUri!!, context)
            } else {
                dialog!!.dismiss()
                Toast.makeText(context, "Book info updated...", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun updateImageBook(imageUrl: String) {
        val hashMap = HashMap<String, Any?>()
        hashMap[DATA.IMAGE] = imageUrl
        lifecycleScope.launch {
            bookViewModel.updateBook(bookId!!, hashMap)
            dialog!!.dismiss()
            Toast.makeText(context, "Book updated...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun categoryDialog() {
        val categoriesArray = categoryTitle.toTypedArray()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) { _, which ->
                selectedId = categoryId[which]
                selectedTitle = categoryTitle[which]
                binding!!.category.text = selectedTitle
            }.show()
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                binding!!.image.setImageURI(imageUri)
            }
        }

    private val requestPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickImageGallery()
            } else {
                Toast.makeText(context, "Permission denied...", Toast.LENGTH_SHORT).show()
            }
        }

    private fun pickImageGallery() {
        if (PermissionUtils.checkStoragePermission(context)) {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            galleryActivityResultLauncher.launch(intent)
        } else {
            requestPermissionLauncher.launch(PermissionUtils.storagePermission)
        }
    }
}



