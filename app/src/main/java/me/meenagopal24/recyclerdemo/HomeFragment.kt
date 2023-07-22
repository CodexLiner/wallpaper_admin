package me.meenagopal24.recyclerdemo

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
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
    lateinit var recV: RecyclerView
    lateinit var swipe: SwipeRefreshLayout
    lateinit var list: ArrayList<wallpapers.item>
    lateinit var dialog: Dialog

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

        val swipeHelper: SwipeHelper = object : SwipeHelper(requireContext(), recV) {
            override fun instantiateUnderlayButton(
                viewHolder: RecyclerView.ViewHolder,
                underlayButtons: MutableList<UnderlayButton>,
            ) {
                underlayButtons.add(UnderlayButton(
                    "Delete",
                    R.drawable.fi_ss_settings,
                    Color.parseColor("#FF3C30")
                ) {
                    // TODO: onDelete
                    if (recV.adapter?.itemCount!! >= list.size) {
                        deleteFile(list[it])
//                        list.removeAt(it)
//                        recV.adapter = RecyclerAdapter(list)
                    }
                })
            }
        }
    }

    private fun deleteFile(item: wallpapers.item) {
        showProgress(true)
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL) // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val apiService = retrofit.create(Api::class.java)
        val call: Call<delete_response?>? =
            apiService.deleteWallpaper(item.uuid, item.name, item.category)
        call?.enqueue(object : Callback<delete_response?> {
            override fun onResponse(
                call: Call<delete_response?>,
                response: Response<delete_response?>,
            ) {
                getWallpapers()
                Toast.makeText(requireContext(), "Wallpaper Deleted", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", "onResponse: $response")
                dialog.dismiss()
            }

            override fun onFailure(call: Call<delete_response?>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to delete wallpaper", Toast.LENGTH_SHORT)
                    .show()
                Log.d("TAG", "onResponse: ${t.toString()}")
                dialog.dismiss()

            }

        })

    }

    override fun onResume() {
        super.onResume()
    }
    private fun showProgress(boolean: Boolean) {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.screens_dialog)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        val  text : TextView = dialog.findViewById(R.id.dialog_text)
        text.text = "Deleting Please Wait"
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)

        layoutParams.width = resources.displayMetrics.widthPixels - 100

        dialog.window?.attributes = layoutParams

        if (boolean) dialog.show() else dialog.dismiss()
    }

    private fun getWallpapers() {
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL) // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val apiService = retrofit.create(Api::class.java)
        val call: Call<wallpapers?>? = apiService.getWallpaper()
        call?.enqueue(object : Callback<wallpapers?> {
            override fun onResponse(call: Call<wallpapers?>, response: Response<wallpapers?>) {
                if (response.body()?.list != null) {
                    list = response.body()?.list as ArrayList<wallpapers.item>
                    recV.adapter = response.body()?.list?.let { RecyclerAdapter(it) }
                    if (swipe.isRefreshing) swipe.isRefreshing = false
                }
            }

            override fun onFailure(call: Call<wallpapers?>, t: Throwable) {
                Toast.makeText(requireContext(), "something went wrong", Toast.LENGTH_SHORT).show()
            }

        })

    }

    companion object {
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