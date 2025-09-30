package com.frabon.rememberthedate.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.frabon.rememberthedate.R
import com.frabon.rememberthedate.RememberTheDateApplication
import com.frabon.rememberthedate.data.Event
import com.frabon.rememberthedate.databinding.FragmentAddEditEventBinding
import com.frabon.rememberthedate.viewmodels.AddEditViewModel
import com.frabon.rememberthedate.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class AddEditEventFragment : Fragment() {

    private var _binding: FragmentAddEditEventBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditEventFragmentArgs by navArgs()
    private var currentEvent: Event? = null

    private val addEditViewModel: AddEditViewModel by viewModels {
        ViewModelFactory((requireActivity().application as RememberTheDateApplication).repository)
    }

    private var selectedDay: Int = 0
    private var selectedMonth: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditEventBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load existing event data if editing
        if (args.eventId != -1) {
            setupMenu() // Only show delete menu when editing
            lifecycleScope.launch {
                currentEvent = addEditViewModel.getEventById(args.eventId).first()
                currentEvent?.let { populateUi(it) }
            }
        }

        binding.dateEditText.setOnClickListener {
            showDatePickerDialog()
        }

        binding.saveButton.setOnClickListener {
            saveEvent()
        }
    }

    private fun populateUi(event: Event) {
        binding.nameEditText.setText(event.name)
        binding.yearOfBirthEditText.setText(event.yearOfBirth?.toString() ?: "")
        selectedDay = event.day
        selectedMonth = event.month
        binding.dateEditText.setText("$selectedDay/$selectedMonth")
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, _, monthOfYear, dayOfMonth ->
                selectedDay = dayOfMonth
                selectedMonth = monthOfYear + 1
                binding.dateEditText.setText("$selectedDay/$selectedMonth")
            },
            year,
            month,
            day
        ).show()
    }

    private fun saveEvent() {
        val name = binding.nameEditText.text.toString().trim()
        val yearOfBirth = binding.yearOfBirthEditText.text.toString().toIntOrNull()

        if (name.isBlank() || selectedDay == 0) {
            Toast.makeText(context, "Please enter a name and select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val eventToSave = currentEvent?.copy(
            name = name,
            day = selectedDay,
            month = selectedMonth,
            yearOfBirth = yearOfBirth
        ) ?: Event(
            name = name,
            day = selectedDay,
            month = selectedMonth,
            yearOfBirth = yearOfBirth
        )

        if (currentEvent == null) {
            addEditViewModel.insertEvent(eventToSave)
        } else {
            addEditViewModel.updateEvent(eventToSave)
        }
        findNavController().navigateUp()
    }

    private fun deleteEvent() {
        currentEvent?.let {
            addEditViewModel.deleteEvent(it)
            findNavController().navigateUp()
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.edit_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete -> {
                        deleteEvent()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}