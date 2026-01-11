package com.example.projekuasam

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    // UI Variables
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnSignIn: Button
    private lateinit var btnGoogle: Button
    private lateinit var tvSignUp: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // 1. Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // 2. Initialize Views using the NEW XML IDs
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnSignIn = findViewById(R.id.btnSignIn)
        btnGoogle = findViewById(R.id.btnGoogle)
        tvSignUp = findViewById(R.id.tvSignUp)

        // 3. Setup Google Sign In Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // 4. Button Listeners

        // Login Button
        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(this, "Please Fill in All Fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Button
        btnGoogle.setOnClickListener {
            signInWithGoogle()
        }

        // Sign Up Link (Replaces your old Register Button)
        setupSignUpLink()
    }

    // --- YOUR ORIGINAL LOGIC FUNCTIONS ---

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // This function is now called when clicking the "Sign Up" text link
    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Registration successful, welcome ${user?.email}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- GOOGLE SIGN IN LOGIC ---

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    Toast.makeText(this, "Welcome ${user?.displayName}", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Authentication Failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // --- STYLING FOR THE SIGN UP LINK ---

    private fun setupSignUpLink() {
        val fullText = "Don't have an account? Sign Up"
        val spannableString = SpannableString(fullText)
        val clickableColor = Color.parseColor("#FFC107") // Yellow

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: android.view.View) {
                // When clicked, grab the text from the boxes and try to Register
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (email.isNotEmpty() && password.isNotEmpty()) {
                    registerUser(email, password)
                } else {
                    Toast.makeText(this@LoginActivity, "To Sign Up, please enter an Email and Password above.", Toast.LENGTH_LONG).show()
                }
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = clickableColor
                ds.isUnderlineText = false
            }
        }

        val startIndex = fullText.indexOf("Sign Up")
        if (startIndex != -1) {
            spannableString.setSpan(clickableSpan, startIndex, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            spannableString.setSpan(ForegroundColorSpan(clickableColor), startIndex, fullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        tvSignUp.text = spannableString
        tvSignUp.movementMethod = LinkMovementMethod.getInstance()
    }
}