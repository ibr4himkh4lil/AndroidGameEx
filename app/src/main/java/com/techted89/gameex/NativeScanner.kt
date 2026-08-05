package com.techted89.gameex

object NativeScanner {
    init {
        System.loadLibrary("native-scanner")
    }

    // Fuzzy Scan Modes
    const val FUZZY_CHANGED = 0
    const val FUZZY_UNCHANGED = 1
    const val FUZZY_INCREASED = 2
    const val FUZZY_DECREASED = 3

    // Supported Data Types
    const val TYPE_BYTE = 1
    const val TYPE_WORD = 2
    const val TYPE_DWORD = 4
    const val TYPE_QWORD = 8
    const val TYPE_FLOAT = 16
    const val TYPE_DOUBLE = 32
    const val TYPE_AUTO = 64
    const val TYPE_XOR = 128

    /**
     * Read a sequence of bytes from the memory of a target process.
     */
    external fun readMemory(pid: Int, address: Long, size: Int): ByteArray

    /**
     * Searches memory with a specific data type.
     */
    external fun searchMemory(pid: Int, query: String, type: Int): Int

    /**
     * Filters the current search results using a query string and specified type.
     */
    external fun filterMemory(pid: Int, query: String, type: Int): Int

    /**
     * Starts a fuzzy scan by capturing current memory snapshot.
     */
    external fun startFuzzyScan(pid: Int, type: Int)

    /**
     * Filters the fuzzy scan results.
     */
    external fun filterFuzzy(pid: Int, mode: Int, type: Int): Int

    /**
     * Retrieves a list of loaded modules (libraries) in the target process.
     */
    external fun getLoadedModules(pid: Int): Array<String>

    /**
     * Enables stealth mode features.
     */
    external fun enableStealthMode()

    /**
     * Disassembles a Lua script.
     */
    external fun disassembleScript(inPath: String, outPath: String)

    /**
     * Assembles an assembly listing back into a Lua binary chunk.
     */
    external fun assembleScript(inPath: String, outPath: String)

    /**
     * Dumps memory regions to files on disk.
     */
    external fun dumpMemory(pid: Int, from: Long, to: Long, path: String): Boolean

    /**
     * Installs a hook at the target address.
     */
    external fun installHook(pid: Int, targetAddress: Long, replacementAddress: Long): Boolean

    /**
     * Removes a previously installed hook.
     */
    external fun removeHook(pid: Int, targetAddress: Long): Boolean

    /**
     * Sets the speed multiplier for the speedhack engine.
     */
    external fun setSpeed(speed: Double): Boolean

    /**
     * Retrieves up to the specified number of addresses discovered by the native scanner.
     */
    external fun getResults(limit: Int): LongArray
}
