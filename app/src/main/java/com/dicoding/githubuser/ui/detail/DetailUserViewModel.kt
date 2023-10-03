package com.dicoding.githubuser.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dicoding.githubuser.api.RetrofitClient
import com.dicoding.githubuser.ui.data.local.FavoriteUser
import com.dicoding.githubuser.ui.data.local.FavoriteUserDao
import com.dicoding.githubuser.ui.data.local.UserDatabase
import com.dicoding.githubuser.ui.data.model.DetailUserResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailUserViewModel(application: Application) : AndroidViewModel(application) {
    private val user = MutableLiveData<DetailUserResponse>()
    private val errorMessageLiveData = MutableLiveData<String>()

    private var userDao: FavoriteUserDao?
    private var userDb: UserDatabase?

    private val _isFailed = MutableLiveData<String>()
    val isFailed : LiveData<String> = _isFailed

    init {
        userDb = UserDatabase.getDatabase(application)
        userDao = userDb?.favoriteUserDao()
    }

    fun setUserDetail(username: String) {
        RetrofitClient.apiInstance
            .getUserDetail(username)
            .enqueue(object : Callback<DetailUserResponse> {
                override fun onResponse(
                    call: Call<DetailUserResponse>,
                    response: Response<DetailUserResponse>
                ) {
                    if (response.isSuccessful) {
                        user.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<DetailUserResponse>, t: Throwable) {
                    val errorMessage = t.message ?: "Unknown error"
                    errorMessageLiveData.postValue(errorMessage)

                    _isFailed.value = "Data loading error."
                }
            })
    }

    fun getUserDetail(): LiveData<DetailUserResponse> {
        return user
    }

    fun addToFavorite(username: String, id: Int, avatarUrl: String){
        CoroutineScope(Dispatchers.IO).launch {
             val user = FavoriteUser(
                 username,
                 id,
                 avatarUrl
             )
            userDao?.addToFavorite(user)
        }
    }
    suspend fun checkUser(id: Int) = userDao?.checkUser(id)

    fun removeFromFavorite(id: Int){
        CoroutineScope(Dispatchers.IO).launch {
            userDao?.removeFromFavorite(id)
        }
    }

    fun getErrorMessageLiveData(): LiveData<String> {
        return errorMessageLiveData
    }
}
