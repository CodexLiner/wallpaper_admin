package me.meenagopal24.recyclerdemo

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.*
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.*
import me.meenagopal24.recyclerdemo.MainActivity.Companion.SERVER_URL
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient.Builder
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

data class dataClass (val name: String, val filename: String, val category: String)
interface Api {
    @GET("category")
    suspend fun getCategories(): Category
    @Multipart
    @POST("wallpaper")
    fun uploadImage(
       @Part name: MultipartBody.Part,
       @Part fileName: MultipartBody.Part,
       @Part category: MultipartBody.Part
    ): Call<SqlResponse>

    @Multipart
    @POST("category")
    fun addCategory(
        @Part category: MultipartBody.Part,
        @Part fileName: MultipartBody.Part,
    ): Call<SqlResponse>

    @GET("/wallpaper")
    fun getWallpaper(): Call<wallpapers?>?

}

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class FirstTab : Fragment(), ImageUpload, AdapterView.OnItemSelectedListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var imageView: ImageView
    private var category = "Select"
    private var nameWall = "Wallpaper"
    private var selectedImageUri: Uri? = null
    lateinit var editText: EditText
    lateinit var swipRe: SwipeRefreshLayout
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
        return inflater.inflate(R.layout.fragment_first_tab, container, false)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val items: ArrayList<String> = arrayListOf()
        items.add("Select Category")// Replace with your actual data
        editText = view.findViewById(R.id.name_edit)
        swipRe = view.findViewById(R.id.swipRe)


        val spinner = view.findViewById<Spinner>(R.id.spinner)
        val adapter = CustomSpinnerAdapter(requireContext(), items = items)

        spinner.adapter = adapter
        spinner.onItemSelectedListener = this
        imageView = view.findViewById(R.id.imageView)

        getCategories(spinner);
        swipRe.setOnRefreshListener {
            getCategories(spinner);
        }

        view.findViewById<Button>(R.id.btnUpload).setOnClickListener {
            showProgress(true)
            if (editText.text.isBlank()) {
                Toast.makeText(requireContext(), "name is required", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }
            nameWall = editText.text.toString()
            val bitmap =
                getBitmapFromUri(selectedImageUri) // Replace with the actual path to your image file
            GlobalScope.launch {
                AsyncTask(this@FirstTab, bitmap, category, "").execute()
            }
        }
        view.findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            selectImage()
        }

    }

    private fun getCategories(spinner: Spinner) {
        val retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL) // Replace with your API base URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val categoryApi = retrofit.create(Api::class.java)

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val categories = categoryApi.getCategories()
                Log.d("TAG", "getCategories: $categories")

                if (spinner.isNotEmpty()) {
                    val list: ArrayList<String> = arrayListOf()
                    for (s in categories.result) {
                        list.add(s.name)
                    }
                    requireActivity().runOnUiThread {
                        if (swipRe.isRefreshing) {
                            swipRe.isRefreshing = false
                        }
                    }
                    val adapter = CustomSpinnerAdapter(requireContext(), items = list)
                    spinner.adapter = adapter
                }
            } catch (e: Exception) {
                Log.d("TAG", "getCategories: e $e")
                // Handle error
            }
        }

    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstTab().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selectedItem = parent?.getItemAtPosition(position).toString()
        category = selectedItem
        Toast.makeText(requireContext(), "Selected Item: $selectedItem", Toast.LENGTH_SHORT).show()
    }


    override fun onNothingSelected(p0: AdapterView<*>?) {
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, MainActivity.REQUEST_IMAGE_PICK)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun uploadImage(imageUploadResponse: ImageUploadResponse) {
        val okHttpClient: OkHttpClient = Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Increase the connection timeout
            .writeTimeout(60, TimeUnit.SECONDS) // Increase the write timeout
            .readTimeout(60, TimeUnit.SECONDS) // Increase the read timeout
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(SERVER_URL) // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()

        val apiService = retrofit.create(Api::class.java)
        // Inside a CoroutineScope
        GlobalScope.launch {
            try {
                val namePart = MultipartBody.Part.createFormData("name", editText.text.toString())
                val categoryPart = MultipartBody.Part.createFormData("category", this@FirstTab.category)
                val filenamePart = MultipartBody.Part.createFormData("filename", imageUploadResponse.fileName)
                val response: Call<SqlResponse> =
                    apiService.uploadImage(namePart , filenamePart , categoryPart)
                response.enqueue(object : retrofit2.Callback<SqlResponse> {
                    override fun onResponse(
                        call: Call<SqlResponse>,
                        response: Response<SqlResponse>,
                    ) {
                        if (response.body()?.result != null) {
                            editText.text = null
                            Toast.makeText(
                                requireContext(),
                                "wallpaper uploaded successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Log.e("TAG", "uploadImage: ${response.body().toString()}")
                            Toast.makeText(
                                requireContext(),
                                response.body()?.code,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        dialog.dismiss()
                    }

                    override fun onFailure(call: Call<SqlResponse>, t: Throwable) {

                    }

                })

            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), e.toString(), Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                e.printStackTrace()
            }
        }


    }

    private fun showProgress(boolean: Boolean) {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.screens_dialog)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)

        layoutParams.width = resources.displayMetrics.widthPixels - 100

        dialog.window?.attributes = layoutParams

        if (boolean) dialog.show() else dialog.dismiss()

    }

    private fun getBitmapFromUri(uri: Uri?): Bitmap? {
        uri?.let {
            return try {
                MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
            } catch (e: IOException) {
                Log.e("ImageUpload", "Error getting bitmap from URI", e)
                null
            }
        }
        return null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == MainActivity.REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                val og = getBitmapFromUri(uri)
                val scaledBitmap = og?.let { Bitmap.createScaledBitmap(it, 400, 400, true) }

                imageView.setImageBitmap(scaledBitmap)
            }
        }
    }

    override fun onImageUploaded(imageUploadResponse: ImageUploadResponse) {
        if (imageUploadResponse.fileName != null) {
            requireActivity().runOnUiThread {
                uploadImage(imageUploadResponse)
            }
        }

    }
}