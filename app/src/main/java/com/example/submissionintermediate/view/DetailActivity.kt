package com.example.submissionintermediate.view


import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.submissionintermediate.databinding.ActivityDetailBinding
import com.example.submissionintermediate.viewmodel.MainViewModel
import com.example.submissionintermediate.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val detailViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getAuthInstance(application)
    }

    companion object {
        const val EXTRA_ID = "extra_id"
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
    private fun checkUserSession() {
        // Check if the user is logged in by examining the session
        detailViewModel.getSession().observe(this) { user ->
            if (user.isLogin) {
                Log.d("UserSession", "Get session: $user")

                val id = intent.getStringExtra(EXTRA_ID) ?: ""
                detailViewModel.getDetailStories(user.token, id)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()


        detailViewModel.isLoading.observe(this) {
            showLoading(it)
        }

        checkUserSession()

        lifecycleScope.launch {
            detailViewModel.detailstories.collect { result ->
                result?.onSuccess {detail->
                    Glide.with(this@DetailActivity)
                        .load(detail.photoUrl)
                        .into(binding.ivDetailPhoto)

                    binding.tvDetailName.text = detail.name
                    binding.tvDetailDescription.text = detail.description
                    "Lat: ${detail.lat?.toString() ?: "N/A"}".also { binding.tvDetailLat.text = it }
                    "Lon: ${detail.lon?.toString() ?: "N/A"}".also { binding.tvDetailLon.text = it }


                }?.onFailure {
                    // Handle the error
                }
            }
        }

    }
}