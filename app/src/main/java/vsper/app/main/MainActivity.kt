package vsper.app.main

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import qiu.niorgai.StatusBarCompat
import vsper.app.R
import vsper.app.global.GlobalRegistry
import vsper.app.main.contacts.ContactsFragment
import vsper.app.update.UpdateUtils
import vsper.app.utils.AppUtils


class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "mainActivity"
        var firstStart = true
        var fragmentLoadFirst: Fragment = DialogListFragment()
    }

    private lateinit var navigationView: NavigationView
    private lateinit var navigationCaller: ImageButton
    private lateinit var drawerLayout: DrawerLayout
//    private lateinit var debugButton: View
    private lateinit var contactsButton:View
    private lateinit var settingsButton: View
    private lateinit var homeButton: View
    private lateinit var shareButton: View


    @SuppressLint("ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "main activity creating. ")

//        StatusBarCompat.translucentStatusBar(this, true);
        val statusColor = AppUtils.getColor(this, R.color.toolbar)
        StatusBarCompat.setStatusBarColor(this, statusColor, 0)

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        navigationView = findViewById(R.id.navigation_view)
        navigationView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> replaceFragment(DialogListFragment())
                R.id.menu_settings -> replaceFragment(SettingsFragment())
                R.id.menu_share -> replaceFragment(UserInfoFragment())
                R.id.menu_about -> replaceFragment(SiginInFragment())
                R.id.menu_debug ->replaceFragment(DebugFragment())
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            false
        }


        navigationCaller = findViewById(R.id.main_navigateCaller)
        navigationCaller.setOnClickListener { drawerLayout.openDrawer(navigationView) }

//        debugButton = findViewById(R.id.menu_debug)
//        debugButton.setOnClickListener {
//            replaceFragment(DebugFragment())
//        }

        contactsButton=findViewById(R.id.bottom_nav_contacts)
        contactsButton.setOnClickListener {
            replaceFragment(ContactsFragment())
        }

        settingsButton = findViewById(R.id.bottom_nav_settings)
        settingsButton.setOnClickListener {
            replaceFragment(SettingsFragment())
        }

        homeButton = findViewById(R.id.bottom_nav_home)
        homeButton.setOnClickListener {
            replaceFragment(DialogListFragment())
        }

        shareButton = findViewById(R.id.bottom_nav_share)
        shareButton.setOnClickListener {
            replaceFragment(UserInfoFragment())
        }

        if (GlobalRegistry.autoUpdate() && firstStart) {
            UpdateUtils.update(this, this)
        }

        replaceFragment(fragmentLoadFirst)
        firstStart = false

    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        transaction.replace(R.id.mainWindow, fragment)
        transaction.commit()
    }

}