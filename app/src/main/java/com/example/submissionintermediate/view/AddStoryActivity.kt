package com.example.submissionintermediate.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.submissionintermediate.R
import com.example.submissionintermediate.databinding.ActivityAddStoryBinding
import com.example.submissionintermediate.utils.reduceFileImage
import com.example.submissionintermediate.utils.uriToFile
import com.example.submissionintermediate.view.CameraActivity.Companion.CAMERAX_RESULT
import com.example.submissionintermediate.viewmodel.MainViewModel
import com.example.submissionintermediate.viewmodel.ViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody


class AddStoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddStoryBinding
    private val addStoryViewModel by viewModels<MainViewModel> {
        ViewModelFactory.getAuthInstance(application)
    }
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.GONE
        }
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Permission request granted", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }
        binding.location.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                getLastKnownLocation() // Ambil lokasi saat ini
            }
        }


        binding.buttonGallery.setOnClickListener { startGallery() }
        binding.buttonCamera.setOnClickListener { startCamera() }
        binding.buttonAdd.setOnClickListener { uploadImage() }

        addStoryViewModel.currentImageUri.observe(this) { uri ->
            uri?.let {
                // Display the selected image in an ImageView (for example)
                binding.rvImage.setImageURI(it)
            }
        }
        addStoryViewModel.upload.observe(this) { isUploaded ->
            if (isUploaded == true) {
                Toast.makeText(this, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish() // Tutup activity setelah berhasil upload
            }
        }

        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.dark_green)) // Set your desired color here
        )

        // Optionally, change the title text color (if ActionBar is visible)
        supportActionBar?.title = "Add Story"
        // Observe loading state
        addStoryViewModel.isLoading.observe(this) {
            showLoading(it)
        }

    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            addStoryViewModel.setCurrentImageUri(uri) // Set the URI in ViewModel
        } else {
            Log.d("Photo Picker", "No media selected")
        }
    }

    private fun startCamera() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            val uri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            if (uri != null) {
                addStoryViewModel.setCurrentImageUri(uri) // Set the URI in ViewModel
            } else {
                Log.d("Photo Picker", "No media selected")
            }
        }
    }

    private fun uploadImage() {
        val uri = addStoryViewModel.currentImageUri.value
        if (uri != null) {
            val description = binding.edAddDescription.text.toString()

            if (description.isBlank()) {
                Toast.makeText(this, "Harap isi deskripsi terlebih dahulu", Toast.LENGTH_SHORT).show()
                return
            }
            val imageFile = uriToFile(uri, this).reduceFileImage()
            val lat = latitude.toString().toRequestBody()
            val lon = longitude.toString().toRequestBody()
            val requestBody = description.toRequestBody("text/plain".toMediaType())
            val requestImageFile = imageFile.asRequestBody("image/jpeg".toMediaType())
            val multipartBody = MultipartBody.Part.createFormData(
                "photo",
                imageFile.name,
                requestImageFile
            )
            addStoryViewModel.getSession().observe(this) { user ->
                if (user.isLogin) {
                    addStoryViewModel.uploadStory(user.token, multipartBody, requestBody,lat,lon)
                }
            }
        } else {
            Toast.makeText(
                this,
                "Lengkapi data terlebih dahulu",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getLastKnownLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    latitude = location.latitude
                    longitude = location.longitude
                } else {
                    Log.d("Location", "No location available")
                }
            }
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}