package com.flatcode.littlebooksadmin.Activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.flatcode.littlebooksadmin.Adapter.EditorsChoiceAdapter
import com.flatcode.littlebooksadmin.Modelimport.EditorsChoice
import com.flatcode.littlebooksadmin.R
import com.flatcode.littlebooksadmin.Unit.THEME
import com.flatcode.littlebooksadmin.databinding.ActivityEditorsChoiceBinding

class EditorsChoiceActivity : AppCompatActivity() {

    private var binding: ActivityEditorsChoiceBinding? = null
    var context: Context = this@EditorsChoiceActivity
    var list: ArrayList<EditorsChoice>? = null
    var adapter: EditorsChoiceAdapter? = null
    var editorsChoice = EditorsChoice()

    override fun onCreate(savedInstanceState: Bundle?) {
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityEditorsChoiceBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.editors_choice)
        binding!!.toolbar.back.setOnClickListener { onBackPressed() }

        //binding.recyclerView.setHasFixedSize(true);
        list = ArrayList()
        adapter = EditorsChoiceAdapter(context, list!!)
        binding!!.recyclerView.adapter = adapter
    }

    fun IdeaPosts() {
        list!!.clear()
        for (i in 0..49) {
            list!!.add(editorsChoice)
        }
        adapter!!.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onResume() {
        IdeaPosts()
        super.onResume()
    }

    override fun onRestart() {
        IdeaPosts()
        super.onRestart()
    }
}