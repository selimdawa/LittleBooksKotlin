package com.flatcode.littlebooks.ui.book

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
import com.flatcode.littlebooks.databinding.ActivityBookEditBinding
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.PermissionUtils
import com.flatcode.littlebooks.utils.ProgressDialog
import com.flatcode.littlebooks.utils.loadCategory
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.startCropActivity
import com.flatcode.littlebooks.viewmodel.BookViewModel
import com.flatcode.littlebooks.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookEditActivity : BaseActivity() {

    private var binding: ActivityBookEditBinding? = null
    var context: Context = this@BookEditActivity
    private var bookId: String? = null
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null

    private var categoryTitle = ArrayList<String>()
    private var categoryId = ArrayList<String>()

    private val bookViewModel: BookViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        bookId = intent.getStringExtra(DATA.BOOK_ID)
        dialog = ProgressDialog(this).apply {
            setTitle("Please wait...")
            setCanceledOnTouchOutside(false)
        }

        binding!!.toolbar.nameSpace.setText(R.string.edit_book)
        binding!!.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
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
                    categoryViewModel.categories.collect { list ->
                        categoryTitle.clear()
                        categoryId.clear()
                        list.forEach {
                            categoryTitle.add(it.category ?: "")
                            categoryId.add(it.id)
                        }
                    }
                }
                launch {
                    bookViewModel.bookDetails.collect { book ->
                        book?.let {
                            binding!!.titleEt.setText(it.title)
                            binding!!.descriptionEt.setText(it.description)
                            binding!!.image.loadImage(false, it.image)
                            selectedId = it.categoryId ?: ""
                            binding!!.category.loadCategory(selectedId)
                        }
                    }
                }
                launch {
                    bookViewModel.uploadImageStatus.collect { result ->
                        result?.let {
                            dialog?.dismiss()
                            if (it.isSuccess) {
                                updateImageBook(it.getOrNull()!!)
                            } else {
                                Toast.makeText(
                                    context,
                                    it.exceptionOrNull()?.message ?: "Upload image failed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
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
        title = binding!!.titleEt.text.toString().trim()
        description = binding!!.descriptionEt.text.toString().trim()
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
        dialog?.setMessage("Updating book info...")
        dialog?.show()
        val hashMap = HashMap<String, Any?>()
        hashMap[DATA.TITLE] = DATA.EMPTY + title
        hashMap[DATA.DESCRIPTION] = DATA.EMPTY + description
        hashMap[DATA.CATEGORY_ID] = DATA.EMPTY + selectedId

        lifecycleScope.launch {
            bookViewModel.updateBook(bookId!!, hashMap)
            if (imageUri != null) {
                bookViewModel.uploadBookImage(imageUri!!)
            } else {
                dialog?.dismiss()
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
            dialog?.dismiss()
            Toast.makeText(context, "Book updated...", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun categoryDialog() {
        val categoriesArray = categoryTitle.toTypedArray()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Category").setItems(categoriesArray) { _, which ->
                selectedId = categoryId[which]
                selectedTitle = categoryTitle[which]
                binding!!.category.text = selectedTitle
            }.show()
    }

    private val cropImageLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            imageUri = result.data?.let { intent ->
                IntentCompat.getParcelableExtra(intent, "CROP_RESULT_URI", Uri::class.java)
            }
            binding!!.image.setImageURI(null)
            binding!!.image.setImageURI(imageUri)
        }
    }

    private val pickImageLauncher = registerForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let {
            cropImageLauncher.launch(context.startCropActivity(it, 10, 14, false))
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

    private fun pickImageGallery() {
        if (PermissionUtils.checkStoragePermission(context)) {
            pickImageLauncher.launch("image/*")
        } else {
            requestPermissionLauncher.launch(PermissionUtils.storagePermission)
        }
    }
}