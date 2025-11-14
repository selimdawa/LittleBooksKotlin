package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Unit.DATA
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.databinding.ActivityBookViewBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import java.text.MessageFormat

class BookViewActivity : AppCompatActivity() {

    private var binding: ActivityBookViewBinding? = null
    var context: Context = this@BookViewActivity
    var bookId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityBookViewBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val intent = intent
        bookId = intent.getStringExtra(DATA.BOOK_ID)

        loadBookDetails()
        binding!!.toolbar.numberPage.visibility = View.VISIBLE
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }
    }

    private fun loadBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference(DATA.BOOKS)
        ref.child(bookId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val pdfUrl = DATA.EMPTY + snapshot.child(DATA.URL).value
                loadBookFromUrl(pdfUrl)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadBookFromUrl(pdfUrl: String) {
        val reference = FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl)
        reference.getBytes(DATA.MAX_BYTES_PDF.toLong()).addOnSuccessListener { bytes: ByteArray? ->
            binding!!.progressBar.visibility = View.GONE
            binding!!.pdfView.fromBytes(bytes).swipeHorizontal(false)
                .onPageChange { page: Int, pageCount: Int ->
                    val correctPage = page + 1
                    binding!!.toolbar.numberPage.text =
                        MessageFormat.format("{0}/{1}", correctPage, pageCount)
                }.onError { t: Throwable ->
                    Toast.makeText(context, DATA.EMPTY + t.message, Toast.LENGTH_SHORT).show()
                }
                .onPageError { page: Int, t: Throwable ->
                    Toast.makeText(
                        context, "Error on page " + page + DATA.SPACE + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }.load()
            binding!!.progressBar.visibility = View.GONE
        }.addOnFailureListener { binding!!.progressBar.visibility = View.GONE }
    }
}