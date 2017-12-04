package bmtreuherz.blelocationservice.utilities

import java.nio.ByteBuffer
import java.util.*

fun getBytesFromUUID(uuid: UUID): ByteArray{
    var buffer = ByteBuffer.wrap(ByteArray(16))
    buffer.putLong(uuid.mostSignificantBits)
    buffer.putLong(uuid.leastSignificantBits)

    return buffer.array()
}

fun getUUIDFromBytes(bytes: ByteArray): UUID{
    var byteBuffer = ByteBuffer.wrap(bytes)
    var mSB = byteBuffer.long
    var lSB = byteBuffer.long

    return UUID(mSB, lSB)
}

// Simple algorithm for calculating distance (in meters)
fun calculateDistance(txPower: Int, rssi: Double): Double{
    // Compute the distance
    return Math.pow(10.0, (txPower-rssi) / 20.0)
}

// AltBeacon algorithm for calculating distance (in meters)
// Accuracy will likely be associated with distance
// -1 indicates no distance could be found
// Distance < 1 can be considered immediate vicinity
// Distance [1,3] is close
// Distance >3 is far
fun calculateDistanceAltBeaconAlgorithm(txPower: Int, rssi: Double): Double{
    if (rssi == 0.0) return -1.0

    var ratio = rssi * 1.0 / txPower

    return when (ratio < 1.0) {
        true -> Math.pow(ratio, 10.0)
        false -> (0.89976) * Math.pow(ratio, 7.7095) + 0.111
    }
}