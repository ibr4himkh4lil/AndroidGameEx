package com.aether.memoryeditor.data.model

enum class DataType(val id: Int, val size: Int, val displayName: String) {
    BYTE(1, 1, "Byte"),
    WORD(2, 2, "Word"),
    DWORD(4, 4, "Dword"),
    QWORD(8, 8, "Qword"),
    FLOAT(16, 4, "Float"),
    DOUBLE(32, 8, "Double"),
    AUTO(64, 0, "Auto"),
    XOR(128, 4, "XOR");

    companion object {
        fun fromId(id: Int): DataType {
            return values().find { it.id == id } ?: DWORD
        }
    }
}
