package bmtreuherz.blelocationservice.GattStuff.Utilities

import java.nio.ByteBuffer

/**
 * Created by Bradley on 1/21/18.
 */

// TODO: A lot of repition here. Find a better way to do this.
object MessageFactory {
    var positionType: Byte = 0x0
    var rotationType: Byte = 0x1


    fun createPositionValue(position: PositionMessage): ByteArray{
        var buffer = ByteBuffer.allocate(17)
        buffer.put(positionType)
        buffer.putInt(position.id)
        buffer.putFloat(position.x)
        buffer.putFloat(position.y)
        buffer.putFloat(position.z)

        var value = ByteArray(17)
        buffer.get(value, 0, 17)
        return value
    }

    fun createPositionFromBytes(bytes: ByteArray): PositionMessage {
        var id = ByteBuffer.wrap(bytes, 1, 4).int
        var x = ByteBuffer.wrap(bytes, 5, 4).float
        var y = ByteBuffer.wrap(bytes, 9, 4).float
        var z = ByteBuffer.wrap(bytes, 13, 4).float
        return PositionMessage(id, x, y, z)
    }

    fun createRotationValue(rotation: RotationMessage): ByteArray{
        var buffer = ByteBuffer.allocate(17)
        buffer.put(rotationType)
        buffer.putInt(rotation.id)
        buffer.putFloat(rotation.x)
        buffer.putFloat(rotation.y)
        buffer.putFloat(rotation.z)

        var value = ByteArray(17)
        buffer.get(value, 0, 17)
        return value
    }

    fun createRotationFromBytes(bytes: ByteArray): RotationMessage {
        var buffer = ByteBuffer.wrap(bytes)
        var id = buffer.getInt(1)
        var x = buffer.getFloat(5)
        var y = buffer.getFloat(9)
        var z = buffer.getFloat(13)
        return RotationMessage(id, x, y, z)
    }

    class PositionMessage {
        var id: Int
        var x: Float
        var y: Float
        var z: Float

        constructor(id: Int, x: Float, y: Float, z: Float){
            this.id = id
            this.x = x
            this.y = y
            this.z = z
        }

        fun equals(other: PositionMessage): Boolean {
                return this.id == other.id && this.x == other.x && this.y == other.y && this.z == other.z
        }
    }

    class RotationMessage {
        var id: Int
        var x: Float
        var y: Float
        var z: Float

        constructor(id: Int, x: Float, y: Float, z: Float){
            this.id = id
            this.x = x
            this.y = y
            this.z = z
        }

        fun equals(other: RotationMessage): Boolean {
            return this.id == other.id && this.x == other.x && this.y == other.y && this.z == other.z
        }
    }
}