package com.coderGtm.yantra.gui.contextmenu.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.coderGtm.yantra.R
import com.coderGtm.yantra.gui.contextmenu.model.ContextAction

class ContextMenuAdapter(

    private val actions: List<ContextAction>,

    private val onActionClick: (ContextAction) -> Unit

) : RecyclerView.Adapter<ContextMenuAdapter.ViewHolder>() {

    class ViewHolder(
        view: View
    ) : RecyclerView.ViewHolder(view) {

        val icon: ImageView =
            view.findViewById(R.id.actionIcon)

        val title: TextView =
            view.findViewById(R.id.actionTitle)

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(
                R.layout.item_context_action,
                parent,
                false
            )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val action = actions[position]

        holder.title.text = action.title
        holder.icon.setImageResource(action.iconRes)

        holder.itemView.isEnabled = action.enabled
        holder.itemView.alpha =
            if (action.enabled) 1f else 0.45f

        holder.itemView.setOnClickListener {

            if (action.enabled) {
                onActionClick(action)
            }

        }

    }

    override fun getItemCount(): Int {

        return actions.size

    }

}