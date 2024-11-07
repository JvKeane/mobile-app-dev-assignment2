package com.example.locationfinder

import android.annotation.SuppressLint
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import com.opencsv.CSVReader
import java.io.InputStreamReader


class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private var database: SQLiteDatabase? = null

    // This function is responsible for populating the database with the initial 100 entries
    fun populateDatabase(){
        val inStream = context.resources.openRawResource(R.raw.unique_gta_locations)
        val locations = mutableListOf<LocationData>()
        // Reads from the .csv file in the res/raw folder
        InputStreamReader(inStream).use {reader ->
            val csvReader = CSVReader(reader)
            val records = csvReader.readAll()
            var first = true;
            for (record in records) {
                // Skips header row
                if (!first){
                    locations.add(LocationData(street = LocationData.standardizeAddress(record[0]),
                        lat = record[1].toFloat(), long = record [2].toFloat()))
                } else {
                    first = false
                }
            }
            for (location in locations){
                // Ensures no duplicate addresses are added
                if (!addressExists(location.street)){
                    addLocation(location)
                }
            }
        }
    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "LocationFinderDatabase.db"
        const val TABLE_NAME = "locations_table"
        const val COLUMN_ID = "id"
        const val COLUMN_STREET_ADDRESS = "street"
        const val COLUMN_LATITUDE = "lat"
        const val COLUMN_LONGITUDE = "long"
    }

    override fun onCreate(db: SQLiteDatabase) {

        // Creates the database table
        val createTable = ("CREATE TABLE $TABLE_NAME ("
                + "$COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "$COLUMN_STREET_ADDRESS TEXT, "
                + "$COLUMN_LATITUDE FLOAT, "
                + "$COLUMN_LONGITUDE FLOAT)")
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    // Logic to add a new entry to the database, it will return an integer to represent its success
    fun addLocation(data: LocationData): Boolean{
        val db = getDatabase()
        val contentValues = ContentValues().apply {
            put(COLUMN_STREET_ADDRESS, data.street)
            put(COLUMN_LATITUDE, data.lat)
            put(COLUMN_LONGITUDE, data.long)
        }
        val status = db.insert(TABLE_NAME, null, contentValues)
        return status != -1L
    }

    // Logic to remove  a location from the database
    // Returns an integer to represents its success
    fun removeLocation(streetAddress: String): Boolean{
        val db = getDatabase()
        val where = "$COLUMN_STREET_ADDRESS = ?"
        val args = arrayOf(streetAddress)
        val status = db.delete(TABLE_NAME, where, args)
        return status > 0
    }

    // Logic to check if an address already exists in the database
    // Return a boolean to show the status
    fun addressExists(address: String): Boolean{
        val db = getDatabase()
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_STREET_ADDRESS = ?"
        val cursor = db.rawQuery(query, arrayOf(LocationData.standardizeAddress(address)))
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Ensures that only one instance of the database can get opened at a time
    private fun getDatabase(): SQLiteDatabase {
        if (database == null || !database!!.isOpen) {
            database = writableDatabase
        }
        return database!!
    }

    // Logic to query suggested results from the database
    // Return a list of suggested addresses
    fun suggestedAddress(streetAddress: String): List<String>{
        val suggestions = mutableListOf<String>()
        val db = getDatabase()

        // Splits the address into words
        val words = streetAddress.split(" ")

        // Makes a where clause to join the words so that if the user enters "Grove"
        // and an entry "123 Grove Street" is in the database, then it will match grove with that entry
        val whereClause = words.joinToString(" AND ") { "$COLUMN_STREET_ADDRESS LIKE ?" }
        val args = words.map { "%$it%" }.toTypedArray()
        var cursor: Cursor? = null

        try {
            // Will return the first 5 results. This is to ensure the user is not overloaded incase there are many matches
            cursor = db.query(
                TABLE_NAME,
                arrayOf(COLUMN_STREET_ADDRESS),
                whereClause,
                args,
                null,
                null,
                null,
                "5"
            )

            // Adds the results of the query to list
            if (cursor.moveToFirst()) {
                do {
                    suggestions.add(cursor.getString(cursor.getColumnIndexOrThrow(
                        COLUMN_STREET_ADDRESS
                    )))
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            Log.e("DatabaseError", "Error searching address", e)
        } finally {
            cursor?.close()
        }
        // Return the list of suggested addresses
        return suggestions
    }

    // Gets a particular location from the database queried by its street address
    @SuppressLint("Range")
    fun getLocation(streetAddress: String): LocationData?{
        val db = getDatabase()

        // Query to retrieve the entry from the database by its street address
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_STREET_ADDRESS = ?"
        val cursor = db.rawQuery(query, arrayOf(streetAddress))
        var locationData: LocationData? = null

        // Returns the first entry that matches and adds the data to a LocationData variable
        if (cursor.moveToFirst()){
            val address = cursor.getString(cursor.getColumnIndex(COLUMN_STREET_ADDRESS))
            val lat = cursor.getString(cursor.getColumnIndex(COLUMN_LATITUDE))
            val long = cursor.getString(cursor.getColumnIndex(COLUMN_LONGITUDE))
            locationData = LocationData(street = address, lat = lat.toFloat(), long = long.toFloat())
        }
        cursor.close()
        // Returns the location data
        return locationData
    }

    // Logic to update a locations information
    fun updateLocation(streetAddress: String, data: LocationData): Boolean{
        val db = getDatabase()
        val contentValues = ContentValues().apply {
            put(COLUMN_STREET_ADDRESS, data.street)
            put(COLUMN_LATITUDE, data.lat)
            put(COLUMN_LONGITUDE, data.long)
        }

        // Updates the provided street address with new location data
        val status = db.update(
            TABLE_NAME,
            contentValues,
            "$COLUMN_STREET_ADDRESS = ?",
            arrayOf(streetAddress)
        )
        db.close()
        // Returns an integer to indicates its success
        return status > 0
    }

}
