package com.flatcode.littlebooks.Activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.flatcode.littlebooks.Fragment.CategoriesFragment
import com.flatcode.littlebooks.Fragment.FollowersFragment
import com.flatcode.littlebooks.Fragment.HomeFragment
import com.flatcode.littlebooks.Fragment.SettingsFragment
import com.flatcode.littlebooks.R
import com.flatcode.littlebooks.Unit.CLASS
import com.flatcode.littlebooks.Unit.DATA
import com.flatcode.littlebooks.Unit.THEME
import com.flatcode.littlebooks.Unit.VOID
import com.flatcode.littlebooks.databinding.ActivityMainBinding
import com.google.android.gms.ads.MobileAds
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Objects

class MainActivity : AppCompatActivity(), OnSharedPreferenceChangeListener {

    private var binding: ActivityMainBinding? = null
    var activity: Activity? = null
    var context: Context = also { activity = it }
    var meowBottomNavigation: MeowBottomNavigation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .registerOnSharedPreferenceChangeListener(this)
        THEME.setThemeOfApp(context)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding!!.root
        setContentView(view)

        // Color Mode ----------------------------- Start
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingFragment())
            .commit()
        // Color Mode -------------------------------- End
        //val sharedPreferences = PreferenceManager
        //    .getDefaultSharedPreferences(baseContext)
        //if (sharedPreferences.getString(DATA.COLOR_OPTION, "BASIC") == "BASIC") {
        //    binding!!.toolbar.mode.setBackgroundResource(R.drawable.sun)
        //} else if (sharedPreferences.getString(DATA.COLOR_OPTION, "NIGHT_ONE") == "NIGHT_ONE") {
        //    binding!!.toolbar.mode.setBackgroundResource(R.drawable.moon)
        //}

        meowBottomNavigation = binding!!.bottomNavigation
        meowBottomNavigation!!.add(MeowBottomNavigation.Model(1, R.drawable.ic_settings))
        meowBottomNavigation!!.add(MeowBottomNavigation.Model(2, R.drawable.ic_home))
        meowBottomNavigation!!.add(MeowBottomNavigation.Model(3, R.drawable.ic_books))
        meowBottomNavigation!!.add(MeowBottomNavigation.Model(4, R.drawable.ic_group))
        meowBottomNavigation!!.setOnShowListener { item: MeowBottomNavigation.Model ->
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

        //meowBottomNavigation.setCount(3, numberBooks);
        meowBottomNavigation!!.show(2, true)
        meowBottomNavigation!!.setOnClickMenuListener { item: MeowBottomNavigation.Model ->
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
        meowBottomNavigation!!.setOnReselectListener { item: MeowBottomNavigation.Model ->
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
    }

    private fun loadFragment(fragment: Fragment?) {
        supportFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment!!)
            .commit()
    }

    private fun loadUserInfo() {
        val reference = FirebaseDatabase.getInstance().getReference(DATA.USERS)
        reference.child(Objects.requireNonNull(DATA.FirebaseUserUid))
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val profileImage = DATA.EMPTY + snapshot.child(DATA.PROFILE_IMAGE).value
                    VOID.Glide_(true, context, profileImage, binding!!.toolbar.image)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        VOID.closeApp(context, activity)
    }

    // Color Mode ----------------------------- Start
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == DATA.COLOR_OPTION) {
            recreate()
        }
    }

    class SettingFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SETTINGS_CODE) {
            recreate()
        }
    } // Color Mode -------------------------------- End

    companion object {
        private const val SETTINGS_CODE = 234
    }
}