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
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityCategoryAddBinding
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.BaseActivity
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.cropImage
import com.flatcode.littlebooksadmin.utils.loadImage
import com.flatcode.littlebooksadmin.utils.pickImage
import com.flatcode.littlebooksadmin.utils.requestStorage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryEditActivity : BaseActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    var categoryId: String? = null
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val viewModel: CategoryAddViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        loadCategoryInfo()

        binding.toolbar.nameSpace.setText(R.string.edit_category)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.image.setOnClickListener {
            requestStorage(DATA.MIX_SQUARE) {
                pickImage(DATA.MIX_SQUARE)
            }
        }
        binding.toolbar.ok.setOnClickListener { validateData() }
    }

    private fun validateData() {
        val name = binding.categoryEt.text.toString().trim()
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show()
        } else {
            dialog = AlertDialog.Builder(this).apply {
                setMessage(getString(R.string.updating_category))
            }.show()
            categoryId?.let { id ->
                viewModel.updateCategory(id, name, imageUri) { success, message ->
                    dialog?.dismiss()
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    if (success) finish()
                }
            }
        }
    }

    private fun loadCategoryInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        reference.child(categoryId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Category::class.java) ?: return
                val categoryName = item.category
                val image = item.image
                if (imageUri == null) {
                    binding.image.loadImage(isUser = true, data = image)
                }
                binding.categoryEt.setText(categoryName)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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
                    binding.image.loadImage(isUser = true, data = imageUri)
                }
            }
        }
    }
}