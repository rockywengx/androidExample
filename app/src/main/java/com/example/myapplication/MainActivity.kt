package com.example.myapplication
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.crepowermay.ezui.*
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.viewmodels.MainState
import com.example.myapplication.viewmodels.MainViewModel
import java.util.concurrent.CompletableFuture

private const val TAG = "MainActivity"
class MainActivity : AppCompatActivity() {
    private lateinit var bluetoothScannerManager: BluetoothScannerManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothScannerManager = BluetoothScannerManager.getInstance(this)
        setupScannerListeners()
        setupUI()
        setupAutoPairing()
        checkDeviceConnection()
    }
    /**
     * 設置掃描器監聽器-查找設備、連接設備、接收數據、連接錯誤、設備斷開
     */
    private fun setupScannerListeners() {
        bluetoothScannerManager.setConnectionListener(object : BluetoothScannerManager.ConnectionListener {
            override fun onConnectionStateChanged(isConnected: Boolean) {
                updateConnectionStatus(isConnected)
            }

            override fun onConnectionTimeout() {
                showToast("連線超時")
            }

            override fun onDataReceived(data: ByteArray) {
                handleScannerData(data)
            }

            override fun onConnectionError(errorCode: Int) {
                Log.d("MainActivity", "onConnectionError: $errorCode")
                showToast("連線錯誤")
            }

            override fun onDeviceDisconnected() {
                handleDeviceDisconnected()
            }
        })

        bluetoothScannerManager.setScanningListener(object : BluetoothScannerManager.ScanningListener {
            override fun onScanStarted() {
                updateStatus("正在尋找...")
            }

            @SuppressLint("MissingPermission")
            override fun onDeviceFound(device: BluetoothDevice) {
                updateStatus("找到設備: ${device.name}")
                bluetoothScannerManager.connect(device)
                updateInputText(device.name)
            }

            override fun onScanFailed(errorCode: Int) {
                runOnUiThread {
                    updateStatus("掃描失敗: $errorCode")
                }
            }

            override fun onScanTimeout() {
                updateStatus("找尋設備超時")
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun setupUI() {

        findViewById<Button>(R.id.connectButton).setOnClickListener {
            if (bluetoothScannerManager.isDeviceConnected()) {
                showToast("裝置已連線")
            } else {
                val pairedDevices = bluetoothScannerManager.getPairedDevices()
                if (pairedDevices.isNotEmpty()) {
                    val device = pairedDevices.first()
                    val isConnected = bluetoothScannerManager.connect(device)
                    if (isConnected) {
                        updateStatus("正在連線到: ${device.name}")
                    } else {
                        showToast("連線失敗")
                    }
                } else {
                    showToast("沒有配對的裝置")
                }
            }
        }

        findViewById<Button>(R.id.disconnectButton).setOnClickListener {
            bluetoothScannerManager.disconnect()
            updateConnectionStatus(false)
        }

        findViewById<Button>(R.id.startSearchButton).setOnClickListener {
            bluetoothScannerManager.startScan()
            updateStatus("開始找尋中...")
        }

        findViewById<EditText>(R.id.inputText).setOnFocusChangeListener { _, hasFocus ->
            onInputFocusChange(hasFocus)
        }
    }

    private fun onInputFocusChange(hasFocus: Boolean) {
        if (hasFocus) {
            bluetoothScannerManager.startScan()
        } else {
            bluetoothScannerManager.stopScan()
        }
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        runOnUiThread {
            findViewById<TextView>(R.id.statusText).text =
                if (isConnected) "已連線" else "已斷開"
            if (!isConnected) {
                findViewById<TextView>(R.id.pairedDeviceName).text = "配對裝置名稱: 無"
            }
        }
    }

    private fun updateStatus(message: String) {
        runOnUiThread {
            findViewById<TextView>(R.id.statusText).text = message
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun handleScannerData(data: ByteArray) {
        val inputText = String(data)
        runOnUiThread {
            findViewById<EditText>(R.id.inputText).setText(inputText)
        }
    }

    @SuppressLint("MissingPermission")
    private fun setupAutoPairing() {
        val pairedDevices = bluetoothScannerManager.getPairedDevices()
        if (pairedDevices.isNotEmpty()) {
            val deviceName = pairedDevices.first().name
            updateStatus("自動配對中...")
            updatePairedDeviceName(deviceName)
            val isConnected = bluetoothScannerManager.connect(pairedDevices.first())
            if (isConnected) {
                updateStatus("配對成功")
            } else {
                updateStatus("配對失敗")
            }
        }
    }

    private fun updatePairedDeviceName(deviceName: String) {
        runOnUiThread {
            findViewById<TextView>(R.id.pairedDeviceName).text = "配對裝置名稱: $deviceName"
        }
    }

    private fun checkDeviceConnection() {
        if (bluetoothScannerManager.isDeviceConnected()) {
            updateStatus("裝置已連線")
        } else {
            updateStatus("裝置未連線")
        }
    }

    private fun handleDeviceDisconnected() {
        runOnUiThread {
            updateStatus("裝置已離線")
            updateConnectionStatus(false)
        }
    }

    private fun updateInputText(deviceName: String) {
        runOnUiThread {
            findViewById<EditText>(R.id.inputText).setText(deviceName)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothScannerManager.disconnect()
    }
}
