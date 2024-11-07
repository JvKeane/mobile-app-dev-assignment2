package com.example.locationfinder

// This class is to represent location data, this data is
// what will be added to the database rows and columns
class LocationData(var street: String, val lat: Float, val long: Float) {

    companion object {

        // This function is responsible for standardizing the street addresses
        // It does so by converting all short form variants to their complete versions
        fun standardizeAddress(address: String): String {
            val abbreviations = mapOf(
                "St" to "Street",
                "Ave" to "Avenue",
                "Rd" to "Road",
                "Blvd" to "Boulevard",
                "Dr" to "Drive",
                "Ln" to "Lane",
                "Ct" to "Court",
                "Pl" to "Place",
                "Sq" to "Square",
                "Cir" to "Circle",
                "Ter" to "Terrace",
                "Hwy" to "Highway",
                "Pky" to "Parkway",
                "Expy" to "Expressway",
                "Rte" to "Route",
                "Bch" to "Beach",
                "Ctr" to "Center",
                "Cres" to "Crescent",
                "Grv" to "Grove",
                "Lk" to "Lake",
                "Mnr" to "Manor",
                "Pz" to "Plaza",
                "Sta" to "Station",
                "Trl" to "Trail",
                "Vly" to "Valley",
                "N" to "North",
                "S" to "South",
                "E" to "East",
                "W" to "West",
                "NE" to "Northeast",
                "NW" to "Northwest",
                "SE" to "Southeast",
                "SW" to "Southwest"
            )
            var standard = address;
            for ((abbreviation, complete) in abbreviations){
                standard = standard.replace(Regex("\\b$abbreviation\\b", RegexOption.IGNORE_CASE), complete)
            }

            // This capitalizes the first letter of every word in the string
            standard = standard.split(" ")
                .joinToString(" ") { word -> word.lowercase().replaceFirstChar { it.uppercase() } }
            return standard
        }
    }
}