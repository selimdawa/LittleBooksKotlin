package com.flatcode.littlebooks.ui.settings

import android.content.Context
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityPrivacyPolicyBinding
import com.flatcode.littlebooks.db.SettingDao
import com.flatcode.littlebooks.model.Setting
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PrivacyPolicyActivity : BaseActivity() {

    private var binding: ActivityPrivacyPolicyBinding? = null
    var context: Context = this@PrivacyPolicyActivity

    @Inject
    lateinit var settingDao: SettingDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        binding!!.toolbar.nameSpace.setText(R.string.privacy_policy)
        binding!!.toolbar.back.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        lifecycleScope.launch {
            settingDao.getSettingById(DATA.PRIVACY_POLICY).collectLatest { setting ->
                if (setting != null && !setting.name.isNullOrEmpty()) {
                    binding!!.text.text = setting.name
                }
            }
        }
    }

    private fun privacyPolicy() {
        val reference =
            FirebaseDatabase.getInstance().reference.child(DATA.TOOLS).child(DATA.PRIVACY_POLICY)
        reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.value?.toString().orEmpty()
                if (name.isNotEmpty()) {
                    binding!!.text.text = name
                    lifecycleScope.launch {
                        settingDao.insertSetting(Setting(id = DATA.PRIVACY_POLICY, name = name))
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onResume() {
        privacyPolicy()
        super.onResume()
    }
}
