package com.example.dummyapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {

    // Android emulator -> host machine.
    // Later, replace this with your Azure VM/backend URL.
    private val baseUrl = "http://10.0.2.2:5006"

    private lateinit var statusText: TextView
    private lateinit var employeeList: TextView
    private lateinit var nameInput: EditText
    private lateinit var emailInput: EditText
    private lateinit var departmentInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        employeeList = findViewById(R.id.employeeList)
        nameInput = findViewById(R.id.nameInput)
        emailInput = findViewById(R.id.emailInput)
        departmentInput = findViewById(R.id.departmentInput)

        findViewById<Button>(R.id.refreshButton).setOnClickListener {
            loadEmployees()
        }

        findViewById<Button>(R.id.addButton).setOnClickListener {
            addEmployee()
        }

        loadEmployees()
    }

    private fun loadEmployees() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = httpGet("$baseUrl/api/employees")
                val employees = JSONArray(response)

                val output = StringBuilder()

                for (i in 0 until employees.length()) {
                    val employee = employees.getJSONObject(i)
                    output.append(
                        "${employee.getInt("id")}. " +
                        "${employee.getString("name")} | " +
                        "${employee.getString("department")}\n" +
                        "${employee.getString("email")}\n\n"
                    )
                }

                withContext(Dispatchers.Main) {
                    statusText.text = "Backend connected"
                    employeeList.text = output.toString()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Backend error: ${e.message}"
                }
            }
        }
    }

    private fun addEmployee() {
        val name = nameInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val department = departmentInput.text.toString().trim()

        if (name.isEmpty() || email.isEmpty() || department.isEmpty()) {
            statusText.text = "Please enter all fields"
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject()
                json.put("name", name)
                json.put("email", email)
                json.put("department", department)

                httpPost("$baseUrl/api/employees", json.toString())

                withContext(Dispatchers.Main) {
                    nameInput.text.clear()
                    emailInput.text.clear()
                    departmentInput.text.clear()
                    statusText.text = "Employee added"
                    loadEmployees()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    statusText.text = "Add failed: ${e.message}"
                }
            }
        }
    }

    private fun httpGet(urlString: String): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        return connection.inputStream.bufferedReader().use { it.readText() }
    }

    private fun httpPost(urlString: String, body: String): String {
        val connection = URL(urlString).openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/json")
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        connection.outputStream.use {
            it.write(body.toByteArray(Charsets.UTF_8))
        }

        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}
