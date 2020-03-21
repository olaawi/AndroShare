package com.example.androshare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlin.properties.Delegates.observable


class ImageAdapter(private val images: ArrayList<Image>, var eventPage: EventPage) :
    RecyclerView.Adapter<ImageAdapter.ImageHolder>() {

    enum class MODE { REGULAR, SELECT }

    var mode: MODE by observable(MODE.REGULAR) { _, _, newValue ->
        if (newValue == MODE.SELECT)
            eventPage.setSelectLayout()
        else {
            eventPage.setRegularLayout()
        }
    }

    inner class ImageHolder(val context: Context, val view: View) : RecyclerView.ViewHolder(view) {
        var img: ImageView = view.findViewById(R.id.grid_image)
        var selectOverlay: View = view.findViewById(R.id.select_overlay)
        var checkBox: ImageView = view.findViewById(R.id.image_checkbox)


        fun bind(image: Image, position: Int) {
            itemView.setOnLongClickListener {
                image.toggleSelect()
                notifyDataSetChanged()
                mode = MODE.SELECT
                true
            }

            itemView.setOnClickListener {
                if (mode == MODE.SELECT) {
                    image.toggleSelect()
                    notifyDataSetChanged()
                } else {
                    eventPage.openImageViewer(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.image_in_grid, null)
        return ImageHolder(parent.context, view)
    }

    override fun onViewRecycled(holder: ImageHolder) {
        // TODO implement
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        Glide.with(holder.itemView.context).load(images[position].drawable).into(holder.img)
        if (mode == MODE.SELECT) {
            holder.checkBox.visibility = View.VISIBLE
            if (images[position].isSelected) {
                holder.selectOverlay.visibility = View.VISIBLE
                holder.checkBox.setImageResource(R.drawable.ic_check_circle_black_24dp)
            } else {
                holder.selectOverlay.visibility = View.INVISIBLE
                holder.checkBox.setImageResource(R.drawable.ic_radio_button_unchecked_black_24dp)
            }
        } else {
            holder.selectOverlay.visibility = View.INVISIBLE
            holder.checkBox.visibility = View.INVISIBLE
        }
        holder.bind(images[position], position)
    }

    override fun getItemCount(): Int {
        return images.size
    }

}
