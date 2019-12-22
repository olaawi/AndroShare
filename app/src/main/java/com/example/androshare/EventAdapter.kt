import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.androshare.Event
import com.example.androshare.R
import kotlin.collections.mutableListOf

class EventAdapter(var context: Context, val events: Array<Event?>) : RecyclerView.Adapter<EventAdapter.EventHolder>() {


    class EventHolder(val view: View) : RecyclerView.ViewHolder(view){
        var image: ImageView = view.findViewById(R.id.imageId)
        var title: TextView = view.findViewById(R.id.titleId)
        var description: TextView = view.findViewById(R.id.descriptionId)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventAdapter.EventHolder {
        val view: View = LayoutInflater.from(parent.getContext()).inflate(R.layout.event, null)
        return EventHolder(view)
    }

    override fun onBindViewHolder(holder: EventHolder, position: Int) {
        holder.title.text = events[position]!!.title
        holder.description.text = events[position]!!.description
    }

    override fun getItemCount(): Int {
        return events.size
    }



}
