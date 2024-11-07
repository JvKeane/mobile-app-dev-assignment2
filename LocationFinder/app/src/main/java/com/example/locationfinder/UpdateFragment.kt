package com.example.locationfinder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class UpdateFragment : Fragment() {
    private lateinit var db : DatabaseHelper


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_update, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Provides an instance of the database for the fragment to work with
        db = (activity as MainActivity).db

        // Sets the street address passed from the other fragment to be displayed
        // SO the user knows what entry they are modifying
        val editTarget = view.findViewById<TextView>(R.id.editTarget)
        val streetAddress = arguments?.getString("street")
        editTarget.text = streetAddress

        // Creates instances of the fragments buttons
        val saveButton = view.findViewById<Button>(R.id.saveBtn)
        val cancelButton = view.findViewById<Button>(R.id.cancelBtn)

        // Auto fills input fields with the data from the database that needs to be modified
        val streetAddressInput = view.findViewById<EditText>(R.id.streetAddressInput)
        val latInput = view.findViewById<EditText>(R.id.latInput)
        val longInput = view.findViewById<EditText>(R.id.longInput)
        streetAddressInput.setText(streetAddress)
        latInput.setText(arguments?.getString("latitude"))
        longInput.setText(arguments?.getString("longitude"))

        // Adds functionality to the save button
        saveButton.setOnClickListener{

            // Ensures that all fields have been checked
            if (checkAllFields()){
                // Ensures that the address that was passed to the fragment is not null
                // This address is needed because it is this entry that will be queried and updated in the database
                if (streetAddress != null) {
                    // Saves the modified string to the database and on a successful save the user goes back to the add fragment
                    if (saveData(streetAddress)) {
                        (activity as MainActivity).loadFragment(AddFragment())
                    }
                }
            }
        }

        // Adds functionality to the cancel button
        // When clicked the user will go back to the add fragment
        cancelButton.setOnClickListener{
            (activity as MainActivity).loadFragment(AddFragment())
        }
    }

    // Function to check that all fields have been assigned values
    private fun checkAllFields(): Boolean{
        val address = view?.findViewById<EditText>(R.id.streetAddressInput)?.text.toString()
        val latInput = view?.findViewById<EditText>(R.id.latInput)?.text.toString()
        val longInput = view?.findViewById<EditText>(R.id.longInput)?.text.toString()
        if (address.isBlank() || latInput.isBlank() || longInput.isBlank()){
            Toast.makeText(requireContext(), "Please enter values for all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Simple function to check whether a variable is of type float or not
    private fun isFloat(float: String): Boolean{
        try{
            float.toFloat()
            return true
        } catch (e: NumberFormatException){
            return false
        }
    }

    // Logic to save data to the database
    private fun saveData(originalAddress: String): Boolean{

        // Gets input from user on what data they want saved to the database
        var address = view?.findViewById<EditText>(R.id.streetAddressInput)?.text.toString()
        val latInput = view?.findViewById<EditText>(R.id.latInput)?.text.toString()
        val longInput = view?.findViewById<EditText>(R.id.longInput)?.text.toString()
        val lat: Float
        val long: Float

        // validates if the users input is valid for the lat and long
        if (isFloat(latInput) && isFloat(longInput)){
            lat = latInput.toFloat()
            long = longInput.toFloat()

            // Standardizes the users input to be stored in the database
            address = LocationData.standardizeAddress(address)

            // Ensures that the address they have entered (if they modified it) does not already
            // exist in the database. If it does exist the user in notified.
            if (address == originalAddress || !db.addressExists(address)){

                // Creates LocationData to be saved to the data base
                val locationData = LocationData(street = address, lat = lat, long = long)

                // Saves the location data to the database where the OLD entry was
                val status = db.updateLocation(originalAddress, locationData)

                // On a successful save the user is notified. Otherwise they are notified that an error occurred
                if (status) {
                    Toast.makeText(requireContext(), "Your data was successfully updated", Toast.LENGTH_SHORT)
                        .show()
                    return true
                } else {
                    Toast.makeText(requireContext(), "An error occurred while updating the data", Toast.LENGTH_SHORT)
                        .show()
                }
            }else {
                Toast.makeText(requireContext(), "This target address already exists, please enter a different address.", Toast.LENGTH_SHORT).show()
            }
        } else{
            Toast.makeText(requireContext(), "Please enter valid values for Latitude and Longitude", Toast.LENGTH_SHORT).show()
        }
        return false
    }

}