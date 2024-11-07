package com.example.locationfinder


import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class SearchFragment : Fragment() {

    private lateinit var db : DatabaseHelper

    // Inflates view on creation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Provides an instance of the database for the fragment to work with
        db = (activity as MainActivity).db

        // Gets the searchInput (AutoTextView) and creates an adapter and sets it to the AutoTextView
        val searchInput = view.findViewById<AutoCompleteTextView>(R.id.searchInput)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf<String>())
        searchInput.setAdapter(adapter)

        // Adds an onTextChanged listener to searchInput so it can provide the user with suggested results
        searchInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // Logic that is executed when text is changed in searchInput
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var input = s.toString()
                input = LocationData.standardizeAddress(input)
                if(input.isNotEmpty()) {

                    // Queries search suggestions based on the users input from the database
                    val suggestions = db.suggestedAddress(input)
                    if (suggestions.isNotEmpty()) {

                        // Updates the searchInput to show any suggestions
                        adapter.clear()
                        adapter.addAll(suggestions)
                        adapter.notifyDataSetChanged()
                        searchInput.showDropDown()
                        } else {
                            searchInput.dismissDropDown()
                        }
                } else {
                    searchInput.dismissDropDown()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val editButton = view.findViewById<Button>(R.id.editBtn)
        val searchButton = view.findViewById<Button>(R.id.searchBtn)

        // Adds navigation functionality to the edit button so when it is clicked the user will be taken to
        // the AddFragment
        editButton.setOnClickListener{
            (activity as MainActivity).loadFragment(AddFragment())
        }

        // Adds functionality to the search button so that when the user presses search
        // their search is queried from the database
        searchButton.setOnClickListener{
            var address = searchInput.text.toString()
            if (validateAddress(address)){
                address = LocationData.standardizeAddress(address)
                onSubmit(address)
            } else{
                Toast.makeText(requireContext(), "Please enter a value in the search field", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // A check to ensure the search box is not empty
    private fun validateAddress(address: String): Boolean{
        return address.isNotEmpty()
    }

    // The logic that is executed to query the search results from the database
    private fun onSubmit(address: String){
        val searchResults =  view?.findViewById<TextView>(R.id.searchResults)
        val locationData = db.getLocation(address)

        if (locationData == null){
            // Checks to ensure that the user entered a valid search
            Toast.makeText(requireContext(), "Please enter a valid address in the search field", Toast.LENGTH_SHORT).show()
        } else{
            // Updates the view with queried data results
            val street = locationData.street
            val lat = locationData.lat.toString()
            val long = locationData.long.toString()
            val results = "Street Address: $street\nLatitude: $lat\nLongitude: $long"
            searchResults?.text = results
        }
    }

}