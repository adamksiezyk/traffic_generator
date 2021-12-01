package com.example.trafficgenerator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.widget.EditText
import android.content.SharedPreferences
import android.graphics.Color
import com.example.trafficgenerator.databinding.ActivityLoginBinding


class LoginActivity : Activity() {

    private val ipRegex = Regex("([0-9]{1,3}\\.){3}[0-9]{1,3}")
    private lateinit var binding: ActivityLoginBinding
    private lateinit var usernameEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var deviceNameEdit: EditText
    private lateinit var ipAddressEdit: EditText
    private var dataIntent: Intent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get UUID from shared preferences if it exists
        val sharedPreferences = getSharedPreferences("tgr_prefs", Context.MODE_PRIVATE)
        val uuid = sharedPreferences.getString("uuid", null)
        dataIntent.putExtra("uuid", uuid)

        if (!uuid.isNullOrEmpty()) {
            binding.uuidErrTextView.setText("UUID: $uuid")
            binding.uuidErrTextView.setTextColor(Color.rgb(0, 128, 0))
            dataIntent.putExtra("login", true)

            binding.loginButton.setOnClickListener {
                dataIntent.putExtra("username", usernameEdit.text.trim())
                dataIntent.putExtra("password", passwordEdit.text)
                dataIntent.putExtra("deviceName", deviceNameEdit.text)
                dataIntent.putExtra("ipAddress", ipAddressEdit.text.trim())

                if (checkLoginValidity() == RESULT_OK) {
                    setResult(RESULT_OK, dataIntent)
                    finish()
                } else {
                    setResult(RESULT_CANCELED)
                }
            }
        } else {
            binding.uuidErrTextView.setText("UUID not found, login not available")
            dataIntent.putExtra("login", false)

            binding.loginButton.setOnClickListener {
                dataIntent.putExtra("username", usernameEdit.text.trim())
                dataIntent.putExtra("password", passwordEdit.text)
                dataIntent.putExtra("deviceName", deviceNameEdit.text)
                dataIntent.putExtra("ipAddress", ipAddressEdit.text.trim())
                dataIntent.putExtra("login", false)

                if (checkRegisterValidity() == RESULT_OK) {
                    setResult(RESULT_OK, dataIntent)
                    finish()
                } else {
                    setResult(RESULT_CANCELED)
                }
            }
        }

        usernameEdit = findViewById(R.id.editTextUsername)
        passwordEdit = findViewById(R.id.editTextPassword)
        deviceNameEdit = findViewById(R.id.editTextTextPersonName)
        ipAddressEdit = findViewById(R.id.editTextIpAddress)
    }

    private fun checkLoginValidity() : Int {
        val validityUsername = usernameEdit.text.trim().matches(Regex("[a-zA-Z0-9]+"))
        val validityPassword = passwordEdit.text.isNotEmpty()
        val validityDeviceName = deviceNameEdit.text.isNotEmpty()
        val validityIpAddress = true //ipAddressEdit.text.trim().matches(ipRegex) - uncomment if ip will be used

        if (!validityUsername) usernameEdit.error = "Unsupported characters!"
        if (!validityPassword) passwordEdit.error = "Password cannot be empty!"
        if (!validityIpAddress) ipAddressEdit.error = "Bad IP address!"

        return if (validityUsername and validityPassword and validityDeviceName and validityIpAddress) RESULT_OK else RESULT_CANCELED
    }

    private fun checkRegisterValidity() : Int {
        val validityUsername = usernameEdit.text.trim().matches(Regex("[a-zA-Z0-9]+"))
        val validityPassword = passwordEdit.text.isNotEmpty()
        val validityIpAddress = true //ipAddressEdit.text.trim().matches(ipRegex) - uncomment if ip will be used

        if (!validityUsername) usernameEdit.error = "Unsupported characters!"
        if (!validityPassword) passwordEdit.error = "Password cannot be empty!"
        if (!validityIpAddress) ipAddressEdit.error = "Bad IP address!"

        return if (validityUsername and validityPassword and validityIpAddress) RESULT_OK else RESULT_CANCELED
    }
}