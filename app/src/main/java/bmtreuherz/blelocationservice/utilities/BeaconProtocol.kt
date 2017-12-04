package bmtreuherz.blelocationservice.utilities

/**
 * Created by bradl on 12/4/2017.
 */
enum class BeaconProtocol(val firstByte: Byte, val secondByte: Byte, val manufacturerID: Int){
    ALT_BEACON(0xBE as Byte, 0xAC as Byte, 224)
}