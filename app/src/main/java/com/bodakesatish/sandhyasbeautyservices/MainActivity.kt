package com.bodakesatish.sandhyasbeautyservices

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.bodakesatish.sandhyasbeautyservices.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_category_list
            )
        )
        navView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            //setFragmentTitle(destination.id)
            if (destination.id == R.id.navigation_home || destination.id == R.id.navigation_dashboard || destination.id == R.id.navigation_category_list) {
                //binding.navView.visibility = View.VISIBLE
                binding.navView.animate().translationY(0f).alpha(1f).setDuration(300)
                    .withStartAction {
                        binding.navView.visibility = View.VISIBLE
                    }
            } else {
                //binding.navView.visibility = View.GONE
                binding.navView.animate().translationY(binding.navView.height.toFloat()).alpha(0f)
                    .setDuration(300).withEndAction {
                        binding.navView.visibility = View.GONE
                    }
            }
        }

    }
}