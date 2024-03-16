package com.flatcode.littlebooksadmin.Activity

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityBookAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class BookAddActivity : AppCompatActivity() {

    private var binding: ActivityBookAddBinding? = null
    var context: Context = this@BookAddActivity
    private var uri: Uri? = null
    private var imageUri: Uri? = null
    private var titleList: ArrayList<String>? = null
    private var idList: ArrayList<String>? = null
    private var dialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        loadBookCategories()
        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        binding!!.toolbar.nameSpace.setText(R.string.add_new_book)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.image.setOnClickListener { pickImageGallery() }
        binding!!.chooseBook.setOnClickListener { bookPickIntent() }
        binding!!.category.setOnClickListener { categoryPickDialog() }
        binding!!.toolbar.ok.setOnClickListener { validateData() }
    }

    private var title = DATA.EMPTY
    private var description = DATA.EMPTY
    private fun validateData() {
        //get data
        title = binding!!.titleEt.text.toString().trim { it <= ' ' }
        description = binding!!.descriptionEt.text.toString().trim { it <= ' ' }

        //validate data
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(context, "Enter Title...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(description)) {
            Toast.makeText(context, "Enter Description...", Toast.LENGTH_SHORT).show()
        } else if (TextUtils.isEmpty(selectedTitle)) {
            Toast.makeText(context, "Pick Category...", Toast.LENGTH_SHORT).show()
        } else if (uri == null) {
            Toast.makeText(context, "Pick Book...", Toast.LENGTH_SHORT).show()
        } else {
            uploadBookToStorage()
        }
    }

    private fun uploadBookToStorage() {
        dialog!!.setMessage("Uploading Book...")
        dialog!!.show()
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        val id = ref.push().key
        val filePathAndName = "PDF/Books/$id"
        val storageReference = FirebaseStorage.getInstance()
            .getReference(filePathAndName + DATA.DOT + VOID.getFileExtension(uri, context))
        storageReference.putFile(uri!!)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedBookUrl = DATA.EMPTY + uriTask.result
                uploadBookInfoDB(uploadedBookUrl, id, ref)
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, "Book upload failed due to " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun uploadBookInfoDB(uploadedBookUrl: String, id: String?, ref: DatabaseReference) {
        dialog!!.setMessage("Uploading book info...")

        //setup data to upload
        val hashMap = HashMap<String?, Any?>()
        hashMap[DATA.PUBLISHER] = DATA.EMPTY + DATA.FirebaseUserUid
        hashMap[DATA.ID] = id
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

        assert(id != null)
        ref.child(id!!).setValue(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            Toast.makeText(context, "Successfully uploaded...", Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener {
            VOID.incrementItemCount(DATA.USERS, DATA.FirebaseUserUid, DATA.BOOKS_COUNT)
            uploadImage(id)
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(
                context, "Failure to upload to db due to :" + e.message, Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadBookCategories() {
        titleList = ArrayList()
        idList = ArrayList()

        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                titleList!!.clear()
                idList!!.clear()
                for (data in snapshot.children) {
                    val categoryId = DATA.EMPTY + data.child(DATA.ID).value
                    val categoryTitle = DATA.EMPTY + data.child(DATA.CATEGORY).value

                    titleList!!.add(categoryTitle)
                    idList!!.add(categoryId)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateImageBook(imageUrl: String, bookId: String?) {
        dialog!!.setMessage("Updating Image Book...")
        dialog!!.show()
        val hashMap = HashMap<String?, Any>()
        if (imageUri != null) {
            hashMap[DATA.IMAGE] = DATA.EMPTY + imageUrl
        } else {
            hashMap[DATA.IMAGE] = DATA.EMPTY + DATA.BASIC
        }

        val reference = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        reference.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            Toast.makeText(context, "Image updated...", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e: Exception ->
            dialog!!.dismiss()
            Toast.makeText(context, "Failed to update db duo to " + e.message, Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun uploadImage(BookId: String?) {
        dialog!!.setMessage("Updating Image Book")
        dialog!!.show()

        val filePathAndName = "BookImages/" + DATA.FirebaseUserUid
        val reference = FirebaseStorage.getInstance()
            .getReference(filePathAndName + DATA.DOT + VOID.getFileExtension(imageUri, context))
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                val uriTask = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = DATA.EMPTY + uriTask.result
                updateImageBook(uploadedImageUrl, BookId)
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, "Failed to upload image due to " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private var selectedId: String? = null
    private var selectedTitle: String? = null

    private fun categoryPickDialog() {
        val categories = arrayOfNulls<String>(titleList!!.size)
        for (i in titleList!!.indices)
            categories[i] = titleList!![i]

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Pick Category")
            .setItems(categories) { dialog: DialogInterface?, which: Int ->
                selectedTitle = titleList!![which]
                selectedId = idList!![which]
                binding!!.category.text = selectedTitle
            }.show()
    }

    private fun bookPickIntent() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"
        startActivityForResult(intent, BOOK_PICK_CODE)
    }

    private val galleryActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data!!
                imageUri = data.data
                binding!!.image.setImageURI(imageUri)
            }
        }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BOOK_PICK_CODE) {
                assert(data != null)
                uri = data!!.data
                binding!!.book.setBackgroundResource(R.color.green)
                binding!!.choose.setText(R.string.ok)
            }
        } else {
            binding!!.book.setBackgroundResource(R.color.red)
            binding!!.choose.setText(R.string.choose_book)
            Toast.makeText(context, "Cancelled picking book", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    companion object {
        private const val BOOK_PICK_CODE = 1000
    }
}