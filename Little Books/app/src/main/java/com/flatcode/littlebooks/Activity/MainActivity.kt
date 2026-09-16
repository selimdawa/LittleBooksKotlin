package com.flatcode.littlebooks.Activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import com.flatcode.littlebooks.Fragment.CategoriesFragment
import com.flatcode.littlebooks.Fragment.FollowersFragment
import com.flatcode.littlebooks.Fragment.HomeFragment
import com.flatcode.littlebooks.Fragment.SettingsFragment
import com.flatcode.littlebooks.R
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
    private lateinit var navController: NavController

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding!!.toolbar.card.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = systemBars.top
            }
            binding!!.bottomNavigation.updatePadding(bottom = systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        bottomNavigation = binding!!.bottomNavigation
        bottomNavigation!!.add(NafisBottomNavigation.Model(1, R.drawable.ic_settings))
        bottomNavigation!!.add(NafisBottomNavigation.Model(2, R.drawable.ic_home))
        bottomNavigation!!.add(NafisBottomNavigation.Model(3, R.drawable.ic_books))
        bottomNavigation!!.add(NafisBottomNavigation.Model(4, R.drawable.ic_group))

        bottomNavigation!!.setOnShowListener { item: NafisBottomNavigation.Model ->
            val destinationId = when (item.id) {
                1 -> {
                    binding!!.toolbar.card.visibility = View.GONE
                    R.id.settingsFragment
                }

                2 -> {
                    binding!!.toolbar.card.visibility = View.VISIBLE
                    R.id.homeFragment
                }

                3 -> {
                    binding!!.toolbar.card.visibility = View.GONE
                    R.id.followersFragment
                }

                4 -> {
                    binding!!.toolbar.card.visibility = View.GONE
                    R.id.categoriesFragment
                }

                else -> R.id.homeFragment
            }
            navController.navigate(destinationId, navOptions {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = true
                }
                launchSingleTop = true
                restoreState = true
            })
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
            VOID.IntentExtra(context, ProfileActivity::class.java, DATA.PROFILE_ID, DATA.FirebaseUserUid)
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

    private fun loadUserInfo() {
        DATA.FirebaseUserUid?.let { viewModel.getUserInfo(it) }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        VOID.closeApp(context, activity)
    }
}