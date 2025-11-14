package com.flatcode.littlebooksadmin.Activity

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityBookEditBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask

class BookEditActivity : AppCompatActivity() {

    private var binding: ActivityBookEditBinding? = null
    var context: Context = this@BookEditActivity
    private var bookId: String? = null
    private var imageUri: Uri? = null
    private var dialog: ProgressDialog? = null
    private var categoryTitle: ArrayList<String>? = null
    private var categoryId: ArrayList<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityBookEditBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        bookId = intent.getStringExtra(DATA.BOOK_ID)

        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)
        loadCategories()
        loadBooksInfo()

        binding!!.toolbar.nameSpace.setText(R.string.edit_book)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        binding!!.image.setOnClickListener { pickImageGallery() }
        binding!!.category.setOnClickListener { categoryDialog() }
        binding!!.toolbar.ok.setOnClickListener { validateData() }
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
        dialog!!.setMessage("Uploading book info...")
        dialog!!.show()
        val hashMap = HashMap<String?, Any>()
        hashMap[DATA.TITLE] = DATA.EMPTY + title
        hashMap[DATA.DESCRIPTION] = DATA.EMPTY + description
        hashMap[DATA.CATEGORY_ID] = DATA.EMPTY + selectedId
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.child(bookId!!).updateChildren(hashMap).addOnSuccessListener {
            dialog!!.dismiss()
            Toast.makeText(context, "Book info updated...", Toast.LENGTH_SHORT).show()
        }.addOnCompleteListener { if (imageUri != null) uploadImage(bookId) }
            .addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(context, DATA.EMPTY + e.message, Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadBooksInfo() {
        val refBooks = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        refBooks.child(bookId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get book info
                selectedId = DATA.EMPTY + snapshot.child(DATA.CATEGORY_ID).value
                val description = DATA.EMPTY + snapshot.child(DATA.DESCRIPTION).value
                val title = DATA.EMPTY + snapshot.child(DATA.TITLE).value
                val image = DATA.EMPTY + snapshot.child(DATA.IMAGE).value

                VOID.Glide(false, context, image, binding!!.image)
                binding!!.titleEt.setText(title)
                binding!!.descriptionEt.setText(description)

                val refBookCategory = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
                refBookCategory.child(selectedId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            //get category
                            val category = DATA.EMPTY + snapshot.child(DATA.CATEGORY).value
                            binding!!.category.text = category
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun categoryDialog() {
        val categoriesArray = arrayOfNulls<String>(categoryTitle!!.size)
        for (i in categoryTitle!!.indices) {
            categoriesArray[i] = categoryTitle!![i]
        }
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Category")
            .setItems(categoriesArray) { dialog: DialogInterface?, which: Int ->
                selectedId = categoryId!![which]
                selectedTitle = categoryTitle!![which]
                binding!!.category.text = selectedTitle
            }.show()
    }

    private fun loadCategories() {
        categoryId = ArrayList()
        categoryTitle = ArrayList()
        val ref = FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                categoryId!!.clear()
                categoryTitle!!.clear()
                for (data in snapshot.children) {
                    val id = DATA.EMPTY + data.child(DATA.ID).value
                    val category = DATA.EMPTY + data.child(DATA.CATEGORY).value
                    categoryId!!.add(id)
                    categoryTitle!!.add(category)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateImageBook(imageUrl: String, bookId: String?) {
        dialog!!.setMessage("Updating image book...")
        dialog!!.show()
        val hashMap = HashMap<String?, Any>()
        if (imageUri != null) {
            hashMap[DATA.IMAGE] = DATA.EMPTY + imageUrl
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
        val filePathAndName = "Images/Books/$bookId"
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

    private val galleryActivityResultLauncher =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data!!
                imageUri = data.data
                binding!!.image.setImageURI(imageUri)
            }
        }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }
}