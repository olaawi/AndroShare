package com.example.androshare

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class ParticipantsAdapter(val context: Context, private val participantsList: ArrayList<User>) :
    BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var view = convertView
        if (view == null) {
            holder = ViewHolder()
            val inflater = context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            view = inflater.inflate(R.layout.participant_in_list, null, true)
            holder.name = view.findViewById(R.id.name) as TextView
            holder.avatar = view.findViewById(R.id.avatar) as ImageView
            view.tag = holder
        } else {
            holder = view.tag as ViewHolder
        }
        val name =
            participantsList[position].givenName + " " + participantsList[position].familyName
        holder.name!!.text = name
        holder.avatar!!.setImageResource(participantsList[position].avatar)
        return view!!
    }

    override fun getItem(position: Int): Any {
        return participantsList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        Log.d("Adapter", participantsList.size.toString())
        return participantsList.size
    }

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    private inner class ViewHolder {
        var name: TextView? = null
        var avatar: ImageView? = null
    }

}