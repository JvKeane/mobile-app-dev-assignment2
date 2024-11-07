package com.example.locationfinder
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment


class MainActivity : AppCompatActivity() {

    internal lateinit var db: DatabaseHelper
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Creates database and gets preferences
        db = DatabaseHelper(this)
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)

        // Checks if the database has been populated if not it will populate it
        // (This happens on the first run of the app)
        if (!isDatabasePopulated()) {
            db.populateDatabase()
            setDatabasePopulated()
            // Notifies the user that the database has been populated
            Toast.makeText(
                this, "Database successfully initialized with 100+ entries",
                Toast.LENGTH_SHORT
            ).show()
        }

        // Loads the initial fragment (search fragment)
        if (savedInstanceState == null) {
            loadFragment(SearchFragment())
        }
    }

    // this function is responsible for loading fragments and it can be called by fragments
    fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    // This function is responsible for setting the database is populated preference variable to true
    // after the database has been populated
    private fun setDatabasePopulated() {
        sharedPreferences.edit().putBoolean("isDatabasePopulated", true).apply()
    }

    // This function is responsible for checking the isDatabasePopulated variable
    // It will return false if the variable doesn't exist
    private fun isDatabasePopulated(): Boolean {
        return sharedPreferences.getBoolean("isDatabasePopulated", false)
    }

}

