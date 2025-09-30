package com.frabon.rememberthedate.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frabon.rememberthedate.data.Event
import com.frabon.rememberthedate.data.EventRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AddEditViewModel(private val repository: EventRepository) : ViewModel() {

    fun insertEvent(event: Event) = viewModelScope.launch {
        repository.insertEvent(event)
    }

    fun updateEvent(event: Event) = viewModelScope.launch {
        repository.updateEvent(event)
    }

    fun deleteEvent(event: Event) = viewModelScope.launch {
        repository.deleteEvent(event)
    }

    fun getEventById(id: Int): Flow<Event> = repository.getEventById(id)

}