package com.example.alertpointdraft

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.alertpointdraft.R
import java.util.Arrays


private lateinit var streetDropdown: AutoCompleteTextView
class SignUp : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)


            streetDropdown = findViewById(R.id.streetDropdown)
            val streets: Array<String> = resources.getStringArray(R.array.street_options)
            val adapter = ArrayAdapter(this, R.layout.list_item_street, streets)
            streetDropdown.setAdapter(adapter)
        }
}