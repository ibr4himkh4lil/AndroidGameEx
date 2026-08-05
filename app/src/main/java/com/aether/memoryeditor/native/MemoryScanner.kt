package com.aether.memoryeditor.native

import com.aether.memoryeditor.data.model.DataType
import com.aether.memoryeditor.data.model.MemoryRegion
import com.aether.memoryeditor.data.model.ScanResult
import com.aether.memoryeditor.data.model.SearchType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber

class MemoryScanner(private val nativeBridge: NativeBridge) {

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _progress = MutableStateFlow(0)
    val progress: StateFlow<Int> = _progress.asStateFlow()

    private val _resultCount = MutableStateFlow(0L)
    val resultCount: StateFlow<Long> = _resultCount.asStateFlow()

    private var currentPid: Int = -1
    private val frozenAddresses = mutableMapOf<Long, FrozenValue>()

    data class FrozenValue(
        val address: Long,
        val value: ByteArray,
        val type: DataType
    )

    fun startScan(
        value: String,
        dataType: DataType,
        searchType: SearchType,
        regions: Set<MemoryRegion>,
        alignedOnly: Boolean = false,
        xorKey: Int? = null
    ) {
        if (currentPid == -1) {
            Timber.e("No process attached")
            return
        }

        _isScanning.value = true
        _progress.value = 0

        try {
            when (searchType) {
                SearchType.EXACT -> {
                    val count = nativeBridge.searchMemory(currentPid, value, dataType.id)
                    _resultCount.value = count.toLong()
                }
                SearchType.RANGE -> {
                    val count = nativeBridge.searchMemory(currentPid, value, dataType.id)
                    _resultCount.value = count.toLong()
                }
                SearchType.FUZZY_CHANGED,
                SearchType.FUZZY_UNCHANGED,
                SearchType.FUZZY_INCREASED,
                SearchType.FUZZY_DECREASED -> {
                    nativeBridge.startFuzzyScan(currentPid, dataType.id)
                    _resultCount.value = 0
                }
                else -> {
                    Timber.w("Unsupported search type: $searchType")
                }
            }
            _progress.value = 100
        } catch (e: Exception) {
            Timber.e(e, "Error during scan")
        } finally {
            _isScanning.value = false
        }
    }

    fun refineScan(
        searchType: SearchType,
        value: String? = null,
        dataType: DataType = DataType.AUTO
    ) {
        if (currentPid == -1) {
            Timber.e("No process attached")
            return
        }

        _isScanning.value = true
        _progress.value = 0

        try {
            when (searchType) {
                SearchType.EXACT, SearchType.RANGE -> {
                    if (value != null) {
                        val count = nativeBridge.filterMemory(currentPid, value, dataType.id)
                        _resultCount.value = count.toLong()
                    }
                }
                SearchType.FUZZY_CHANGED -> {
                    val count = nativeBridge.filterFuzzy(currentPid, 0, dataType.id)
                    _resultCount.value = count.toLong()
                }
                SearchType.FUZZY_UNCHANGED -> {
                    val count = nativeBridge.filterFuzzy(currentPid, 1, dataType.id)
                    _resultCount.value = count.toLong()
                }
                SearchType.FUZZY_INCREASED -> {
                    val count = nativeBridge.filterFuzzy(currentPid, 2, dataType.id)
                    _resultCount.value = count.toLong()
                }
                SearchType.FUZZY_DECREASED -> {
                    val count = nativeBridge.filterFuzzy(currentPid, 3, dataType.id)
                    _resultCount.value = count.toLong()
                }
                else -> {
                    Timber.w("Unsupported refine search type: $searchType")
                }
            }
            _progress.value = 100
        } catch (e: Exception) {
            Timber.e(e, "Error during refine scan")
        } finally {
            _isScanning.value = false
        }
    }

    fun getResults(offset: Long, limit: Long): List<ScanResult> {
        if (currentPid == -1) return emptyList()

        return try {
            val addresses = nativeBridge.getResults(limit.toInt())
            addresses.mapNotNull { address ->
                try {
                    val value = nativeBridge.readMemory(currentPid, address, 4)
                    ScanResult(
                        address = address,
                        value = value,
                        dataType = DataType.DWORD,
                        isFrozen = frozenAddresses.containsKey(address)
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting results")
            emptyList()
        }
    }

    fun clearResults() {
        _resultCount.value = 0
        _progress.value = 0
    }

    fun cancelScan() {
        _isScanning.value = false
    }

    fun attachProcess(pid: Int) {
        currentPid = pid
        Timber.d("Attached to process: $pid")
    }

    fun modifyValue(address: Long, value: ByteArray, type: DataType): Boolean {
        if (currentPid == -1) return false

        return try {
            // Use native bridge to write memory
            // This is a placeholder - actual implementation depends on native layer
            Timber.d("Modified value at 0x${address.toString(16)}")
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to modify value")
            false
        }
    }

    fun freezeValue(address: Long, value: ByteArray, type: DataType) {
        frozenAddresses[address] = FrozenValue(address, value, type)
        Timber.d("Frozen value at 0x${address.toString(16)}")
    }

    fun unfreezeValue(address: Long) {
        frozenAddresses.remove(address)
        Timber.d("Unfrozen value at 0x${address.toString(16)}")
    }

    fun unfreezeAll() {
        frozenAddresses.clear()
        Timber.d("Unfrozen all values")
    }
}
