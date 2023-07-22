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
import androidx.fragment.app.Fragment
import android.widget.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.ByteArrayOutputStream
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
class SecondTab : Fragment() , ImageUpload {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null
    private var category = "Select"
    lateinit var textArea: EditText
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
        return inflater.inflate(R.layout.fragment_second_tab, container, false)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        textArea = view.findViewById(R.id.name_edit)
        val items =
            listOf("No Category Found") // Replace with your actual data
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.spinner_item,
            items
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        imageView = view.findViewById<ImageView>(R.id.imageView)
        view.findViewById<Button>(R.id.btnUploadCAT).setOnClickListener {
            showProgress(true)
            if (textArea.text.isBlank()){
                Toast.makeText(requireContext(), "label should not empty", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                return@setOnClickListener
            }
            val bitmap =
                getBitmapFromUri(selectedImageUri) // Replace with the actual path to your image file
            GlobalScope.launch {
                AsyncTask(this@SecondTab, bitmap, "category_thumbnails", "").execute()
            }
        }
        view.findViewById<Button>(R.id.btnSelectImage).setOnClickListener {
            selectImage()
        }
    }
    companion object {
        fun newInstance(param1: String, param2: String) =
            SecondTab().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun selectImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, MainActivity.REQUEST_IMAGE_PICK)
    }
    private fun showProgress(boolean: Boolean) {
        dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.screens_dialog)
        dialog.window?.setBackgroundDrawableResource(R.color.transparent)
        val  text : TextView = dialog.findViewById(R.id.dialog_text)
        text.text = "Adding Category Please Wait"
        val layoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes)

        layoutParams.width = resources.displayMetrics.widthPixels - 100

        dialog.window?.attributes = layoutParams

        if (boolean) dialog.show() else dialog.dismiss()
    }
    @OptIn(DelicateCoroutinesApi::class)
    private fun addCategory(imageUploadResponse: ImageUploadResponse) {
        if (textArea.text.isBlank()) {
            Toast.makeText(requireContext(), "name is required", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
            return
        }
        val retrofit = Retrofit.Builder()
            .baseUrl(MainActivity.SERVER_URL) // Replace with your base URL
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder().build())
            .build()

        val apiService = retrofit.create(Api::class.java)
        val namePart = MultipartBody.Part.createFormData("category", textArea.text.toString())
        val imagePart = MultipartBody.Part.createFormData("fileName", imageUploadResponse.fileName)

        // Inside a CoroutineScope
        GlobalScope.launch {
            try {
                val response: Call<SqlResponse> = apiService.addCategory(namePart, imagePart)
                response.enqueue(object : Callback<SqlResponse> {
                    override fun onResponse(
                        call: Call<SqlResponse>,
                        response: Response<SqlResponse>,
                    ) {
                        requireActivity().runOnUiThread {
                            if (response.body()?.result != null) {
                                textArea.text = null
                                Toast.makeText(
                                    requireContext(),
                                    "Category Added Successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().runOnUiThread {
                                  dialog.dismiss()
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "something went wrong",
                                    Toast.LENGTH_SHORT
                                ).show()
                                requireActivity().runOnUiThread {
                                    dialog.dismiss()
                                }
                            }
                        }
                    }

                    override fun onFailure(call: Call<SqlResponse>, t: Throwable) {
                        view?.findViewById<TextView>(R.id.err_text)?.text = t.localizedMessage
                        requireActivity().runOnUiThread {
                            dialog.dismiss()
                        }
                    }

                })
            } catch (e: Exception) {

                e.printStackTrace()
                requireActivity().runOnUiThread {
                    view?.findViewById<TextView>(R.id.err_text)?.text = e.toString()
                    dialog.dismiss()
                }
            }
        }


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
                imageView.setImageURI(uri)
            }
        }
    }
    override fun onImageUploaded(imageUploadResponse: ImageUploadResponse?) {
        if (imageUploadResponse?.fileName != null){
           requireActivity().runOnUiThread {  addCategory(imageUploadResponse) }
        }
    }

}