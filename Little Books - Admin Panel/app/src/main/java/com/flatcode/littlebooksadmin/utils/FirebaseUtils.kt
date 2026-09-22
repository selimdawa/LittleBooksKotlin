package com.flatcode.littlebooksadmin.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object FirebaseUtils {
    fun incrementItemCount(database: String?, id: String?, childDB: String?) {
        val ref = FirebaseDatabase.getInstance().getReference(database!!)
        ref.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var itemsCount = DATA.EMPTY + snapshot.child(childDB!!).value
                if (itemsCount == DATA.EMPTY || itemsCount == DATA.NULL) itemsCount =
                    DATA.EMPTY + DATA.ZERO

                val newItemsCount = itemsCount.toInt() + 1
                val hashMap = HashMap<String?, Any>()
                hashMap[childDB] = newItemsCount
                val reference = FirebaseDatabase.getInstance().getReference(database)
                reference.child(id).updateChildren(hashMap)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun incrementItemRemoveCount(database: String?, id: String?, childDB: String?) {
        val ref = FirebaseDatabase.getInstance().getReference(database!!)
        ref.child(id!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var lovesCount = DATA.EMPTY + snapshot.child(childDB!!).value
                if (lovesCount == DATA.EMPTY || lovesCount == DATA.NULL) lovesCount =
                    DATA.EMPTY + DATA.ZERO

                val i = lovesCount.toInt()
                if (i > 0) {
                    val removeLovesCount = lovesCount.toInt() - 1
                    val hashMap = HashMap<String?, Any>()
                    hashMap[childDB] = removeLovesCount
                    val reference = FirebaseDatabase.getInstance().getReference(database)
                    reference.child(id).updateChildren(hashMap)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
