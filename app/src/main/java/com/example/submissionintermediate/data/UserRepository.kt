package com.example.submissionintermediate.data

import androidx.lifecycle.LiveData
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.liveData
import com.example.submissionintermediate.data.pref.UserModel
import com.example.submissionintermediate.data.pref.UserPreference
import com.example.submissionintermediate.data.response.AddStoryResponse
import com.example.submissionintermediate.data.response.ListStoryItem
import com.example.submissionintermediate.data.response.ListStoryResponse
import com.example.submissionintermediate.data.response.LoginResult
import com.example.submissionintermediate.data.response.Story
import com.example.submissionintermediate.data.retrofit.ApiService
import com.example.submissionintermediate.database.StoriesDatabase
import com.example.submissionintermediate.paging.StoriesRemoteMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class UserRepository private constructor(
    private val storiesDatabase: StoriesDatabase,
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {
    suspend fun register(name: String, email: String, password: String): Result<Unit> {
        return try {
            apiService.register(name, email, password)
            Result.success(Unit)
        } catch (e: HttpException) {
            when (e.code()) {
                400 -> Result.failure(Exception("Email sudah digunakan atau format email salah"))
                else -> Result.failure(e)
            }
        }catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<LoginResult> {
        return try {
            val response = apiService.login(email, password)
            if (!response.error) {
                Result.success(response.loginResult)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> Result.failure(Exception("Email atau password salah"))
                400 -> Result.failure(Exception("Lengkapi data terlebih dahulu"))
                else -> Result.failure(e)
            }
        }catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getStories(token: String): LiveData<PagingData<ListStoryItem>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),

            remoteMediator = StoriesRemoteMediator(storiesDatabase, apiService, "Bearer $token"),
            pagingSourceFactory = {
//                StoriesPagingSource(apiService,"Bearer $token" )
                storiesDatabase.storiesDao().getAllStories()
            }
        ).liveData
    }
    suspend fun getStoriesWithLocation(token: String, location: Int = 1): Result<ListStoryResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getStoriesWithLocation("Bearer $token", location)
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getDetailStories(token: String, id: String): Result<Story> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.getDetailStory("Bearer $token", id)
            }

            // If the response contains an error, return failure
            if (response.error) {
                Result.failure(Exception(response.message))
            } else {
                // Successfully return the story
                Result.success(response.story)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun uploadImage(
        token: String,
        file: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody,
        lon: RequestBody
    ): Result<AddStoryResponse> {
        return try {
            val response = withContext(Dispatchers.IO) {
                apiService.uploadStory("Bearer $token", file, description, lat, lon)
            }
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }


    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            storiesDatabase: StoriesDatabase,
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(storiesDatabase,userPreference, apiService)
            }.also { instance = it }
    }

}