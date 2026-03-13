package com.manus.whatsappbotcontainer

import android.content.Intent
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.net.Uri
import android.Manifest
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject
import java.net.URISyntaxException

class MainActivity : AppCompatActivity() {

    private lateinit var statusCard: MaterialCardView
    private lateinit var statusText: TextView
    private lateinit var authSwitch: SwitchMaterial
    private lateinit var qrImageView: ImageView
    private lateinit var pairingInput: EditText
    private lateinit var pairingButton: Button
    private lateinit var pairingCodeText: TextView
    private lateinit var consoleRecyclerView: RecyclerView
    private lateinit var startStopButton: Button

    private var mSocket: Socket? = null
    private val logList = mutableListOf<String>()
    private lateinit var logAdapter: LogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        checkAndRequestPermissions()
        setupSocket()
        
        startStopButton.setOnClickListener {
            val intent = Intent(this, BotEngineService::class.java)
            if (BotEngineService.isRunning) {
                stopService(intent)
                startStopButton.text = "Iniciar Bot"
            } else {
                startForegroundService(intent)
                startStopButton.text = "Parar Bot"
            }
        }

        authSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Pairing Code mode
                qrImageView.visibility = View.GONE
                pairingInput.visibility = View.VISIBLE
                pairingButton.visibility = View.VISIBLE
                pairingCodeText.visibility = View.VISIBLE
            } else {
                // QR Code mode
                qrImageView.visibility = View.VISIBLE
                pairingInput.visibility = View.GONE
                pairingButton.visibility = View.GONE
                pairingCodeText.visibility = View.GONE
            }
        }

        pairingButton.setOnClickListener {
            val number = pairingInput.text.toString()
            if (number.isNotEmpty()) {
                mSocket?.emit("start_pairing_process", number)
            }
        }
    }

    private fun initViews() {
        statusCard = findViewById(R.id.statusCard)
        statusText = findViewById(R.id.statusText)
        authSwitch = findViewById(R.id.authSwitch)
        qrImageView = findViewById(R.id.qrImageView)
        pairingInput = findViewById(R.id.pairingInput)
        pairingButton = findViewById(R.id.pairingButton)
        pairingCodeText = findViewById(R.id.pairingCodeText)
        consoleRecyclerView = findViewById(R.id.consoleRecyclerView)
        startStopButton = findViewById(R.id.startStopButton)

        logAdapter = LogAdapter(logList)
        consoleRecyclerView.layoutManager = LinearLayoutManager(this)
        consoleRecyclerView.adapter = logAdapter
    }

    private fun setupSocket() {
        try {
            // Se o servidor Node.js estiver em um dispositivo diferente, substitua "localhost" pelo IP da sua rede Wi-Fi (ex: "http://192.168.1.15:3000")
            mSocket = IO.socket("http://localhost:3000")
        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        mSocket?.on("qr_code_emit") { args ->
            runOnUiThread {
                val qr = args[0] as String
                // Here you would normally use a library like ZXing to generate a bitmap from the QR string
                // For now, we just log it
                addLog("QR Code recebido: $qr")
            }
        }

        mSocket?.on("pairing_code_emit") { args ->
            runOnUiThread {
                val code = args[0] as String
                pairingCodeText.text = "Código: $code"
                addLog("Código de pareamento: $code")
            }
        }

        mSocket?.on("connection_status") { args ->
            runOnUiThread {
                val status = args[0] as String
                statusText.text = "Status: $status"
                addLog("Status da conexão: $status")
            }
        }

        mSocket?.on("error") { args ->
            runOnUiThread {
                val errorMessage = args[0] as String
                addLog("Erro do servidor: $errorMessage")
                statusText.text = "Erro: $errorMessage"
            }
        }

        mSocket?.on("console_log") { args ->
            runOnUiThread {
                val log = args[0] as String
                addLog(log)
            }
        }

        mSocket?.connect()
    }

    private fun addLog(message: String) {
        logList.add(message)
        logAdapter.notifyItemInserted(logList.size - 1)
        consoleRecyclerView.scrollToPosition(logList.size - 1)
    }

    private val PERMISSION_REQUEST_CODE = 1001

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val readPermission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
            val writePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            val permissionsToRequest = mutableListOf<String>()

            if (readPermission != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            if (writePermission != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
            }

            // For Android 11 (API 30) and above, MANAGE_EXTERNAL_STORAGE is needed for broad access
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (!Environment.isExternalStorageManager()) {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                addLog("Permissões de armazenamento concedidas.")
            } else {
                addLog("Permissões de armazenamento negadas. O bot pode não funcionar corretamente.")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
    }
}
