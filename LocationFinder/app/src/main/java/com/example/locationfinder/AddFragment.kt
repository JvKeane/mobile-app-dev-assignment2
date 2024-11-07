package com.example.locationfinder

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class AddFragment : Fragment() {
    private lateinit var db : DatabaseHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Provides an instance of the database for the fragment to work with
        db = (activity as MainActivity).db

        // Gets the searchAddressInput (Edit Text) and adds an onTextChanged listener to it
        // Also gets the suggestedSearchView where suggested results will be displayed to the user
        // for enhanced user experience
        val streetAddressInput = view.findViewById<EditText>(R.id.streetAddressInput)
        val suggestedSearchView = view.findViewById<TextView>(R.id.suggestedResults)

        // Adds an onTextChanged listener to searchAddressInput so it can provide the user with suggested results
        streetAddressInput.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            // Logic that is executed when text is changed in searchAddressInput
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                var input = s.toString()
                input = LocationData.standardizeAddress(input)
                if(input.isNotEmpty()){

                    // Again queries suggestions from the database
                    val suggestions = db.suggestedAddress(input)

                    // Updates the suggested view to show any suggestions if found
                    // Otherwise displays default message
                    if (suggestions.isNotEmpty()){
                        suggestedSearchView.text = suggestions.joinToString("\n")
                    } else {
                        suggestedSearchView.text =  getString(R.string.no_recent)
                    }
                } else {
                    suggestedSearchView.text =  getString(R.string.no_recent)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Adds functionality to the various buttons on the page
        val addButton = view.findViewById<Button>(R.id.addBtn)
        val deleteButton = view.findViewById<Button>(R.id.deleteBtn)
        val updateButton = view.findViewById<Button>(R.id.updateBtn)
        val backButton = view.findViewById<Button>(R.id.backBtn)

        // Calls add function when clicked so data can be added to the database
        addButton.setOnClickListener {
            if (checkAllFields()) {
                addData()
            }
        }

        // Calls delete function when clicked so data can be removed from the database
        deleteButton.setOnClickListener{
            if (checkAddressField()){
                deleteData()
            }
        }

        // Adds functionality to update button so that when it is clicked
        // The update fragment can be started
        updateButton.setOnClickListener{

            // Checks if the address is not empty
            if (checkAddressField()){
                var streetAddress = streetAddressInput.text.toString()
                streetAddress = LocationData.standardizeAddress(streetAddress)

                // First checks to see that the street address actually exists in the database
                // otherwise it will display an error message.
                if (db.addressExists(streetAddress)){
                    val locationData = db.getLocation(streetAddress)

                    // Creates a bundle of the data to be edited and send this
                    // information to the next fragment
                    val fragment = UpdateFragment()
                    val bundle = Bundle()
                    if (locationData != null) {
                        bundle.putString("street", locationData.street)
                        bundle.putString("latitude", locationData.lat.toString())
                        bundle.putString("longitude", locationData.long.toString())
                        fragment.arguments = bundle
                        (activity as MainActivity).loadFragment(fragment)
                    }
                } else {
                    Toast.makeText(requireContext(), "The address you are looking to edit does not exist",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Adds functionality to back button so the user will be guided back to the search fragment
        backButton.setOnClickListener{
            (activity as MainActivity).loadFragment(SearchFragment())
        }
    }

    // Simple function to check if a variable types is of type float
    private fun isFloat(float: String): Boolean{
        try{
            float.toFloat()
            return true
        } catch (e: NumberFormatException){
            return false
        }
    }

    // Function to check that all fields have been assigned values
    private fun checkAllFields(): Boolean{
        val address = view?.findViewById<EditText>(R.id.streetAddressInput)?.text.toString()
        val latInput =  view?.findViewById<EditText>(R.id.latInput)?.text.toString()
        val longInput =  view?.findViewById<EditText>(R.id.longInput)?.text.toString()
        if (address.isBlank() || latInput.isBlank() || longInput.isBlank()){
            Toast.makeText(requireContext(), "Please enter values for all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Function to check that the address field is not empty
    private fun checkAddressField(): Boolean{
        val address =  view?.findViewById<EditText>(R.id.streetAddressInput)?.text.toString()
        if (address.isBlank()){
            Toast.makeText(requireContext(), "Please enter the address if you wish to delete or update an entry.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Logic to add data to the database
    private fun addData(){
        // Gets the input from the EditText fields from the user
        val streetAddressInput = view?.findViewById<EditText>(R.id.streetAddressInput)
        val latInput = view?.findViewById<EditText>(R.id.latInput)
        val longInput = view?.findViewById<EditText>(R.id.longInput)
        val lat: Float
        val long: Float

        // Validates user input for the lat and long fields, ensures that they are of the right type
        // Otherwise it notifies the user to enter valid input
        if (isFloat(latInput?.text.toString()) && isFloat(longInput?.text.toString())){
            lat = latInput?.text.toString().toFloat()
            long = longInput?.text.toString().toFloat()

            // Standardizes the address that the user entered
            val address = LocationData.standardizeAddress(streetAddressInput?.text.toString())

            // Checks if the address already exists in the database
            // this is to ensure there are no duplicates. Otherwise it tells the user
            // the address already exists.
            if (!db.addressExists(address)){

                // Creates LocationData to be added to the database then adds it
                val locationData = LocationData(street = address, lat = lat, long = long)
                val status = db.addLocation(locationData)

                // If the add was success full then it clears all input fields and displays a message
                // If for some reason the add failed then the user will be notified
                if (status) {
                    streetAddressInput?.text = null
                    latInput?.text = null
                    longInput?.text = null
                    Toast.makeText(requireContext(), "Your data was successfully added", Toast.LENGTH_SHORT)
                        .show()
                }
                else {
                    Toast.makeText(requireContext(), "An error occurred while adding data", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "This address already exists in the database", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Please enter valid values for Latitude and Longitude", Toast.LENGTH_SHORT).show()
        }
    }

    // Logic to delete data
    private fun deleteData(){
        // Gets the users address input of the address they'd like to delete
        val streetAddressInput = view?.findViewById<EditText>(R.id.streetAddressInput)
        val latInput = view?.findViewById<EditText>(R.id.latInput)
        val longInput = view?.findViewById<EditText>(R.id.longInput)
        val address = streetAddressInput?.text.toString()

        // Removes the address from the database and notifies the user
        val status = db.removeLocation(address)

        // On a successful delete address fields will be cleared
        if (status){
            streetAddressInput?.text = null
            latInput?.text = null
            longInput?.text = null
            Toast.makeText(requireContext(), "Item was successfully removed", Toast.LENGTH_SHORT).show()
        } else{
            Toast.makeText(requireContext(), "Entry could not be found, or it could not be deleted.\nPlease try again.", Toast.LENGTH_SHORT).show()
        }
    }

}