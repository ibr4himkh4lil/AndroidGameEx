package com.aether.memoryeditor.data.model

enum class MemoryRegion(val displayName: String, val filter: String) {
    ANONYMOUS("Anonymous", "[anon]"),
    C_HEAP("C++ Heap", "[heap]"),
    C_ALLOC("C++ Alloc", "C_ALLOC"),
    JAVA_HEAP("Java Heap", "/dev/ashmem"),
    STACK("Stack", "[stack]"),
    CODE_SYSTEM("Code System", "/system"),
    BAD("Bad", "BAD"),
    CODE_APP("Code App", "/data/app"),
    ASHMEM("Ashmem", "ashmem")
}
