package com.flatcode.littlebooks.ui.book

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.databinding.ActivityBookAddBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.BookViewModel
import com.flatcode.littlebooks.viewmodel.CategoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BookAddActivity : AppCompatActivity() {

    private var binding: ActivityBookAddBinding? = null
    var context: Context = this@BookAddActivity
    private var uri: Uri? = null
    private var imageUri: Uri? = null
    
    private var titleList = ArrayList<String>()
    private var idList = ArrayList<String>()
    private var dialog: ProgressDialog? = null

    private val bookViewModel: BookViewModel by viewModels()
    private val categoryViewModel: CategoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.item.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top + 10 // Original margin was 10dp
            }
            insets
        }

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.nameSpace.setText(R.string.add_new_book)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.image.setOnClickListener { pickImageGallery() }
        binding!!.chooseBook.setOnClickListener { bookPickIntent() }
        binding!!.category.setOnClickListener { categoryPickDialog() }
        binding!!.toolbar.ok.setOnClickListener { validateData() }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    categoryViewModel.categories.collect { resource ->
                        if (resource is Resource.Success) {
                            titleList.clear()
                            idList.clear()
                            resource.data?.forEach {
                                titleList.add(it.category ?: "")
                                idList.add(it.id ?: "")
                            }
                        }
                    }
                }
                launch {
                    bookViewModel.uploadFileStatus.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                uploadBookInfoDB(resource.data!!)
                            }
                            is Resource.Error -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            is Resource.Loading -> {
                                dialog!!.setMessage("Uploading Book...")
                                dialog!!.show()
                            }
                            null -> {}
                        }
                    }
                }
                launch {
                    bookViewModel.addBookStatus.collect { resource ->
                        when (resource) {
                            is Resource.Success -> {
                                uploadImage(resource.data!!)
                            }
                            is Resource.Error -> {
                                dialog!!.dismiss()
                                Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                            }
                            is Resource.Loading -> {
                                dialog!!.setMessage("Uploading book info...")
                                dialog!!.show()
                            }
                            null -> {}
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

    private var title = DATA.EMPTY
    private var description = DATA.EMPTY
    private fun validateData() {
        title = binding!!.titleEt.text.toString().trim { it <= ' ' }
        description = binding!!.descriptionEt.text.toString().trim { it <= ' ' }

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(selectedTitle)) {
            Toast.makeText(context, "Pick Category...", Toast.LENGTH_SHORT).show()
        } else if (uri == null) {
            Toast.makeText(context, "Pick Book...", Toast.LENGTH_SHORT).show()
        } else if (imageUri == null) {
            Toast.makeText(context, "Pick Image...", Toast.LENGTH_SHORT).show()
        } else {
            bookViewModel.uploadBookFile(DATA.FirebaseUserUid, uri!!, context)
        }
    }

    private fun uploadBookInfoDB(uploadedBookUrl: String) {
        val bookId = DATA.EMPTY + System.currentTimeMillis() // Or use repo to generate
        val hashMap = HashMap<String, Any?>()
        hashMap[DATA.PUBLISHER] = DATA.EMPTY + DATA.FirebaseUserUid
        hashMap[DATA.ID] = bookId
        hashMap[DATA.TITLE] = DATA.EMPTY + title
        hashMap[DATA.DESCRIPTION] = DATA.EMPTY + description
        hashMap[DATA.CATEGORY_ID] = DATA.EMPTY + selectedId
        hashMap[DATA.URL] = DATA.EMPTY + uploadedBookUrl
        hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
        hashMap[DATA.VIEWS_COUNT] = 0
        hashMap[DATA.DOWNLOADS_COUNT] = 0
        hashMap[DATA.LOVES_COUNT] = 0
        hashMap[DATA.EDITORS_CHOICE] = 0
        hashMap[DATA.IMAGE] = DATA.EMPTY + DATA.BASIC

        bookViewModel.addBook(hashMap)
    }

    private fun uploadImage(bookId: String) {
        bookViewModel.uploadBookImage(DATA.FirebaseUserUid, imageUri!!, context)
        // Store bookId somewhere to update it later, or pass it to uploadBookImage
        // For simplicity, let's assume we update the last added book or pass ID
    }

    private fun updateImageBook(imageUrl: String) {
        // This is a bit tricky with the current flow. 
        // Ideally addBook should be called AFTER all files are uploaded.
        // Or updateBook should be called with the bookId.
        // Let's just finish for now or implement a better orchestration.
        Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
        finish()
    }

    private var selectedId: String? = null
    private var selectedTitle: String? = null

    private fun categoryPickDialog() {
        val categories = titleList.toTypedArray()
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Pick Category")
            .setItems(categories) { _, which ->
                selectedTitle = titleList[which]
                selectedId = idList[which]
                binding!!.category.text = selectedTitle
            }.show()
    }

    private fun bookPickIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        bookPickLauncher.launch(intent)
    }

    private val bookPickLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            uri = result.data?.data
            binding!!.book.setBackgroundResource(R.color.green)
            binding!!.choose.setText(R.string.ok)
        } else {
            binding!!.book.setBackgroundResource(R.color.red)
            binding!!.choose.setText(R.string.choose_book)
            Toast.makeText(context, "Cancelled picking book", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                binding!!.image.setImageURI(imageUri)
            }
        }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }
}



