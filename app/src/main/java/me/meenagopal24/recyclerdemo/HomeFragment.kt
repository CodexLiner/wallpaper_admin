package me.meenagopal24.recyclerdemo

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class HomeFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var recV : RecyclerView
    lateinit var swipe : SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recV = view.findViewById(R.id.staggered_recycler)
        swipe = view.findViewById(R.id.swipe)
        swipe.setOnRefreshListener {
            Toast.makeText(requireContext(), "Refreshing", Toast.LENGTH_SHORT).show()
            getWallpapers()
        }
        recV.layoutManager = LinearLayoutManager(requireContext())
        getWallpapers()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getWallpapers() {
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL) // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val apiService = retrofit.create(Api::class.java)
        val call : Call<wallpapers?>? = apiService.getWallpaper()
        call?.enqueue(object : Callback<wallpapers?> {
            override fun onResponse(call: Call<wallpapers?>, response: Response<wallpapers?>) {
               if (response.body()?.list != null){
                   var list = response.body()?.list as ArrayList<wallpapers.item>
                   recV.adapter = response.body()?.list?.let { RecyclerAdapter(it) }
                   if (swipe.isRefreshing)swipe.isRefreshing = false
               }
            }
            override fun onFailure(call: Call<wallpapers?>, t: Throwable) {
                Toast.makeText(requireContext(), "something went wrong", Toast.LENGTH_SHORT).show()
            }

        })

    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment HomeFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}