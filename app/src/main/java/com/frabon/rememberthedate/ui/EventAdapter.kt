package com.frabon.rememberthedate.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.frabon.rememberthedate.data.Event
import com.frabon.rememberthedate.databinding.ItemEventBinding
import com.frabon.rememberthedate.databinding.ItemHeaderBinding
import java.util.Calendar

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class EventAdapter(private val onEventClicked: (Event) -> Unit) :
    ListAdapter<UiItem, RecyclerView.ViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            ITEM_VIEW_TYPE_ITEM -> EventViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is EventViewHolder -> {
                val eventItem = getItem(position) as UiItem.EventItem
                holder.bind(eventItem.event, onEventClicked)
            }
            is HeaderViewHolder -> {
                val headerItem = getItem(position) as UiItem.Header
                holder.bind(headerItem.month)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is UiItem.Header -> ITEM_VIEW_TYPE_HEADER
            is UiItem.EventItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class EventViewHolder private constructor(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event, onEventClicked: (Event) -> Unit) {
            binding.eventName.text = event.name
            binding.eventDate.text = "${event.day}/${event.month}"

            // Calculate and display age if year of birth is available
            event.yearOfBirth?.let {
                val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                val age = currentYear - it
                binding.eventAge.visibility = View.VISIBLE
                binding.eventAge.text = "Turning $age"
            } ?: run {
                binding.eventAge.visibility = View.GONE
            }

            binding.root.setOnClickListener { onEventClicked(event) }
        }


        companion object {
            fun from(parent: ViewGroup): EventViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemEventBinding.inflate(layoutInflater, parent, false)
                return EventViewHolder(binding)
            }
        }
    }

    class HeaderViewHolder private constructor(private val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(month: String) {
            binding.headerTitle.text = month
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemHeaderBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<UiItem>() {
    override fun areItemsTheSame(oldItem: UiItem, newItem: UiItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UiItem, newItem: UiItem): Boolean {
        return oldItem == newItem
    }
}

sealed class UiItem {
    data class EventItem(val event: Event) : UiItem() {
        override val id = event.id.toLong()
    }

    data class Header(val month: String) : UiItem() {
        override val id = Long.MIN_VALUE + month.hashCode()
    }

    abstract val id: Long
}