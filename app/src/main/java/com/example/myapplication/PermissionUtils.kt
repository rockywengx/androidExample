import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object PermissionUtils {
  fun hasBluetoothPermissions(context: Context): Boolean {
      return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
          context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED &&
          context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
      } else {
          context.checkSelfPermission(android.Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
          context.checkSelfPermission(android.Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED &&
          context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
      }
  }
}
