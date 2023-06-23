package me.meenagopal24.recyclerdemo

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class CustomSpinnerAdapter(context: Context, items: ArrayList<String>) :
    ArrayAdapter<String>(context, R.layout.simple_item, items) {

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate the custom layout for the spinner item view
        val view = LayoutInflater.from(context).inflate(R.layout.spinner_item_single, parent, false)

        // Get a reference to the TextView in the custom layout
        val textView = view.findViewById<TextView>(R.id.item_text)

        // Set the text for the TextView
        textView.text = getItem(position)

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        // Inflate the custom layout for the dropdown item view
        val view = LayoutInflater.from(context).inflate(R.layout.simple_item, parent, false)

        // Get a reference to the TextView in the custom layout
        val textView = view.findViewById<TextView>(R.id.item_text)

        // Set the text for the TextView
        textView.text = getItem(position)

        return view
    }
}
