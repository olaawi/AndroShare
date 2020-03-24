import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.androshare.Event
import com.example.androshare.R

class EventAdapter(var context: Context, val events: ArrayList<Event?>, private val clickListener: (Event) -> Unit) : RecyclerView.Adapter<EventAdapter.EventHolder>() {


    class EventHolder(val view: View) : RecyclerView.ViewHolder(view){
        var title: TextView = view.findViewById(R.id.titleId)
        var description: TextView = view.findViewById(R.id.descriptionId)
        var time : TextView = view.findViewById(R.id.timeId)

        fun bind(event: Event, clickListener: (Event) -> Unit) {
            itemView.setOnClickListener { clickListener(event)}
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.event_in_dashboard, null)
        return EventHolder(view)
    }


    override fun onViewRecycled(holder: EventHolder) {
        // TODO implement
        super.onViewRecycled(holder)
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.title.text = events[position]!!.title
        holder.description.text = events[position]!!.description
        holder.time.text = events[position]!!.getTime()
        holder.bind(events[position]!!, clickListener)
    }

    override fun getItemCount(): Int {
        return events.size
    }



}
