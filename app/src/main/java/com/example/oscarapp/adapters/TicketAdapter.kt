package com.example.oscarapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oscarapp.R
import com.example.oscarapp.models.Ticket

class TicketAdapter(
    private var tickets: List<Ticket>,
    private val onItemClick: (Ticket) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>(), Filterable {

    private var filteredTickets: List<Ticket> = tickets

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        val ticket = filteredTickets[position]
        holder.bind(ticket, onItemClick)
    }

    override fun getItemCount(): Int = filteredTickets.size

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = if (constraint.isNullOrEmpty()) {
                    tickets
                } else {
                    val filterPattern = constraint.toString().lowercase().trim()
                    tickets.filter {
                        it.titulo.lowercase().contains(filterPattern) ||
                                it.numTicket.lowercase().contains(filterPattern) ||
                                it.observaciones.lowercase().contains(filterPattern) ||
                                it.ubicacionActividad.lowercase().contains(filterPattern)
                    }
                }
                return FilterResults().apply { values = filteredList }
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredTickets = results?.values as List<Ticket>
                notifyDataSetChanged()
            }
        }
    }

    class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ticketTitle: TextView = itemView.findViewById(R.id.ticket_title)
        private val ticketNumber: TextView = itemView.findViewById(R.id.ticket_number)
        private val ticketObservations: TextView = itemView.findViewById(R.id.ticket_observations)
        private val ticketLocation: TextView = itemView.findViewById(R.id.ticket_location)

        fun bind(ticket: Ticket, onItemClick: (Ticket) -> Unit) {
            ticketTitle.text = ticket.titulo
            ticketNumber.text = "#orden ${ticket.numTicket}"
            ticketObservations.text = ticket.observaciones
            ticketLocation.text = ticket.ubicacionActividad
            itemView.setOnClickListener { onItemClick(ticket) }
        }
    }
}
