package com.coderGtm.yantra.terminal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.models.Suggestion
import java.util.Collections

class PrimarySuggestionsReorderAdapter(
    val suggestions: MutableList<Suggestion>,
    private val onReorder: (List<Suggestion>) -> Unit
) : RecyclerView.Adapter<PrimarySuggestionsReorderAdapter.SuggestionViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_suggestion, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        val sug = suggestions[position]
        holder.tvSuggestion.text = sug.text
        if (sug.isHidden) {
            holder.ivVisibility.setImageResource(R.drawable.round_visibility_off_24)
        } else {
            holder.ivVisibility.setImageResource(R.drawable.round_visibility_24)
        }

        holder.ivVisibility.setOnClickListener {
            sug.isHidden = !sug.isHidden
            notifyItemChanged(position)
        }
    }

    override fun getItemCount() = suggestions.size

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(suggestions, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        onReorder(suggestions)  // Call the reorder function
        return true
    }

    fun updateCommands(newSuggestions: List<Suggestion>) {
        suggestions.clear()
        suggestions.addAll(newSuggestions)
        notifyDataSetChanged()
    }


    inner class SuggestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSuggestion: TextView = itemView.findViewById(R.id.suggestionText)
        val ivVisibility: ImageView = itemView.findViewById(R.id.visibilityIcon)
    }
}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}