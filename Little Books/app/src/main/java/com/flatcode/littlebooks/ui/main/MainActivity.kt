package com.flatcode.littlebooks.ui.main

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.navOptions
import androidx.navigation.ui.AppBarConfiguration
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.databinding.ActivityMainBinding
import com.flatcode.littlebooks.ui.profile.ProfileActivity
import com.flatcode.littlebooks.utils.BaseActivity
import com.flatcode.littlebooks.utils.DATA
import com.flatcode.littlebooks.utils.Resource
import com.flatcode.littlebooks.utils.closeApp
import com.flatcode.littlebooks.utils.loadImage
import com.flatcode.littlebooks.utils.openActivity
import com.flatcode.littlebooks.viewmodel.MainViewModel
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import io.selimdawa.bubblebottom.Model
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private var binding: ActivityMainBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.d("MainActivity: onCreate")
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.homeFragment,
                R.id.categoriesFragment,
                R.id.followersFragment,
                R.id.settingsFragment
            )
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> binding!!.toolbar.card.visibility = View.VISIBLE
                else -> binding!!.toolbar.card.visibility = View.GONE
            }
        }

        binding?.bottomNavigation?.apply {
            add(Model(R.id.settingsFragment, R.drawable.ic_settings))
            add(Model(R.id.homeFragment, R.drawable.ic_home))
            add(Model(R.id.followersFragment, R.drawable.ic_books))
            add(Model(R.id.categoriesFragment, R.drawable.ic_group))

            setOnClickMenuListener { model ->
                navController.navigate(model.id, navOptions {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                })
            }

            show(R.id.homeFragment, false)
        }

        MobileAds.initialize(applicationContext) { }
        binding!!.toolbar.image.setOnClickListener {
            context.openActivity<ProfileActivity>(
                clear = false, DATA.PROFILE_ID to DATA.FirebaseUserUid
            )
        }
        loadUserInfo()
        observeViewModel()

        onBackPressedDispatcher.addCallback(this) {
            closeApp()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.user.collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            val user = resource.data
                            binding!!.toolbar.image.loadImage(true, user?.profileImage)
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
        viewModel.getUserInfo(DATA.FirebaseUserUid)
    }
}