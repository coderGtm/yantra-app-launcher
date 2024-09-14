package com.coderGtm.yantra.terminal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coderGtm.yantra.models.Suggestion
import java.util.Collections

class PrimarySuggestionsReorderAdapter(
    val suggestions: MutableList<Suggestion>,
    private val onReorder: (List<Suggestion>) -> Unit
) : RecyclerView.Adapter<PrimarySuggestionsReorderAdapter.SuggestionViewHolder>(), ItemTouchHelperAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1, parent, false)
        return SuggestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(suggestions[position].text)
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
        fun bind(suggestionText: String) {
            (itemView as TextView).text = suggestionText
        }
    }
}

interface ItemTouchHelperAdapter {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean
}