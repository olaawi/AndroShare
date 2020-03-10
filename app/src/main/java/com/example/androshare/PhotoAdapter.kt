package com.example.androshare

import android.content.Context
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.BaseAdapter
import android.widget.ImageView
import com.bumptech.glide.Glide


class PhotoAdapter(var context: Context, var images: List<Uri>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val imageView: ImageView
        if (convertView == null) { // if it's not recycled, initialize some attributes
            imageView = ImageView(context)
            imageView.layoutParams = AbsListView.LayoutParams(100, 100)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView.setPadding(8, 8, 8, 8)
            imageView.setOnClickListener {
                // TODO implement on image click
            }
        } else {
            imageView = convertView as ImageView
        }
        // TODO enable this (temporarily disabled to limit access to storage)
//        Glide.with(context).load(images[position]).into(imageView)
        return imageView
    }

    override fun getItem(position: Int): Any {
        return images[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return images.size
    }
}