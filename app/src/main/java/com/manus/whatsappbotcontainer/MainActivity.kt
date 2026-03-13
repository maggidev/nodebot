package com.manus.whatsappbotcontainer

import android.content.Intent
import android.os.Bundle
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

    override fun onDestroy() {
        super.onDestroy()
        mSocket?.disconnect()
    }
}
