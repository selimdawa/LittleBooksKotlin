package com.flatcode.littlebooksadmin.Activity

import android.Manifest
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.flatcode.littlebooksadmin.Adapter.CommentAdapter
import com.flatcode.littlebooksadmin.Model.Comment
import com.flatcode.littlebooksadmin.Modelimport.Book
import com.flatcode.littlebooksadmin.Modelimport.User
import com.flatcode.littlebooksadmin.MyApplication
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.CLASS
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.Unit.VOID
import com.flatcode.littlebooksadmin.databinding.ActivityBookDetailsBinding
import com.flatcode.littlebooksadmin.databinding.DialogCommentAddBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookDetailsActivity : AppCompatActivity() {

    private var binding: ActivityBookDetailsBinding? = null
    var context: Context = this@BookDetailsActivity
    var bookId: String? = null
    var bookTitle: String? = null
    var bookUrl: String? = null
    private var dialog: ProgressDialog? = null
    private var list: ArrayList<Comment?>? = null
    private var adapter: CommentAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        bookId = intent.getStringExtra(DATA.BOOK_ID)

        binding!!.toolbar.nameSpace.setText(R.string.details_books)
        binding!!.download.visibility = View.GONE
        dialog = ProgressDialog(context)
        dialog!!.setTitle("Please wait...")
        dialog!!.setCanceledOnTouchOutside(false)

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = CommentAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter

        binding!!.love.setOnClickListener { VOID.checkLove(binding!!.love, bookId) }
        binding!!.favorite.setOnClickListener {
            VOID.checkFavorite(binding!!.favorite, bookId)
        }
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
        binding!!.read.setOnClickListener {
            VOID.IntentExtra(context, CLASS.BOOK_VIEW, DATA.BOOK_ID, bookId)
        }
        binding!!.download.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                VOID.downloadBook(
                    context, DATA.EMPTY + bookId,
                    DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                )
            } else {
                resultPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
        binding!!.addComment.setOnClickListener {
            if (DATA.FIREBASE_USER == null) {
                Toast.makeText(context, "You're not logged in...", Toast.LENGTH_SHORT).show()
            } else {
                addCommentDialog()
            }
        }
    }

    private fun init() {
        loadBookDetails()
        loadComments()
        VOID.incrementItemCount(DATA.BOOKS, bookId, DATA.VIEWS_COUNT)
        VOID.isLoves(binding!!.love, bookId)
        VOID.nrLoves(binding!!.loves, bookId)
        VOID.isFavorite(binding!!.favorite, bookId, DATA.FirebaseUserUid)
    }

    private fun loadComments() {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.child(bookId!!).child(DATA.COMMENTS).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                list!!.clear()
                for (data in dataSnapshot.children) {
                    val item = data.getValue(Comment::class.java)!!
                    if (item.bookId == bookId) {
                        list!!.add(item)
                    }
                }
                adapter!!.notifyDataSetChanged()
                if (list!!.isEmpty()) binding!!.textComment.visibility =
                    View.GONE else binding!!.textComment.visibility = View.VISIBLE
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private var comment = DATA.EMPTY
    private fun addCommentDialog() {
        val commentAddBinding = DialogCommentAddBinding.inflate(LayoutInflater.from(this))
        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(commentAddBinding.root)
        val alertDialog = builder.create()
        alertDialog.show()
        commentAddBinding.back.setOnClickListener { alertDialog.dismiss() }
        commentAddBinding.submit.setOnClickListener {
            comment = commentAddBinding.comment.text.toString().trim { it <= ' ' }
            if (TextUtils.isEmpty(comment)) {
                Toast.makeText(context, "Enter your comment...", Toast.LENGTH_SHORT).show()
            } else {
                alertDialog.dismiss()
                addComment()
            }
        }
    }

    private fun addComment() {
        dialog!!.setMessage("Adding comment...")
        dialog!!.show()
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        val id = ref.push().key
        //setup data to add in db for comment
        val hashMap = HashMap<String?, Any?>()
        hashMap[DATA.ID] = id
        hashMap[DATA.BOOK_ID] = DATA.EMPTY + bookId
        hashMap[DATA.TIMESTAMP] = System.currentTimeMillis()
        hashMap[DATA.COMMENT] = DATA.EMPTY + comment
        hashMap[DATA.PUBLISHER] = DATA.EMPTY + DATA.FirebaseUserUid
        assert(id != null)
        ref.child(bookId!!).child(DATA.COMMENTS).child(id!!).setValue(hashMap)
            .addOnSuccessListener {
                Toast.makeText(context, "Comment Added...", Toast.LENGTH_SHORT).show()
                dialog!!.dismiss()
            }.addOnFailureListener { e: Exception ->
                dialog!!.dismiss()
                Toast.makeText(
                    context, "Failed to add comment duo to  " + e.message, Toast.LENGTH_SHORT
                ).show()
            }
    }

    private val resultPermissionLauncher =
        registerForActivityResult(RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                VOID.downloadBook(
                    context, DATA.EMPTY + bookId,
                    DATA.EMPTY + bookTitle, DATA.EMPTY + bookUrl
                )
            } else {
                Toast.makeText(context, "Permission was denied...", Toast.LENGTH_SHORT).show()
            }
        }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.child(bookId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get data
                val item = snapshot.getValue(Book::class.java)!!

                bookTitle = DATA.EMPTY + item.title
                val description = DATA.EMPTY + item.description
                val categoryId = DATA.EMPTY + item.categoryId
                val viewsCount = DATA.EMPTY + item.viewsCount
                val downloadsCount = DATA.EMPTY + item.downloadsCount
                bookUrl = DATA.EMPTY + item.url
                val timestamp = DATA.EMPTY + item.timestamp
                val image = DATA.EMPTY + item.image
                val publisher = DATA.EMPTY + item.publisher
                binding!!.download.visibility = View.VISIBLE

                //format date
                val date: String = MyApplication.formatTimestamp(timestamp.toLong())
                VOID.loadCategory(DATA.EMPTY + categoryId, binding!!.category)
                VOID.loadPdfInfo(DATA.EMPTY + bookUrl, binding!!.size)
                //set data
                VOID.Glide(false, context, image, binding!!.image)
                VOID.Glide(false, context, image, binding!!.cover)
                binding!!.title.text = bookTitle
                binding!!.description.text = description
                binding!!.views.text = viewsCount
                binding!!.downloads.text = downloadsCount
                binding!!.date.text = date
                userInfo(publisher)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun userInfo(userId: String) {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //get data
                if (snapshot.child(userId).exists()) {
                    val item = snapshot.child(userId).getValue(User::class.java)!!
                    val userId = DATA.EMPTY + item.id
                    val imageProfile = DATA.EMPTY + item.profileImage
                    val username = DATA.EMPTY + item.username
                    binding!!.publisherName.text = username
                    VOID.Glide(true, context, imageProfile, binding!!.publisherImage)

                    binding!!.userInfo.setOnClickListener {
                        VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, userId)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onResume() {
        init()
        super.onResume()
    }

    override fun onRestart() {
        init()
        super.onRestart()
    }
}