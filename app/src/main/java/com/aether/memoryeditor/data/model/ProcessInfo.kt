package com.aether.memoryeditor.data.model

data class ProcessInfo(
    val pid: Int,
    val name: String,
    val packageName: String,
    val uid: Int,
    val icon: Any? = null
)
