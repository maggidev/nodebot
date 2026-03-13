package com.manus.whatsappbotcontainer

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class BotEngineService : Service() {

    private var wakeLock: PowerManager.WakeLock? = null
    private var nodeProcess: Process? = null

    companion object {
        const val CHANNEL_ID = "BotEngineChannel"
        var isRunning = false
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isRunning = true
        startForeground(1, createNotification())

        // Acquire WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BotEngine::WakeLock")
        wakeLock?.acquire()

        // Extract and Start Node.js
        Thread {
            try {
                val nodeDir = File(filesDir, "nodejs-project")
                if (!nodeDir.exists()) {
                    extractAssets(this, "nodejs-project", nodeDir)
                }
                
                val nodeBinary = File(nodeDir, "node")
                if (nodeBinary.exists()) {
                    nodeBinary.setExecutable(true)
                }

                val processBuilder = ProcessBuilder(
                    nodeBinary.absolutePath,
                    File(nodeDir, "index.js").absolutePath
                )
                processBuilder.directory(nodeDir)
                processBuilder.redirectErrorStream(true)
                
                nodeProcess = processBuilder.start()
                
                val inputStream = nodeProcess?.inputStream
                val reader = inputStream?.bufferedReader()
                
                reader?.forEachLine { line ->
                    // Logs are handled via Socket.io in MainActivity, 
                    // but we could also broadcast them here.
                    println("Node.js: $line")
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()

        return START_STICKY
    }

    private fun extractAssets(context: Context, assetPath: String, targetDir: File) {
        if (!targetDir.exists()) {
            targetDir.mkdirs()
        }
        val assetManager = context.assets
        val assets = assetManager.list(assetPath) ?: return
        for (asset in assets) {
            val fullAssetPath = "$assetPath/$asset"
            val targetFile = File(targetDir, asset)
            if (assetManager.list(fullAssetPath)?.isNotEmpty() == true) {
                extractAssets(context, fullAssetPath, targetFile)
            } else {
                copyFile(assetManager.open(fullAssetPath), targetFile)
            }
        }
    }

    private fun copyFile(inputStream: InputStream, targetFile: File) {
        FileOutputStream(targetFile).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Bot Engine Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("WhatsApp Bot Ativo")
            .setContentText("O motor Node.js está rodando em segundo plano.")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentIntent(pendingIntent)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        nodeProcess?.destroy()
        wakeLock?.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
