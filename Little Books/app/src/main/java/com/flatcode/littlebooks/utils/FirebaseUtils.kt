package com.flatcode.littlebooks.utils

import android.app.Dialog
import android.content.Context
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.model.ADs
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

fun Context.deleteBook(
    dialogDelete: Dialog,
    publisher: String?,
    bookId: String?,
    bookTitle: String,
) {
    val dialog =
        AlertDialog.Builder(this).setTitle("Please wait").setMessage("Deleting $bookTitle ...")
            .setCancelable(false).show()
    FirebaseDatabase.getInstance().getReference(DATA.BOOKS).child(bookId!!).removeValue()
        .addOnSuccessListener {
            dialog.dismiss()
            Toast.makeText(this, "Books Deleted Successfully...", Toast.LENGTH_SHORT).show()
            dialogDelete.dismiss()
            incrementItemRemoveCount(DATA.USERS, publisher, DATA.BOOKS_COUNT)
        }.addOnFailureListener { e ->
            dialog.dismiss()
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
}

fun ImageView.isFavorite(id: String?, userId: String?) {
    val reference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child(DATA.FAVORITES).child(userId!!)
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.child(id!!).exists()) {
                this@isFavorite.setImageResource(R.drawable.ic_star_selected)
                this@isFavorite.tag = "added"
            } else {
                this@isFavorite.setImageResource(R.drawable.ic_star_unselected)
                this@isFavorite.tag = "add"
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun ImageView.checkFavorite(bookId: String?) {
    if (this.tag == "add") {
        FirebaseDatabase.getInstance().getReference(DATA.FAVORITES).child(DATA.FirebaseUserUid)
            .child(bookId!!).setValue(true)
    } else {
        FirebaseDatabase.getInstance().getReference(DATA.FAVORITES).child(DATA.FirebaseUserUid)
            .child(bookId!!).removeValue()
    }
}

fun ImageView.checkLove(bookId: String?) {
    if (this.tag == "love") {
        FirebaseDatabase.getInstance().getReference(DATA.LOVES).child(bookId!!)
            .child(DATA.FirebaseUserUid).setValue(true)
        incrementItemCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    } else {
        FirebaseDatabase.getInstance().getReference(DATA.LOVES).child(bookId!!)
            .child(DATA.FirebaseUserUid).removeValue()
        incrementItemRemoveCount(DATA.BOOKS, bookId, DATA.LOVES_COUNT)
    }
}

fun ImageView.isLoves(bookId: String?) {
    val reference: DatabaseReference =
        FirebaseDatabase.getInstance().reference.child(DATA.LOVES).child(bookId!!)
    reference.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(dataSnapshot: DataSnapshot) {
            if (dataSnapshot.child(DATA.FirebaseUserUid).exists()) {
                this@isLoves.setImageResource(R.drawable.ic_heart_selected)
                this@isLoves.tag = "loved"
            } else {
                this@isLoves.setImageResource(R.drawable.ic_heart_unselected)
                this@isLoves.tag = "love"
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun TextView.loadCategory(categoryId: String?) {
    categoryId ?: return
    FirebaseDatabase.getInstance().getReference(DATA.CATEGORIES).child(categoryId)
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                text = snapshot.child(DATA.CATEGORY).value.toString()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
}

fun incrementItemCount(database: String?, id: String?, childDB: String?) =
    updateItemCount(database, id, childDB, 1)

fun incrementItemRemoveCount(database: String?, id: String?, childDB: String?) =
    updateItemCount(database, id, childDB, -1)

private fun updateItemCount(database: String?, id: String?, childDB: String?, increment: Int) {
    if (database == null || id == null || childDB == null) return
    val ref = FirebaseDatabase.getInstance().getReference(database).child(id)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.child(childDB).value.toString().toIntOrNull() ?: 0
            ref.updateChildren(mapOf(childDB to (count + increment).coerceAtLeast(0)))
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun adCount(userId: String?, bannerName: String?, key: String?) {
    if (userId == null || bannerName == null || key == null) return
    val ref = FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId).child(bannerName)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.child(key).value.toString().toLongOrNull() ?: 0L
            ref.updateChildren(mapOf(key to count + 1)).addOnCompleteListener {
                adName(DATA.FirebaseUserUid, bannerName)
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun adUserCount(userId: String?, key: String?, number: Int) {
    if (userId == null || key == null) return
    val ref = FirebaseDatabase.getInstance().getReference(DATA.USERS).child(userId)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val count = snapshot.child(key).value.toString().toLongOrNull() ?: 0L
            ref.updateChildren(mapOf(key to count + number))
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}

fun adName(userId: String?, bannerName: String?) {
    if (userId == null || bannerName == null) return
    val ref = FirebaseDatabase.getInstance().getReference(DATA.AD_S).child(userId).child(bannerName)
    ref.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            if (snapshot.getValue(ADs::class.java)?.name == null) {
                ref.updateChildren(mapOf(DATA.NAME to bannerName))
            }
        }

        override fun onCancelled(error: DatabaseError) {}
    })
}
