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
import android.widget.TextView
import bmtreuherz.blelocationservice.utilities.BeaconProtocol
import bmtreuherz.blelocationservice.utilities.getBytesFromUUID
import java.nio.ByteBuffer
import java.util.*

/**
 * Created by Bradley on 1/17/18.
 */

class NewEmitterActivity : AppCompatActivity() {

    // Configuration members
    private val appUUID = UUID.fromString("3F643DCB-DD1E-4300-8FD6-91543CD0E648")
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
    lateinit private var payloadET: EditText
    lateinit private var majorTV: TextView
    lateinit private var minorTV: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emitter)

        // Get the UI Components
        startAdvertisingButton = findViewById(R.id.startAdvertisingButton)
        startAdvertisingButton.setOnClickListener {
            enableAdvertising(!isAdvertising)
        }
        payloadET = findViewById(R.id.payloadET)
        majorTV = findViewById(R.id.majorTV)
        minorTV = findViewById(R.id.minorTV)

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

        var payload = payloadET.text.toString().toLong()
        var payloadBytes = longToBytes(payload)

        // TODO: Remove this after debugging. THESE COULD ALSO BE STORED IN AN INT INSTEAD OF LONG
        majorTV.text = bytesToChar(Arrays.copyOfRange(payloadBytes, 4, 6)).toInt().toString()
        minorTV.text = bytesToChar(Arrays.copyOfRange(payloadBytes, 6, 8)).toInt().toString()


        manufacturerData.put(18, payloadBytes[4])
        manufacturerData.put(19, payloadBytes[5])
        manufacturerData.put(20, payloadBytes[6])
        manufacturerData.put(21, payloadBytes[7])

//        manufacturerData.putChar(18, major.toChar())
//        manufacturerData.putChar(20, minor.toChar())

//        manufacturerData.put(18, 0x00) // first byte of major
//        manufacturerData.put(19, 0x09) // second byte of major
//        manufacturerData.put(20, 0x00) // first byte of minor
//        manufacturerData.put(21, 0x06) // second byte of minor
    }

    fun longToBytes(x: Long): ByteArray {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.putLong(x)
        return buffer.array()
    }

    fun bytesToLong(bytes: ByteArray): Long {
        val buffer = ByteBuffer.allocate(java.lang.Long.BYTES)
        buffer.put(bytes)
        buffer.flip()//need flip
        return buffer.long
    }

    fun bytesToChar(bytes: ByteArray): Char {
        val buffer = ByteBuffer.allocate(2)
        buffer.put(bytes)
        buffer.flip()
        return buffer.char
    }
}

//TODO: Something is going on with the emiter/ scanner where the scanner either
// continues to scan or doesnt scan or the emitter continues or doesnt idk which
// TODO: Distance is giving -1 on iOS.
