package com.example.alertpointdraft

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
// Removed: import androidx.core.view.ViewCompat (not directly used in this revised code)
// Removed: import androidx.core.view.WindowInsetsCompat (not directly used in this revised code)
import com.example.alertpointdraft.R // Ensure this is your project's R class
import com.google.firebase.Firebase

// Firebase imports - Assuming these are now resolving correctly
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

// Removed: import java.util.Arrays (not directly used)
// Removed: import firebase.auth.FirebaseAuth (incorrect import, should be com.google.firebase.auth.FirebaseAuth)


class SignUp : AppCompatActivity() {
    // Firebase instances
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    // UI Elements - Declare all necessary EditTexts and the Button
    private lateinit var houseNumberEditText: EditText
    private lateinit var streetDropdown: AutoCompleteTextView // This was your global, now a class member
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var getStartedButton: Button // This will be our sign-up trigger
    private lateinit var progressBar: ProgressBar // For loading indication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)

        // Initialize Firebase Auth and Firestore
        auth = Firebase.auth
        firestore = Firebase.firestore

        // Initialize UI Elements (Match these IDs with your activity_sign_up.xml)
        houseNumberEditText = findViewById(R.id.houseNumberEditText)
        streetDropdown = findViewById(R.id.streetDropdown) // This was already being initialized
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        phoneEditText = findViewById(R.id.phoneEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        getStartedButton = findViewById(R.id.getStartedButton) // This is the button that triggers sign-up
        progressBar = findViewById(R.id.signUpProgressBar) // Add a ProgressBar with this ID to your XML

        // Setup for the street dropdown
        val streets: Array<String> = resources.getStringArray(R.array.street_options)
        val adapter = ArrayAdapter(this, R.layout.list_item_street, streets)
        streetDropdown.setAdapter(adapter)

        // Set OnClickListener for the "Get Started" button to perform registration
        getStartedButton.setOnClickListener {
            registerUserAndSaveDetails()
        }
    }

    private fun registerUserAndSaveDetails() {
        val houseNumber = houseNumberEditText.text.toString().trim()
        val street = streetDropdown.text.toString().trim() // Get selected street
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val password = passwordEditText.text.toString().trim()

        // --- Basic Validation (Enhance this as needed) ---
        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailEditText.error = "Enter a valid email"
            emailEditText.requestFocus()
            return
        }
        if (password.isEmpty()) {
            passwordEditText.error = "Password is required"
            passwordEditText.requestFocus()
            return
        }
        if (password.length < 6) {
            passwordEditText.error = "Password must be at least 6 characters"
            passwordEditText.requestFocus()
            return
        }
        if (firstName.isEmpty()) {
            firstNameEditText.error = "First name is required"
            firstNameEditText.requestFocus()
            return
        }
        if (lastName.isEmpty()) {
            lastNameEditText.error = "Last name is required"
            lastNameEditText.requestFocus()
            return
        }
        if (houseNumber.isEmpty()) {
            houseNumberEditText.error = "House number is required"
            houseNumberEditText.requestFocus()
            return
        }
        if (street.isEmpty()) {
            // For AutoCompleteTextView, error is usually shown differently or via Toast
            Toast.makeText(this, "Street is required", Toast.LENGTH_SHORT).show()
            streetDropdown.requestFocus()
            return
        }
        if (phone.isEmpty()) {
            phoneEditText.error = "Phone number is required"
            phoneEditText.requestFocus()
            return
        }
        // You might want to add more specific validation for phone number format, house number etc.
        // --- End Basic Validation ---

        progressBar.visibility = View.VISIBLE
        getStartedButton.isEnabled = false // Disable button to prevent multiple clicks

        // 1. Create User in Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success
                    Log.d("SignUpActivity", "createUserWithEmail:success")
                    val firebaseUser = auth.currentUser
                    firebaseUser?.let {
                        // 2. Save Additional User Details to Firestore
                        saveUserDetailsToFirestore(
                            userId = it.uid,
                            houseNumber = houseNumber,
                            street = street,
                            firstName = firstName,
                            lastName = lastName,
                            phone = phone,
                            email = email // Storing email again for easier querying if needed
                        )
                    } ?: run {
                        // Handle case where firebaseUser is null (should not happen if task is successful)
                        Log.e("SignUpActivity", "FirebaseUser is null after successful creation.")
                        Toast.makeText(baseContext, "Authentication succeeded but failed to get user.", Toast.LENGTH_SHORT).show()
                        progressBar.visibility = View.GONE
                        getStartedButton.isEnabled = true
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("SignUpActivity", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    progressBar.visibility = View.GONE
                    getStartedButton.isEnabled = true
                }
            }
    }

    private fun saveUserDetailsToFirestore(
        userId: String,
        houseNumber: String,
        street: String,
        firstName: String,
        lastName: String,
        phone: String,
        email: String
    ) {
        val userProfile = hashMapOf(
            "userId" to userId, // Storing UID explicitly can be useful
            "houseNumber" to houseNumber,
            "street" to street,
            "firstName" to firstName,
            "lastName" to lastName,
            "phone" to phone,
            "email" to email, // Redundant if this is the auth email, but can be convenient
            "createdAt" to com.google.firebase.Timestamp.now() // Timestamp of profile creation
        )

        // Save to Firestore in a "users" collection, with the document ID being the user's UID
        firestore.collection("users").document(userId)
            .set(userProfile)
            .addOnSuccessListener {
                Log.d("SignUpActivity", "User profile successfully written to Firestore!")
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                // Navigate to the VerifyCode activity
                val intent = Intent(this, VerifyCode::class.java)
                // Optionally clear the back stack if you don't want users to return to SignUp
                // intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish() // Close SignUpActivity
            }
            .addOnFailureListener { e ->
                Log.w("SignUpActivity", "Error writing user profile to Firestore", e)
                Toast.makeText(this, "Failed to save user details: ${e.message}", Toast.LENGTH_LONG).show()
                progressBar.visibility = View.GONE
                getStartedButton.isEnabled = true
                // CRITICAL: Consider how to handle this failure.
                // Should you delete the user from Authentication if saving details fails?
                // This prevents orphaned auth accounts without profile data.
                // auth.currentUser?.delete()?.addOnCompleteListener { deleteTask ->
                //     if (deleteTask.isSuccessful) {
                //         Log.d("SignUpActivity", "Orphaned auth user deleted.")
                //     } else {
                //         Log.w("SignUpActivity", "Failed to delete orphaned auth user.", deleteTask.exception)
                //     }
                // }
            }
    }
}
