package ywcheong.sofia.adapter.common.jpa

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import java.nio.ByteBuffer
import java.util.UUID

/**
 * UUID를 BINARY(16)로 변환하는 JPA AttributeConverter.
 *
 * MySQL의 BINARY(16) 컬럼에 UUID를 효율적으로 저장하기 위한 변환기를 제공합니다.
 * 일반적인 문자열 기반 UUID(36자) 대비 저장 공간을 절약할 수 있습니다.
 *
 * autoApply = true로 설정하여 UUID 타입 필드에 자동으로 적용됩니다.
 */
@Converter(autoApply = true)
class BinaryUUIDConverter : AttributeConverter<UUID, ByteArray> {
    override fun convertToDatabaseColumn(attribute: UUID?): ByteArray? =
        attribute?.let { uuid ->
            ByteBuffer
                .allocate(SIZEOF_UUID)
                .putLong(uuid.mostSignificantBits)
                .putLong(uuid.leastSignificantBits)
                .array()
        }

    override fun convertToEntityAttribute(dbData: ByteArray?): UUID? =
        dbData?.let { bytes ->
            require(bytes.size == SIZEOF_UUID) { "UUID requires exactly 16 bytes, got ${bytes.size}" }
            val buffer = ByteBuffer.wrap(bytes)
            UUID(buffer.long, buffer.long)
        }

    companion object {
        private const val SIZEOF_UUID = 16
    }
}
