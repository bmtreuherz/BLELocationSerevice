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

        val SERVER_TO_CLIENT_POSITION_CHARACTERISTIC_UUID = UUID.fromString("92e1ab81-9dcc-442d-a215-50ba6547217d")
        val CLIENT_TO_SERVER_POSITION_CHARACTERISTIC_UUID = UUID.fromString("561650aa-5b0b-473b-b80e-b78c5af96483")

//        val SERVER_TO_CLIENT_ROTATION_CHARACTERISTIC_UUID = UUID.fromString("a6b13226-2108-4d3f-bec9-a312d3abd9a8")
//        val CLIENT_TO_SERVER_ROTATION_CHARACTERISTIC_UUID = UUID.fromString("04f36f7a-0945-4ffc-8ab3-10e8c251c6fa")

        // TODO: Add animation state also

        fun createGameObjectService(): BluetoothGattService{

            var service = BluetoothGattService(GAME_OBJECT_SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

            // Server to Client Characteristic
            var serverToClientPositionCharacteristic = BluetoothGattCharacteristic(SERVER_TO_CLIENT_POSITION_CHARACTERISTIC_UUID,
                    // READ-ONLY and supports Indication (Require that the client acknowledges the read
                    BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
                    BluetoothGattCharacteristic.PERMISSION_READ)
//            var serverToClientRotationCharacteristic = BluetoothGattCharacteristic(SERVER_TO_CLIENT_ROTATION_CHARACTERISTIC_UUID,
//                    // READ-ONLY and supports Indication (Require that the client acknowledges the read
//                    BluetoothGattCharacteristic.PROPERTY_READ or BluetoothGattCharacteristic.PROPERTY_INDICATE,
//                    BluetoothGattCharacteristic.PERMISSION_READ)

            // Server to Client Characteristic
            var clientToServerPositionCharacteristic = BluetoothGattCharacteristic(CLIENT_TO_SERVER_POSITION_CHARACTERISTIC_UUID,
                    // Supports Writes
                    BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE)
//            var clientToServerRotationCharacteristic = BluetoothGattCharacteristic(CLIENT_TO_SERVER_ROTATION_CHARACTERISTIC_UUID,
//                    // Supports Writes
//                    BluetoothGattCharacteristic.PROPERTY_WRITE, BluetoothGattCharacteristic.PERMISSION_WRITE)

            service.addCharacteristic(serverToClientPositionCharacteristic)
//            service.addCharacteristic(serverToClientRotationCharacteristic)
            service.addCharacteristic(clientToServerPositionCharacteristic)
//            service.addCharacteristic(clientToServerRotationCharacteristic)

            return service
        }
    }
}

// TODO: Make sure the property/ permisisons are being used properly
// TODO: May want to add descriptors to the characteristic
// TODO: May want to allow reads on the client to server characteristic