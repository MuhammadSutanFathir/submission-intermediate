package com.example.submissionintermediate.view

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.submissionintermediate.R
import com.example.submissionintermediate.adapter.LoadingStateAdapter
import com.example.submissionintermediate.adapter.StoriesAdapter
import com.example.submissionintermediate.databinding.ActivityMainBinding
import com.example.submissionintermediate.view.auth.LoginActivity
import com.example.submissionintermediate.viewmodel.MainViewModel
import com.example.submissionintermediate.viewmodel.ViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private val mainViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getAuthInstance(this)
    }
    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
    private fun getData(token: String) {
        val adapter = StoriesAdapter()
        binding.rvStories.adapter = adapter.withLoadStateFooter(
            footer = LoadingStateAdapter {
                adapter.retry()
            }
        )
        mainViewModel.fetchStories(token).observe(this) {
            adapter.submitData(lifecycle, it)
            showLoading(false)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.fab.setOnClickListener {
            startActivity(Intent(this, AddStoryActivity::class.java))
        }
        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.dark_green)) // Set your desired color here
        )

        // Optionally, change the title text color (if ActionBar is visible)
        supportActionBar?.title = "Beranda"

        val layoutManager = LinearLayoutManager(this)
        binding.rvStories.layoutManager = layoutManager

        mainViewModel.isLoading.observe(this) {
            showLoading(it)
        }


        mainViewModel.getSession().observe(this) { user ->
            val token = user.token
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            }
            getData(token)
            Log.d("UserSession", "Session cleared successfully.")

        }

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_stories, menu)
        menuInflater.inflate(R.menu.menu_maps, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                mainViewModel.logout()
                mainViewModel.isLoggedOut.observe(this) { isLoggedOut ->
                    if (isLoggedOut) {
                        showLoading(true)
                        Toast.makeText(this, "Logout Success", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                true
            }
            R.id.action_maps -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}