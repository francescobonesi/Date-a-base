package com.frabon.rememberthedate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.map
import com.frabon.rememberthedate.data.Event
import com.frabon.rememberthedate.data.EventRepository
import com.frabon.rememberthedate.ui.UiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import java.time.Month

class MainViewModel(repository: EventRepository) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    // This flow will react to changes in the search query
    private val eventsFlow = searchQuery.flatMapLatest { query ->
        if (query.isEmpty()) {
            repository.allEvents
        } else {
            repository.searchEvents("%$query%") // Add wildcards for LIKE query
        }
    }

    // This remains the same, but it now operates on the filtered flow
    val groupedEvents = eventsFlow.asLiveData().map { events ->
        groupEventsByMonth(events)
    }

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    private fun groupEventsByMonth(events: List<Event>): List<UiItem> {
        val items = mutableListOf<UiItem>()
        if (events.isEmpty()) return items

        val groupedByMonth = events.groupBy { it.month }

        groupedByMonth.keys.sorted().forEach { monthNumber ->
            items.add(UiItem.Header(Month.of(monthNumber).name))
            groupedByMonth[monthNumber]?.forEach { event ->
                items.add(UiItem.EventItem(event))
            }
        }
        return items
    }
}