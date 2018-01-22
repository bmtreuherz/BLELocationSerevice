package bmtreuherz.blelocationservice

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.*
//import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import bmtreuherz.blelocationservice.utilities.BeaconProtocol
import bmtreuherz.blelocationservice.utilities.getBytesFromUUID
import bmtreuherz.blelocationservice.utilities.getUUIDFromBytes
import java.nio.ByteBuffer
import java.util.*

class EmitterActivity : Activity() {

    // Configuration members
    private val appUUID = UUID.fromString("7b334cce-f705-11e7-8c3f-9a214cf093ae")
    private val defaultTXPower = 0x5B.toByte()// This is the value -75DB for nexus 9. This is different for
    // every device and a better way of determining this for each device should be explored.
    lateinit private var beaconProtocol: BeaconProtocol

    // Bluetooth related members
    lateinit private var bluetoothAdapter: BluetoothAdapter
    lateinit private var blueoothLeAdvertiser: BluetoothLeAdvertiser
    lateinit private var advertiseData: AdvertiseData
    lateinit private var advertiseSettings: AdvertiseSettings

    // UI Components
    lateinit private var startAdvertisingButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emitter)

        // Get the UI Components
        startAdvertisingButton = findViewById(R.id.startAdvertisingButton) as Button
        startAdvertisingButton.setOnClickListener {
            startAdvertising()
        }

        // Set the beacon protocl
        beaconProtocol = BeaconProtocol.ALT_BEACON

        // Instantiated bluetooth related stuff
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        blueoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        // Setup the BLE Advertisement
        setAdvertiseData()
        setAdvertiseSettings()
    }

    private fun setAdvertiseData(){
        var builder = AdvertiseData.Builder()
        var manufacturerData = ByteBuffer.allocate(24)
        var uuid = getBytesFromUUID(appUUID)

        // Set the beacon protocol identifier
        manufacturerData.put(0, beaconProtocol.firstByte)
        manufacturerData.put(1, beaconProtocol.secondByte)

        // Add the uuid
        for (i in 2..17){
            manufacturerData.put(i, uuid[i-2])
        }

        // Add the data
        setAdvertisementPayload(manufacturerData)

        // Set the txPower
        manufacturerData.put(22, defaultTXPower)

        // Build the advertiser
        builder.addManufacturerData(beaconProtocol.manufacturerID, manufacturerData.array())
        advertiseData = builder.build()
    }

    private fun setAdvertiseSettings(){
        var builder = AdvertiseSettings.Builder()
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_POWER)
        builder.setConnectable(false)
        builder.setTimeout(0)
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
        advertiseSettings = builder.build()
    }

    private fun startAdvertising(){
        startAdvertisingButton.text = "Advertising"
        blueoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, object: AdvertiseCallback(){})
    }

    private fun setAdvertisementPayload(manufacturerData: ByteBuffer){
        manufacturerData.put(18, 0x00) // first byte of major
        manufacturerData.put(19, 0x09) // second byte of major
        manufacturerData.put(20, 0x00) // first byte of minor
        manufacturerData.put(21, 0x06) // second byte of minor
    }
}
