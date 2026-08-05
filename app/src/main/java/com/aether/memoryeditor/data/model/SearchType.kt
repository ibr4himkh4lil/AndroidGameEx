package com.aether.memoryeditor.data.model

enum class SearchType {
    EXACT,
    RANGE,
    FUZZY_CHANGED,
    FUZZY_UNCHANGED,
    FUZZY_INCREASED,
    FUZZY_DECREASED,
    FUZZY_GREATER,
    FUZZY_LESS,
    ENCRYPTED_XOR,
    UNKNOWN_INITIAL
}
