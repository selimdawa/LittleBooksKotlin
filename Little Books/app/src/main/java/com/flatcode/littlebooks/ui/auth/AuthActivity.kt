package com.flatcode.littlebooks.ui.auth

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.flatcode.littlebooks.utils.intent1
import com.flatcode.littlebooks.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private var binding: ActivityAuthBinding? = null
    var context: Context = this@AuthActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.skipBtn.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = systemBars.bottom + 20 // Original padding was 20sp? Wait, RelativeLayout padding is 20sp.
            }
            insets
        }

        binding!!.loginBtn.setOnClickListener { context.intent1(LoginActivity::class.java) }
        binding!!.skipBtn.setOnClickListener { context.intent1(RegisterActivity::class.java) }
    }
}



