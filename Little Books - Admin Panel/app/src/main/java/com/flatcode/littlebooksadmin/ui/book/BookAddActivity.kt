package com.flatcode.littlebooksadmin.ui.book

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.content.IntentCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityBookAddBinding
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.cropImage
import com.flatcode.littlebooksadmin.utils.isNetworkAvailable
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.pickImage
import com.flatcode.littlebooksadmin.utils.requestStorage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookAddActivity : BaseActivity() {

    private lateinit var binding: ActivityBookAddBinding
    var context: Context = this@BookAddActivity
    private var uri: Uri? = null
    private var imageUri: Uri? = null
    private var categoriesList: List<Category> = emptyList()
    private var dialog: AlertDialog? = null

    private val viewModel: BookAddViewModel by viewModels()

    private val bookPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initUI()
        observeViewModel()
    }

    private fun initUI() {
        binding.toolbar.nameSpace.setText(R.string.add_new_book)
        binding.toolbar.back.setOnClickListener { finish() }

        binding.image.setOnClickListener {
            requestStorage(DATA.MIX_BOOK_X) {
                pickImage(DATA.MIX_BOOK_X)
            }
        }
        binding.chooseBook.setOnClickListener { bookPickIntent() }
        binding.category.setOnClickListener { categoryPickDialog() }
        binding.toolbar.ok.setOnClickListener { validateData() }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    categoriesList = categories
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
        } else if (!isNetworkAvailable()) {
            Toast.makeText(context, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT)
                .show()
        } else {
            dialog = AlertDialog.Builder(context).apply {
                setMessage(getString(R.string.uploading_book))
            }.show()
            viewModel.uploadBook(uri!!, title, description, selectedId ?: "", imageUri) { success, message ->
                dialog?.dismiss()
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                if (success) finish()
            }
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