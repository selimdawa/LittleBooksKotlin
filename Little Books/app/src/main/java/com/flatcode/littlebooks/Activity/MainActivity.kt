package com.flatcode.littlebooks.Activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.flatcode.littlebooks.Fragment.CategoriesFragment
import com.flatcode.littlebooks.Fragment.FollowersFragment
import com.flatcode.littlebooks.Fragment.HomeFragment
import com.flatcode.littlebooks.Fragment.SettingsFragment
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityMainBinding
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.viewmodel.MainViewModel
import com.google.android.gms.ads.MobileAds
import com.nafis.bottomnavigation.NafisBottomNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private var binding: ActivityMainBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    var bottomNavigation: NafisBottomNavigation? = null

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        bottomNavigation = binding!!.bottomNavigation
        bottomNavigation!!.add(NafisBottomNavigation.Model(1, R.drawable.ic_settings))
        bottomNavigation!!.add(NafisBottomNavigation.Model(2, R.drawable.ic_home))
        bottomNavigation!!.add(NafisBottomNavigation.Model(3, R.drawable.ic_books))
        bottomNavigation!!.add(NafisBottomNavigation.Model(4, R.drawable.ic_group))

        bottomNavigation!!.setOnShowListener { item: NafisBottomNavigation.Model ->
            var fragment: Fragment? = null
            when (item.id) {
                1 -> {
                    binding!!.toolbar.card.visibility = View.GONE
                    fragment = SettingsFragment()
                }

                2 -> {
                    binding!!.toolbar.card.visibility = View.VISIBLE
                    fragment = HomeFragment()
                }

                3 -> {
                    binding!!.toolbar.card.visibility = View.GONE
                    fragment = FollowersFragment()
                }

                4 -> {
                    binding!!.toolbar.card.visibility = View.GONE
                    fragment = CategoriesFragment()
                }
            }
            loadFragment(fragment)
        }

        bottomNavigation!!.show(2, true)
        bottomNavigation!!.setOnClickMenuListener { item: NafisBottomNavigation.Model ->
            when (item.id) {
                1 -> Toast.makeText(
                    applicationContext, R.string.settings, Toast.LENGTH_SHORT
                ).show()

                2 -> Toast.makeText(applicationContext, R.string.home, Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(
                    applicationContext,
                    R.string.followers_books,
                    Toast.LENGTH_SHORT
                ).show()

                4 -> Toast.makeText(applicationContext, R.string.categories, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        bottomNavigation!!.setOnReselectListener { item: NafisBottomNavigation.Model ->
            when (item.id) {
                1 -> Toast.makeText(
                    applicationContext, R.string.settings, Toast.LENGTH_SHORT
                ).show()

                2 -> Toast.makeText(applicationContext, R.string.home, Toast.LENGTH_SHORT).show()
                3 -> Toast.makeText(
                    applicationContext,
                    R.string.followers_books,
                    Toast.LENGTH_SHORT
                ).show()

                4 -> Toast.makeText(applicationContext, R.string.categories, Toast.LENGTH_SHORT)
                    .show()
            }
        }

        MobileAds.initialize(applicationContext) { }
        binding!!.toolbar.image.setOnClickListener {
            VOID.IntentExtra(context, CLASS.PROFILE, DATA.PROFILE_ID, DATA.FirebaseUserUid)
        }
        loadUserInfo()
        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val user = resource.data
                            VOID.Glide_(true, context, user?.profileImage, binding!!.toolbar.image)
                        }
                        is Resource.Error -> {
                            // Handle error
                        }
                        is Resource.Loading -> {
                            // Handle loading
                        }
                    }
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment!!)
            .commit()
    }

    private fun loadUserInfo() {
        DATA.FirebaseUserUid?.let { viewModel.getUserInfo(it) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        VOID.closeApp(context, activity)
    }
}