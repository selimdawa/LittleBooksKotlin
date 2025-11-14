package com.flatcode.littlebooksadmin.Activity

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Modelimport.Category
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityCategoryAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage

class CategoryEditActivity : AppCompatActivity() {

    private var binding: ActivityCategoryAddBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    var categoryId: String? = null
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryAddBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        categoryId = intent.getStringExtra(DATA.CATEGORY_ID)

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)
        loadCategoryInfo()

        binding!!.toolbar.nameSpace.setText(R.string.edit_category)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.image.setOnClickListener { VOID.cropImageSquare(activity) }
        binding!!.toolbar.ok.setOnClickListener { validateData() }
    }

    private var name = DATA.EMPTY
    private fun validateData() {
        name = binding!!.categoryEt.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateCategory(DATA.EMPTY)
            } else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        dialog!!.setMessage("Updating Category...")
        dialog!!.show()
        val filePathAndName = "Images/Category/$categoryId"
        val reference = FirebaseStorage.getInstance()
            .getReference(filePathAndName + DATA.DOT + VOID.getFileExtension(imageUri, context))
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = DATA.EMPTY + uriTask.result
                updateCategory(uploadedImageUrl)
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, "Failed to upload image due to " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun updateCategory(imageUrl: String?) {
        dialog!!.setMessage("Updating category image...")
        dialog!!.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.CATEGORY] = DATA.EMPTY + name
        if (imageUri != null) {
            hashMap[DATA.IMAGE] = DATA.EMPTY + imageUrl
        }
        val reference = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        reference.child(categoryId!!).updateChildren(hashMap)
            .addOnSuccessListener {
                dialog!!.dismiss()
                Toast.makeText(context, "Category updated...", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT
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
                VOID.Glide(true, context, image!!, binding!!.image)
                binding!!.categoryEt.setText(name)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = CropImage.getPickImageResultUri(context, data)
            if (CropImage.isReadExternalStoragePermissionsRequired(context, uri)) {
                imageUri = uri
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 0)
            } else {
                VOID.cropImageSquare(activity)
            }
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri
                binding!!.image.setImageURI(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this, "Error! $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}