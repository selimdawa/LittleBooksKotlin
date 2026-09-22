package com.flatcode.littlebooksadmin.ui.category

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.databinding.ActivityCategoryAddBinding
import com.flatcode.littlebooksadmin.model.Category
import com.flatcode.littlebooksadmin.utils.DATA
import com.flatcode.littlebooksadmin.utils.Dialogs
import com.flatcode.littlebooksadmin.utils.loadImage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CategoryEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryAddBinding
    var activity: Activity? = null
    var context: Context = also { activity = it }
    var categoryId: String? = null
    private var imageUri: Uri? = null
    private var dialog: AlertDialog? = null

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            imageUri = result.uriContent
            binding.image.setImageURI(imageUri)
        } else {
            val exception = result.error
            exception?.let {
                Toast.makeText(
                    context, getString(R.string.error_message, it.message), Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startCrop() {
        cropImage.launch(
            CropImageContractOptions(
                uri = null, cropImageOptions = CropImageOptions(
                    guidelines = CropImageView.Guidelines.ON,
                    aspectRatioX = 1,
                    aspectRatioY = 1,
                    fixAspectRatio = true,
                    minCropResultWidth = DATA.MIX_SQUARE,
                    minCropResultHeight = DATA.MIX_SQUARE,
                    cropShape = CropImageView.CropShape.RECTANGLE
                )
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        dialog = Dialogs.createProgressDialog(context, getString(R.string.please_wait))
        loadCategoryInfo()

        binding.toolbar.nameSpace.setText(R.string.edit_category)
        binding.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.image.setOnClickListener { startCrop() }
        binding.toolbar.ok.setOnClickListener { validateData() }
    }

    private var name = DATA.EMPTY
    private fun validateData() {
        name = binding.categoryEt.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, R.string.enter_name, Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateCategory(DATA.EMPTY)
            } else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        dialog = Dialogs.createProgressDialog(context, getString(R.string.updating_category))
        dialog!!.show()
        MediaManager.get().upload(imageUri).option("folder", "Images/Category/")
            .option("public_id", categoryId).callback(object : UploadCallback {
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val uploadedImageUrl = resultData?.get("secure_url") as? String
                    updateCategory(uploadedImageUrl)
                }

                override fun onError(requestId: String?, error: ErrorInfo?) {
                    dialog!!.dismiss()
                    Toast.makeText(
                        context,
                        "Failed to upload image due to " + error?.description,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    private fun updateCategory(imageUrl: String?) {
        dialog = Dialogs.createProgressDialog(context, getString(R.string.updating_category_image))
        dialog!!.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.CATEGORY] = DATA.EMPTY + name
        if (imageUri != null) {
            hashMap[DATA.IMAGE] = DATA.EMPTY + imageUrl
        }
        val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        reference.child(categoryId!!).updateChildren(hashMap).addOnSuccessListener {
                dialog!!.dismiss()
                Toast.makeText(context, R.string.category_updated, Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, getString(R.string.error_message, e.message), Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadCategoryInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        reference.child(categoryId!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val item = snapshot.getValue(Category::class.java)!!
                val name = item.category
                val image = item.image
                binding.image.loadImage(isUser = true, url = image!!)
                binding.categoryEt.setText(name)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}