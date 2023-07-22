package me.meenagopal24.recyclerdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class RecyclerAdapter(private val list: MutableList<wallpapers.item>)  : RecyclerView.Adapter<RecyclerAdapter.Holder>() {
    class Holder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val sImage: ImageView = itemView.findViewById(R.id.staggered_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.staggered_single, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return list.size ?: 0
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val BASE_URL_IMAGE = "https://meenagopal24.me/wallpaperapi/uploads/"
        Glide.with(holder.itemView.context)
            .load(BASE_URL_IMAGE + list[position].category.trim() + "/" + list[position].image.trim())
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .placeholder(R.drawable.img).into(holder.sImage)
    }
}