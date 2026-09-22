package com.coderGtm.yantra.gui.drawer.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import androidx.recyclerview.widget.RecyclerView

import com.coderGtm.yantra.R
import com.coderGtm.yantra.gui.drawer.listeners.DrawerActionListener
import com.coderGtm.yantra.gui.drawer.model.DrawerItem

class AppDrawerAdapter(

    private val apps: List<DrawerItem>,

    private val listener: DrawerActionListener

) : RecyclerView.Adapter<AppDrawerAdapter.ViewHolder>() {

    class ViewHolder(

        view: View

    ) : RecyclerView.ViewHolder(view) {

        val icon: ImageView =
            view.findViewById(R.id.icon)

        val title: TextView =
            view.findViewById(R.id.title)

    }

    override fun onCreateViewHolder(

        parent: ViewGroup,

        viewType: Int

    ): ViewHolder {

        val view = LayoutInflater

            .from(parent.context)

            .inflate(

                R.layout.item_drawer_app,

                parent,

                false

            )

        return ViewHolder(view)

    }

    override fun onBindViewHolder(

        holder: ViewHolder,

        position: Int


    ) {

        val app = apps[position]

        holder.title.text = app.label

        holder.icon.setImageDrawable(app.icon)

        holder.itemView.setOnClickListener {

            listener.onAppClick(app)

        }

        holder.itemView.setOnLongClickListener {

            listener.onAppLongClick(app)

            true

        }

    }

    override fun getItemCount(): Int {

        return apps.size

    }

}