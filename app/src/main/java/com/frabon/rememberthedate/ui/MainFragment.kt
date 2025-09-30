package com.frabon.rememberthedate.ui

import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.frabon.rememberthedate.R
import com.frabon.rememberthedate.RememberTheDateApplication
import com.frabon.rememberthedate.databinding.FragmentMainBinding
import com.frabon.rememberthedate.viewmodels.MainViewModel
import com.frabon.rememberthedate.viewmodels.ViewModelFactory

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val mainViewModel: MainViewModel by viewModels {
        ViewModelFactory((requireActivity().application as RememberTheDateApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupMenu()

        val adapter = EventAdapter { event ->
            val action = MainFragmentDirections.actionMainFragmentToAddEditEventFragment(event.id)
            findNavController().navigate(action)
        }

        binding.recyclerView.adapter = adapter

        mainViewModel.groupedEvents.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }

        binding.fab.setOnClickListener {
            val action = MainFragmentDirections.actionMainFragmentToAddEditEventFragment(-1)
            findNavController().navigate(action)
        }
    }

    private fun setupMenu() {
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)

                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem.actionView as SearchView

                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(newText: String?): Boolean {
                        mainViewModel.setSearchQuery(newText.orEmpty())
                        return true
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Return false to allow the fragment to handle the event.
                return false
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}