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
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityProfileEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import java.util.Objects

class ProfileEditActivity : AppCompatActivity() {

    private var binding: ActivityProfileEditBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)
        loadUserInfo()

        binding!!.toolbar.nameSpace.setText(R.string.edit_profile)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.image.setOnClickListener { VOID.cropImageSquare(activity) }
        binding!!.go.setOnClickListener { validateData() }
    }

    private var username = DATA.EMPTY
    private fun validateData() {
        username = binding!!.nameEt.text.toString().trim { it <= ' ' }
        if (TextUtils.isEmpty(username)) {
            Toast.makeText(context, "Enter name...", Toast.LENGTH_SHORT).show()
        } else {
            if (imageUri == null) {
                updateProfile(DATA.EMPTY)
            } else {
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        dialog!!.setMessage("Uploading Image...")
        dialog!!.show()
        val filePathAndName = "Images/Profile/" + DATA.FirebaseUserUid
        val ref = FirebaseStorage.getInstance()
            .getReference(filePathAndName + DATA.DOT + VOID.getFileExtension(imageUri, context))
        ref.putFile(imageUri!!).addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
            val uriTask = taskSnapshot.storage.downloadUrl
            while (!uriTask.isSuccessful);
            val uploadedImageUrl = DATA.EMPTY + uriTask.result
            updateProfile(uploadedImageUrl)
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(
                context, "Failed to upload image due to " + e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun updateProfile(imageUrl: String?) {
        dialog!!.setMessage("Updating user profile...")
        dialog!!.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.USER_NAME] = DATA.EMPTY + username
        if (imageUri != null) {
            hashMap[DATA.PROFILE_IMAGE] = DATA.EMPTY + imageUrl
        }
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(Objects.requireNonNull(DATA.FirebaseUserUid)).updateChildren(hashMap)
            .addOnSuccessListener {
                dialog!!.dismiss()
                Toast.makeText(context, "Profile updated...", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun loadUserInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(Objects.requireNonNull(DATA.FirebaseUserUid))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val username = DATA.EMPTY + snapshot.child(DATA.USER_NAME).value
                    val profileImage = DATA.EMPTY + snapshot.child(DATA.PROFILE_IMAGE).value
                    VOID.Glide(true, context, profileImage, binding!!.profileImage)
                    binding!!.nameEt.setText(username)
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
                binding!!.profileImage.setImageURI(imageUri)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                Toast.makeText(this, "Error! $error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}