package com.dicoding.githubuser.ui.detail

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.githubuser.api.RetrofitClient
import com.dicoding.githubuser.ui.data.model.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FollowersViewModel: ViewModel() {
    val listFollowers = MutableLiveData<ArrayList<User>>()

    private val _isFailed = MutableLiveData<String>()
    val isFailed : LiveData<String> = _isFailed

    fun setListFollowers(username: String){
        RetrofitClient.apiInstance
        .getFollowers(username)
            .enqueue(object : Callback<ArrayList<User>>{
                override fun onResponse(
                    call: Call<ArrayList<User>>,
                    response: Response<ArrayList<User>>
                ) {
                    if(response.isSuccessful){
                        listFollowers.postValue(response.body())
                    }
                }

                override fun onFailure(call: Call<ArrayList<User>>, t: Throwable) {
                    Log.e("Failure", t.message ?: "Unknown error")

                    _isFailed.value = "Data loading error."
                }

            })
    }

    fun getListFollowers(): LiveData<ArrayList<User>>{
        return listFollowers
    }
}