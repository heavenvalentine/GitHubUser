package com.dicoding.githubuser.ui.detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.dicoding.githubuser.R
import com.dicoding.githubuser.databinding.ActivityDetailUserBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailUserBinding
    private lateinit var viewModel: DetailUserViewModel
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressBar = findViewById(R.id.progressBar)

        val username = intent.getStringExtra(EXTRA_USERNAME) ?: ""
        val id = intent.getIntExtra(EXTRA_ID, 0)
        val avatarUrl = intent.getStringExtra(EXTRA_URL) ?: ""
        val bundle = Bundle()
        bundle.putString(EXTRA_USERNAME, username)

        viewModel = ViewModelProvider(this).get(DetailUserViewModel::class.java)

        if (username != null) {
            showLoading(true)
            viewModel.setUserDetail(username)
        }

        viewModel.getUserDetail().observe(this) { userDetail ->
            if (userDetail != null) {
                binding.apply {
                    // Set other user details as before
                    tvName.text = userDetail.name
                    tvUsername.text = userDetail.login
                    tvFollowers.text = resources.getString(R.string.follower, userDetail.followers)
                    tvFollowing.text = resources.getString(R.string.following, userDetail.following)
                    Glide.with(this@DetailUserActivity)
                        .load(userDetail.avatar_url)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(ivProfile)

                    val githubProfileLink = "https://github.com/${userDetail.login}"

                    share.setOnClickListener {
                        val sendIntent = Intent()
                        sendIntent.action = Intent.ACTION_SEND
                        sendIntent.putExtra(Intent.EXTRA_TEXT, githubProfileLink)
                        sendIntent.type = "text/plain"

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }

                    showLoading(false)
                }
            }
        }

        viewModel.getErrorMessageLiveData().observe(this) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        var _isChecked = false
        CoroutineScope(Dispatchers.IO).launch {
            val count = viewModel.checkUser(id)
            withContext(Dispatchers.Main){
                if(count != null) {
                    if(count>0){
                        binding.toggleFavorite.isChecked = true
                        _isChecked = true
                    }else {
                        binding.toggleFavorite.isChecked = false
                        _isChecked = false
                    }
                }
            }
        }

        binding.toggleFavorite.setOnClickListener{
            _isChecked = ! _isChecked
            if (_isChecked){
                viewModel.addToFavorite(username, id, avatarUrl)
            }
            else {
                viewModel.removeFromFavorite(id)
            }
            binding.toggleFavorite.isChecked = _isChecked
        }

        val sectionPagerAdapter = SectionPagerAdapter(this, supportFragmentManager, bundle)
        binding.apply {
            viewPager.adapter = sectionPagerAdapter
            tabs.setupWithViewPager(viewPager)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }

    companion object {
        const val EXTRA_USERNAME = "extra_username"
        const val EXTRA_ID = "extra_id"
        const val  EXTRA_URL = "extra_url"
    }
}
