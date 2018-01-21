package bmtreuherz.blelocationservice.GattStuff.Server

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattService
import java.util.*

/**
 * Created by Bradley on 1/21/18.
 */
class GameObjectProfile {

    companion object {

        val GAME_OBJECT_SERVICE_UUID = UUID.fromString("fdbb3be0-0586-420b-a0d6-ec902536c7bc")
        val SERVER_TO_CLIENT_CHARACTERISTIC_UUID = UUID.fromString("92e1ab81-9dcc-442d-a215-50ba6547217d")
        val CLIENT_TO_SERVER_CHARACTERISTIC_UUID = UUID.fromString("561650aa-5b0b-473b-b80e-b78c5af96483")

        fun createGameObjectService(): BluetoothGattService{

            var service = BluetoothGattService(GAME_OBJECT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

            // Server to Client Characteristic
            var serverToClientCharacteristic = BluetoothGattCharacteristic(SERVER_TO_CLIENT_CHARACTERISTIC_UUID,
                    // READ-ONLY and supports Indication (Require that the client acknowledges the read
                    BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
                    BluetoothGattCharacteristic.PERMISSION_READ)

            // Server to Client Characteristic
            var clientToServerCharacteristic = BluetoothGattCharacteristic(CLIENT_TO_SERVER_CHARACTERISTIC_UUID,
                    // Supports Writes
                    BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE)

            service.addCharacteristic(serverToClientCharacteristic)
            service.addCharacteristic(clientToServerCharacteristic)

            return service
        }
    }
}

// TODO: Make sure the property/ permisisons are being used properly
// TODO: May want to add descriptors to the characteristic
// TODO: May want to allow reads on the client to server characteristic