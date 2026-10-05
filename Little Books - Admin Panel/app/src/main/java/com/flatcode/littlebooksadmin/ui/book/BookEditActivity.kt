package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
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
import com.flatcode.littlebooksadmin.databinding.ActivityBookEditBinding
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.cropImage
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.pickImage
import com.flatcode.littlebooksadmin.utils.requestStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookEditActivity : BaseActivity() {

    private lateinit var binding: ActivityBookEditBinding
    var context: Context = this@BookEditActivity
    private var bookId: String? = null
    private var imageUri: Uri? = null
    private var categoriesList: List<Category> = emptyList()
    private var dialog: AlertDialog? = null

    private val viewModel: BookEditViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        initUI()
        observeViewModel()

        bookId?.let { viewModel.loadBook(it) }
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.edit_book)
        binding.toolbar.back.setOnClickListener { finish() }

        binding.image.setOnClickListener {
            requestStorage(DATA.MIX_BOOK_X) {
                pickImage(DATA.MIX_BOOK_X)
            }
        }
        binding.category.setOnClickListener { categoryDialog() }
        binding.toolbar.ok.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.book.collect { book ->
                        book?.let {
                            binding.titleEt.setText(it.title)
                            binding.descriptionEt.setText(it.description)
                            selectedId = it.categoryId.orEmpty()
                            if (imageUri == null) {
                                binding.image.loadImage(
                                    isUser = false, data = it.image ?: DATA.BASIC
                                )
                            }

                            categoriesList.find { cat -> cat.id == selectedId }?.let { cat ->
                                binding.category.text = cat.category
                            }
                        }
                    }
                }
                launch {
                    viewModel.categories.collect { categories ->
                        categoriesList = categories
                        categoriesList.find { cat -> cat.id == selectedId }?.let { cat ->
                            binding.category.text = cat.category
                        }
                    }
                }
            }
        }
    }

    private var selectedId = DATA.EMPTY

    private fun validateData() {
        val title = binding.titleEt.text.toString().trim()
        val description = binding.descriptionEt.text.toString().trim()

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, R.string.enter_title, Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, R.string.enter_description, Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(selectedId)) {
            Toast.makeText(context, R.string.pick_category, Toast.LENGTH_SHORT).show()
        } else {
            bookId?.let { id ->
                dialog = AlertDialog.Builder(context).apply {
                    setMessage(getString(R.string.updating_book_info))
                }.show()
                viewModel.updateBook(id, title, description, selectedId, imageUri) { success, message ->
                    dialog?.dismiss()
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    if (success) finish()
                }
            }
        }
    }

    private fun categoryDialog() {
        if (categoriesList.isEmpty()) return

        val categoriesArray = categoriesList.map { it.category.orEmpty() }.toTypedArray()

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.choose_category).setItems(categoriesArray) { _, which ->
            selectedId = categoriesList[which].id
            binding.category.text = categoriesList[which].category
        }.show()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == DATA.MIX_BOOK_X) {
                pickImage(DATA.MIX_BOOK_X)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == DATA.MIX_BOOK_X && resultCode == RESULT_OK && data != null) {
            val imageSelectedUri = data.data
            if (imageSelectedUri != null) {
                cropImage(
                    uri = imageSelectedUri,
                    aspectRatioX = 10,
                    aspectRatioY = 14,
                    isOval = false,
                    minWidth = DATA.MIX_BOOK_X,
                    minHeight = DATA.MIX_BOOK_Y,
                    requestCode = DATA.MIX_BOOK_X
                )
            } else {
                val resultUri =
                    IntentCompat.getParcelableExtra(data, "CROP_RESULT_URI", Uri::class.java)
                if (resultUri != null) {
                    imageUri = resultUri
                    binding.image.loadImage(isUser = false, data = imageUri)
                }
            }
        }
    }
}