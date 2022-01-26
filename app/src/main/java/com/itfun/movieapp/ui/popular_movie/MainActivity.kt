package com.itfun.movieapp.ui.popular_movie

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.GridLayoutManager
import com.itfun.movieapp.R
import com.itfun.movieapp.data.api.TheMovieDBClient
import com.itfun.movieapp.data.api.TheMovieDBInterface
import com.itfun.movieapp.data.repository.NetworkState
import com.itfun.movieapp.receiver.WifiReceiver
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainActivityViewModel

    lateinit var movieRepository: MoviePagedListRepository
    lateinit var receiver: WifiReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        receiver = WifiReceiver()

        IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION).also {
            registerReceiver(receiver, it)
        }


        check_connection.setOnClickListener {
            val connManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifiConn = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobileDataConn = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)

            if (wifiConn.isConnectedOrConnecting){
                Toast.makeText(this, "WIFI IS ENABLED", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this, "MOBILE IS ENABLED", Toast.LENGTH_SHORT).show()
            }
        }

        val apiService : TheMovieDBInterface = TheMovieDBClient.getClient()

        movieRepository = MoviePagedListRepository(apiService)

        viewModel = getViewModel()

        val movieAdapter = PopularMoviePagedListAdapter(this)

        val gridLayoutManager = GridLayoutManager(this, 3)

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = movieAdapter.getItemViewType(position)
                if (viewType == movieAdapter.MOVIE_VIEW_TYPE) return  1
                else return 3
            }
        };


        rv_movie_list.layoutManager = gridLayoutManager
        rv_movie_list.setHasFixedSize(true)
        rv_movie_list.adapter = movieAdapter

        viewModel.moviePagedList.observe(this, Observer {
            movieAdapter.submitList(it)
        })

        viewModel.networkState.observe(this, Observer {
            progress_bar_popular.visibility = if (viewModel.listIsEmpty() && it == NetworkState.LOADING) View.VISIBLE else View.GONE
            txt_error_popular.visibility = if (viewModel.listIsEmpty() && it == NetworkState.ERROR) View.VISIBLE else View.GONE

            if (!viewModel.listIsEmpty()) {
                movieAdapter.setNetworkState(it)
            }
        })

    }


    private fun getViewModel(): MainActivityViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainActivityViewModel(movieRepository) as T
            }
        })[MainActivityViewModel::class.java]
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(receiver)
    }

}
