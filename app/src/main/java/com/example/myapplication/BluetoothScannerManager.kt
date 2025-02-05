package com.example.myapplication

import PermissionUtils
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelUuid
import android.util.Log
import androidx.annotation.RequiresApi
import java.util.UUID


private const val TAG = "BluetoothScannerManager"

class BluetoothScannerManager private constructor(private val context: Context) {
  companion object {
      private var instance: BluetoothScannerManager? = null
      
      fun getInstance(context: Context): BluetoothScannerManager {
          return instance ?: synchronized(this) {
              instance ?: BluetoothScannerManager(context).also { instance = it }
          }
      }

      /**
       * 設置掃描器服務 UUID
       */
      private var SCANNER_SERVICE_UUID: UUID? = null
      private var SCANNER_CHARACTERISTIC_UUID: UUID? = null
      private const val SCAN_PERIOD = 10000L
  }

  private val bluetoothManager: BluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
  private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
  private var scannerGatt: BluetoothGatt? = null
  
  private var connectionListener: ConnectionListener? = null
  private var scanningListener: ScanningListener? = null
  
  private var isScanning = false
  private var isScannerConnected = false

  // 介面定義
  interface ConnectionListener {
      fun onConnectionStateChanged(isConnected: Boolean)
      fun onConnectionTimeout()
      fun onDataReceived(data: ByteArray)
      fun onConnectionError(errorCode: Int)
      fun onDeviceDisconnected()
  }

  interface ScanningListener {
      fun onScanStarted()
      fun onDeviceFound(device: BluetoothDevice)
      fun onScanFailed(errorCode: Int)
      fun onScanTimeout()
  }

  // 掃描相關
  private val scanCallback = object : ScanCallback() {
      @SuppressLint("MissingPermission")
      override fun onScanResult(callbackType: Int, result: ScanResult) {
          val device = result.device
          Log.d("BluetoothScannerManager", "onScanResult: ${device.name} , result: ${result}")

          if (isScannerDevice(result)) {
              stopScan()
              scanningListener?.onDeviceFound(device)
          }
      }

      override fun onScanFailed(errorCode: Int) {
          Log.d("BluetoothScannerManager", "onScanFailed: $errorCode")
          scanningListener?.onScanFailed(errorCode)
      }
  }

  // GATT 回調
  private val gattCallback = object : BluetoothGattCallback() {
      @SuppressLint("MissingPermission")
      override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
//          if (status != BluetoothGatt.GATT_SUCCESS) {
//              connectionListener?.onConnectionError(status)
//              gatt.close()
//              return
//          }
          Log.d("BluetoothScannerManager", "onConnectionStateChange: $newState, status: $status")
          when (newState) {
              BluetoothProfile.STATE_CONNECTED -> handleGattConnected(gatt)
              BluetoothProfile.STATE_DISCONNECTED -> handleGattDisconnected()
          }
      }

      @RequiresApi(Build.VERSION_CODES.TIRAMISU)
      override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
          Log.d("BluetoothScannerManager", "onServicesDiscovered: $status")
          if (status == BluetoothGatt.GATT_SUCCESS) {
              setupNotifications(gatt)
          }
      }
      /**
       * 接收數據
       */
      override fun onCharacteristicChanged(
          gatt: BluetoothGatt,
          characteristic: BluetoothGattCharacteristic,
          value: ByteArray
      ) {
          Log.d("BluetoothScannerManager", "收到數據: ${value.joinToString()}")
          if(value.isNotEmpty()) {
              connectionListener?.onDataReceived(value)
          }
      }

      /**
       * 接收數據（舊 API）
       */
      @Suppress("DEPRECATION") // 忽略 Deprecated 警告
      override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
          val data = characteristic.value
          Log.d("BluetoothScannerManager", "收到數據（舊 API）: ${data?.joinToString()}")
          characteristic.value?.let {
              connectionListener?.onDataReceived(it)
          }
      }
  }

  // 尋找裝置控制
  @SuppressLint("MissingPermission")
  fun startScan() {
      Log.d("BluetoothScannerManager", "startScan")
      if (!PermissionUtils.hasBluetoothPermissions(context)) {
          return
      }

      if (isScanning) return

      val scanFilter = ScanFilter.Builder()
          .setServiceUuid(ParcelUuid(SCANNER_SERVICE_UUID))
          .build()

      val scanSettings = ScanSettings.Builder()
          .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
          .build()

      isScanning = true
      scanningListener?.onScanStarted()

      Handler(Looper.getMainLooper()).postDelayed({
          stopScan()
          scanningListener?.onScanTimeout()
      }, SCAN_PERIOD)

      bluetoothAdapter.bluetoothLeScanner?.startScan(
          listOf(scanFilter),
          scanSettings,
          scanCallback
      )
  }
    /**
     * 透過輸入裝置名稱開始掃描
     */
  @SuppressLint("MissingPermission")
  fun startScanWithInput(input: String) {
      Log.d("BluetoothScannerManager", "startScanWithInput: $input")
      if (!PermissionUtils.hasBluetoothPermissions(context)) {
          return
      }

      if (isScanning) return

      val scanFilter = ScanFilter.Builder()
          .setDeviceName(input)
          .build()

      val scanSettings = ScanSettings.Builder()
          .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
          .build()

      isScanning = true
      scanningListener?.onScanStarted()

      Handler(Looper.getMainLooper()).postDelayed({
          stopScan()
          scanningListener?.onScanTimeout()
      }, SCAN_PERIOD)

      bluetoothAdapter.bluetoothLeScanner?.startScan(
          listOf(scanFilter),
          scanSettings,
          scanCallback
      )
  }

  @SuppressLint("MissingPermission")
  fun stopScan() {
      Log.d("BluetoothScannerManager", "stopScan")
      if (isScanning) {
          isScanning = false
          bluetoothAdapter.bluetoothLeScanner?.stopScan(scanCallback)
      }
  }

  // 連接控制
  @SuppressLint("MissingPermission")
  fun connect(device: BluetoothDevice): Boolean {
      Log.d("BluetoothScannerManager", "connect: ${device.name}")
      scannerGatt = device.connectGatt(context, true, gattCallback, BluetoothDevice.TRANSPORT_LE)
      return scannerGatt != null
  }

  @SuppressLint("MissingPermission")
  fun disconnect() {
      Log.d("BluetoothScannerManager", "disconnect")
      scannerGatt?.let { gatt ->
          gatt.disconnect()
          gatt.close()
          scannerGatt = null
      }
      isScannerConnected = false
  }

  // 監聽器設置
  fun setConnectionListener(listener: ConnectionListener) {
      this.connectionListener = listener
  }

  fun setScanningListener(listener: ScanningListener) {
      this.scanningListener = listener
  }

  // 私有輔助方法
  @SuppressLint("MissingPermission")
  private fun isScannerDevice(result: ScanResult): Boolean {
      Log.d("BluetoothScannerManager", "isScannerDevice: ${result.device.name}, ${SCANNER_SERVICE_UUID?.toString()}")
      return result.scanRecord?.serviceUuids?.contains(ParcelUuid(SCANNER_SERVICE_UUID)) == true
  }

  @SuppressLint("MissingPermission")
  private fun handleGattConnected(gatt: BluetoothGatt) {
      Log.d("BluetoothScannerManager", "handleGattConnected")
      isScannerConnected = true
      connectionListener?.onConnectionStateChanged(true)
      gatt.discoverServices()
  }

  private fun handleGattDisconnected() {
      Log.d("BluetoothScannerManager", "handleGattDisconnected")
      isScannerConnected = false
      connectionListener?.onConnectionStateChanged(false)
      connectionListener?.onDeviceDisconnected()
  }

  @RequiresApi(Build.VERSION_CODES.TIRAMISU)
  @SuppressLint("MissingPermission")
  private fun setupNotifications(gatt: BluetoothGatt) {
      Log.d("BluetoothScannerManager", "setupNotifications")

      for (service in gatt.services) {
          // 從已配對裝置進行時，uuid會為空，需重新設置
          if(SCANNER_SERVICE_UUID == null) {
              SCANNER_SERVICE_UUID = service.uuid
              Log.d(TAG, "找到服務 UUID: $SCANNER_SERVICE_UUID")
          }
          for (characteristic in service.characteristics) {
              Log.d(TAG, "發現特徵值 UUID: " + characteristic.uuid + "Service UUID: " + service.uuid)

              // 訂閱通知 (適用於 Notify 特徵值)
              if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
                  gatt.setCharacteristicNotification(characteristic, true)


                  // 設置 Client Characteristic Configuration Descriptor (CCCD)
                  val descriptor = characteristic.getDescriptor(
                      UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
                  )
                  descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
                  gatt.writeDescriptor(descriptor)
                  Log.d("BLE_SERVICE", "成功啟用通知: ${characteristic.uuid}")
              } else {
                  Log.d("BLE_SERVICE", "不支持 Notify 特徵值")
              }
          }
      }


//      val service = gatt.getService(SCANNER_SERVICE_UUID)
//      if (service == null) {
//          Log.e("BluetoothScannerManager", "找不到服務 UUID: $SCANNER_SERVICE_UUID")
//          connectionListener?.onConnectionError(-1)
//          gatt.close()
//          return
//      }
//
//      for (characteristic in service.characteristics) {
//          Log.d("BluetoothScannerManager", "發現特徵值 UUID: " + characteristic.uuid)
//
//          // 訂閱通知 (適用於 Notify 特徵值)
//          if ((characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0) {
//              gatt.setCharacteristicNotification(characteristic, true)
//              val descriptor = characteristic.getDescriptor(
//                  UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
//              )
//              if (descriptor != null) {
//                  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14+
//                      gatt.writeDescriptor(descriptor, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)
//                  } else {
//                      descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//                      gatt.writeDescriptor(descriptor)
//                  }
//                  Log.d("BluetoothScannerManager", "成功啟用通知: ${characteristic.uuid}")
//              } else {
//                  Log.e("BluetoothScannerManager", "找不到 Descriptor UUID: 00002902-0000-1000-8000-00805f9b34fb")
//              }
//          }
//      }
  }

    /**
     * 取得配對裝置
     */
  @SuppressLint("MissingPermission")
  fun getPairedDevices(): List<BluetoothDevice> {
      Log.d("BluetoothScannerManager", "getPairedDevices")
      val pairedDevices = bluetoothAdapter.bondedDevices.filter { device ->
          device.name.contains("Scanner", ignoreCase = true)
      }
      Log.d("BluetoothScannerManager", "getPairedDevices: $pairedDevices")
      return pairedDevices
  }

  fun isDeviceConnected(): Boolean {
      return isScannerConnected
  }
}
