package com.aether.memoryeditor.native

import com.techted89.gameex.NativeScanner
import timber.log.Timber

class NativeBridge {

    init {
        try {
            System.loadLibrary("native-scanner")
            Timber.d("Native library loaded successfully")
        } catch (e: UnsatisfiedLinkError) {
            Timber.e(e, "Failed to load native library")
        }
    }

    fun attachProcess(pid: Int) {
        Timber.d("Attached to process: $pid")
    }

    fun searchMemory(pid: Int, query: String, type: Int): Int {
        return try {
            NativeScanner.searchMemory(pid, query, type)
        } catch (e: Exception) {
            Timber.e(e, "Error searching memory")
            0
        }
    }

    fun filterMemory(pid: Int, query: String, type: Int): Int {
        return try {
            NativeScanner.filterMemory(pid, query, type)
        } catch (e: Exception) {
            Timber.e(e, "Error filtering memory")
            0
        }
    }

    fun startFuzzyScan(pid: Int, type: Int) {
        try {
            NativeScanner.startFuzzyScan(pid, type)
        } catch (e: Exception) {
            Timber.e(e, "Error starting fuzzy scan")
        }
    }

    fun filterFuzzy(pid: Int, mode: Int, type: Int): Int {
        return try {
            NativeScanner.filterFuzzy(pid, mode, type)
        } catch (e: Exception) {
            Timber.e(e, "Error filtering fuzzy")
            0
        }
    }

    fun getResults(limit: Int): LongArray {
        return try {
            NativeScanner.getResults(limit)
        } catch (e: Exception) {
            Timber.e(e, "Error getting results")
            longArrayOf()
        }
    }

    fun readMemory(pid: Int, address: Long, size: Int): ByteArray {
        return try {
            NativeScanner.readMemory(pid, address, size)
        } catch (e: Exception) {
            Timber.e(e, "Error reading memory at 0x${address.toString(16)}")
            ByteArray(size)
        }
    }

    fun getLoadedModules(pid: Int): Array<String> {
        return try {
            NativeScanner.getLoadedModules(pid)
        } catch (e: Exception) {
            Timber.e(e, "Error getting loaded modules")
            emptyArray()
        }
    }

    fun dumpMemory(pid: Int, from: Long, to: Long, path: String): Boolean {
        return try {
            NativeScanner.dumpMemory(pid, from, to, path)
        } catch (e: Exception) {
            Timber.e(e, "Error dumping memory")
            false
        }
    }

    fun setSpeed(speed: Double): Boolean {
        return try {
            NativeScanner.setSpeed(speed)
        } catch (e: Exception) {
            Timber.e(e, "Error setting speed")
            false
        }
    }
}
