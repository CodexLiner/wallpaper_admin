package me.meenagopal24.recyclerdemo;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AsyncTask extends android.os.AsyncTask<Void, Void, ImageUploadResponse> {
    ImageUpload imageUpload;
    Bitmap bitmap;
    String category;
    String uri;

    public AsyncTask(ImageUpload imageUpload, Bitmap bitmap, String category, String uri) {
        this.imageUpload = imageUpload;
        this.bitmap = bitmap;
        this.category = category;
        this.uri = uri;
    }

    public static void UploadImage() {

    }

    @Override
    protected ImageUploadResponse doInBackground(Void... voids) {
        OkHttpClient client = new OkHttpClient();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        File file = new File("path/to/file.jpg"); // Replace with your file path
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), byteArray))
                .addFormDataPart("category", category) // Additional form data
                .build();

        Request request = new Request.Builder()
                .url("https://meenagopal24.me/wallpaperapi/upload.php") // Replace with your endpoint
                .post(requestBody)
                .build();

        Response response = null;
        try {
            response = client.newCall(request).execute();
            Gson gson = new Gson();
            assert response.body() != null;
            return gson.fromJson(response.body().string() , ImageUploadResponse.class);
        } catch (IOException e) {
            Log.d("TAG", "doInBackground: "+e);
        }
        return null;
    }
    @Override
    protected void onPostExecute(ImageUploadResponse imageUploadResponse) {
        super.onPostExecute(imageUploadResponse);
        if (imageUploadResponse!=null){
            imageUpload.onImageUploaded(imageUploadResponse);
        }

    }
}
