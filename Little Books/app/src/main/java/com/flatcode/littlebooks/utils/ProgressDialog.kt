package com.flatcode.littlebooks.utils

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.drawable.toDrawable
import com.flatcode.littlebooks.databinding.DialogProgressBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ProgressDialog(context: Context) {

    private val binding: DialogProgressBinding =
        DialogProgressBinding.inflate(LayoutInflater.from(context))

    private val dialog: AlertDialog =
        MaterialAlertDialogBuilder(context).setView(binding.root).setCancelable(false).create()

    init {
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    fun setTitle(title: CharSequence?) {
        if (!title.isNullOrEmpty()) {
            binding.tvTitle.text = title
            binding.tvTitle.visibility = View.VISIBLE
        } else {
            binding.tvTitle.visibility = View.GONE
        }
    }

    fun setMessage(message: CharSequence?) {
        if (!message.isNullOrEmpty()) {
            binding.tvMessage.text = message
            binding.tvMessage.visibility = View.VISIBLE
        } else {
            binding.tvMessage.visibility = View.GONE
        }
    }

    fun setCanceledOnTouchOutside(cancel: Boolean) {
        dialog.setCanceledOnTouchOutside(cancel)
    }

    fun show() {
        if (!dialog.isShowing) {
            dialog.show()
        }
    }

    fun dismiss() {
        if (dialog.isShowing) {
            dialog.dismiss()
        }
    }
}