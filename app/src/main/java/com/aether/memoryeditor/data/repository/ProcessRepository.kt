package com.aether.memoryeditor.data.repository

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.aether.memoryeditor.data.model.ProcessInfo
import com.aether.memoryeditor.util.RootManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class ProcessRepository(private val context: Context) {

    private val _processes = MutableStateFlow<List<ProcessInfo>>(emptyList())
    val processes: StateFlow<List<ProcessInfo>> = _processes.asStateFlow()

    private val _selectedProcess = MutableStateFlow<ProcessInfo?>(null)
    val selectedProcess: StateFlow<ProcessInfo?> = _selectedProcess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val packageManager: PackageManager = context.packageManager

    suspend fun refreshProcesses() {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val processList = mutableListOf<ProcessInfo>()
                val procDir = File("/proc")

                procDir.listFiles()?.forEach { file ->
                    if (file.isDirectory && file.name.all { it.isDigit() }) {
                        val pid = file.name.toIntOrNull() ?: return@forEach
                        val processInfo = getProcessInfo(pid)
                        if (processInfo != null) {
                            processList.add(processInfo)
                        }
                    }
                }

                _processes.value = processList.sortedBy { it.name }
                Timber.d("Loaded ${processList.size} processes")
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing processes")
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun searchProcesses(query: String) {
        _isLoading.value = true
        withContext(Dispatchers.IO) {
            try {
                val allProcesses = _processes.value
                val filtered = if (query.isBlank()) {
                    allProcesses
                } else {
                    allProcesses.filter {
                        it.name.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true) ||
                        it.pid.toString().contains(query)
                    }
                }
                _processes.value = filtered
            } catch (e: Exception) {
                Timber.e(e, "Error searching processes")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectProcess(process: ProcessInfo) {
        _selectedProcess.value = process
        Timber.d("Selected process: ${process.name} (${process.pid})")
    }

    fun clearSelection() {
        _selectedProcess.value = null
    }

    private fun getProcessInfo(pid: Int): ProcessInfo? {
        return try {
            val cmdlineFile = File("/proc/$pid/cmdline")
            if (!cmdlineFile.exists()) return null

            val packageName = BufferedReader(FileReader(cmdlineFile)).use { reader ->
                reader.readLine()?.replace("\u0000", "") ?: return null
            }

            if (packageName.isEmpty()) return null

            val statusFile = File("/proc/$pid/status")
            val name = if (statusFile.exists()) {
                BufferedReader(FileReader(statusFile)).use { reader ->
                    reader.lineSequence()
                        .find { it.startsWith("Name:") }
                        ?.substring(5)
                        ?.trim()
                        ?: packageName
                }
            } else {
                packageName
            }

            val uid = try {
                File("/proc/$pid/status").bufferedReader().use { reader ->
                    reader.lineSequence()
                        .find { it.startsWith("Uid:") }
                        ?.split("\\s+".toRegex())
                        ?.getOrNull(1)
                        ?.toInt() ?: 0
                }
            } catch (e: Exception) {
                0
            }

            val icon = try {
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                packageManager.getApplicationIcon(appInfo)
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }

            ProcessInfo(
                pid = pid,
                name = name,
                packageName = packageName,
                uid = uid,
                icon = icon
            )
        } catch (e: Exception) {
            Timber.e(e, "Error getting process info for PID $pid")
            null
        }
    }

    fun getProcessByPid(pid: Int): ProcessInfo? {
        return _processes.value.find { it.pid == pid }
    }

    fun getRunningApps(): List<ProcessInfo> {
        return _processes.value.filter { processInfo ->
            try {
                val appInfo = packageManager.getApplicationInfo(processInfo.packageName, 0)
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }
        }
    }
}
