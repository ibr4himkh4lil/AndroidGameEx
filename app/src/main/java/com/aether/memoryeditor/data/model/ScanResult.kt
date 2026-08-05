package com.aether.memoryeditor.data.model

data class ScanResult(
    val address: Long,
    val value: ByteArray,
    val dataType: DataType,
    val description: String = "",
    val isFrozen: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ScanResult

        if (address != other.address) return false
        if (!value.contentEquals(other.value)) return false
        if (dataType != other.dataType) return false
        if (description != other.description) return false
        if (isFrozen != other.isFrozen) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + value.contentHashCode()
        result = 31 * result + dataType.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + isFrozen.hashCode()
        return result
    }

    fun copy(
        address: Long = this.address,
        value: ByteArray = this.value,
        dataType: DataType = this.dataType,
        description: String = this.description,
        isFrozen: Boolean = this.isFrozen
    ): ScanResult {
        return ScanResult(address, value, dataType, description, isFrozen)
    }
}
