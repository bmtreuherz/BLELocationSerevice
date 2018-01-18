package bmtreuherz.blelocationservice

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import bmtreuherz.blelocationservice.utilities.BeaconProtocol
import bmtreuherz.blelocationservice.utilities.getBytesFromUUID
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Bradley on 1/17/18.
 */

class NewEmitterActivity : AppCompatActivity() {

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
    private var isAdvertising = false

    // UI Components
    lateinit private var startAdvertisingButton: Button
    lateinit private var majorET: EditText
    lateinit private var minorET: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emitter)

        // Get the UI Components
        startAdvertisingButton = findViewById(R.id.startAdvertisingButton)
        startAdvertisingButton.setOnClickListener {
            enableAdvertising(!isAdvertising)
        }
        majorET = findViewById(R.id.majorTV)
        minorET = findViewById(R.id.minorTV)

        // Set the beacon protocl
        beaconProtocol = BeaconProtocol.ALT_BEACON

        // Instantiated bluetooth related stuff
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        blueoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        // Setup the BLE Advertisement
//        setAdvertiseData()
        setAdvertiseSettings()
    }


    private fun setAdvertiseData(){
        var builder = AdvertiseData.Builder()
        var manufacturerData = ByteBuffer.allocate(23)
        var uuid = getBytesFromUUID(appUUID)

        // Set the beacon protocol identifier
        manufacturerData.put(0, 0x02)
        manufacturerData.put(1, 0x15)

        // Add the uuid
        for (i in 2..17){
            manufacturerData.put(i, uuid[i-2])
        }

        // Add the data
        setAdvertisementPayload(manufacturerData)

        // Set the txPower
        manufacturerData.put(22, defaultTXPower)

        // Build the advertiser
        builder.addManufacturerData(76, manufacturerData.array())
        advertiseData = builder.build()
    }

    private fun setAdvertiseSettings(){
        var builder = AdvertiseSettings.Builder()
        builder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        builder.setConnectable(false)
        builder.setTimeout(0)
        builder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
        advertiseSettings = builder.build()
    }

    private fun enableAdvertising(enable: Boolean){
        when(enable){
            true -> {
                setAdvertiseData()
                blueoothLeAdvertiser.startAdvertising(advertiseSettings, advertiseData, object: AdvertiseCallback(){})
                startAdvertisingButton.text = "Stop Advertising"
            }
            false -> {
                blueoothLeAdvertiser.stopAdvertising(object: AdvertiseCallback(){})
                startAdvertisingButton.text = "Start Advertising"
            }
        }
        isAdvertising = enable
    }

    private fun setAdvertisementPayload(manufacturerData: ByteBuffer){
        var major =  majorET.text.toString().toInt()
        var minor = minorET.text.toString().toInt()

        manufacturerData.putChar(18, major.toChar())
        manufacturerData.putChar(20, minor.toChar())

//        manufacturerData.put(18, 0x00) // first byte of major
//        manufacturerData.put(19, 0x09) // second byte of major
//        manufacturerData.put(20, 0x00) // first byte of minor
//        manufacturerData.put(21, 0x06) // second byte of minor
    }
}
