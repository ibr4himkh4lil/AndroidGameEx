package com.aether.memoryeditor.util

import timber.log.Timber
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object RootManager {
    private var isRooted: Boolean? = null
    private var rootProcess: Process? = null
    private var rootOutputStream: DataOutputStream? = null

    fun initialize() {
        checkRootAccess()
    }

    fun isRootAvailable(): Boolean {
        if (isRooted == null) {
            checkRootAccess()
        }
        return isRooted ?: false
    }

    private fun checkRootAccess(): Boolean {
        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            outputStream.writeBytes("id\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()

            val exitCode = process.waitFor()
            isRooted = (exitCode == 0)

            Timber.d("Root access check: ${isRooted}")
            isRooted ?: false
        } catch (e: Exception) {
            Timber.e(e, "Root access check failed")
            isRooted = false
            false
        }
    }

    fun executeRootCommand(command: String): Pair<Boolean, String> {
        if (!isRootAvailable()) {
            return Pair(false, "Root access not available")
        }

        return try {
            val process = Runtime.getRuntime().exec("su")
            val outputStream = DataOutputStream(process.outputStream)
            val inputStream = BufferedReader(InputStreamReader(process.inputStream))
            val errorStream = BufferedReader(InputStreamReader(process.errorStream))

            outputStream.writeBytes("$command\n")
            outputStream.writeBytes("exit\n")
            outputStream.flush()

            val output = StringBuilder()
            var line: String?
            while (inputStream.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            val error = StringBuilder()
            while (errorStream.readLine().also { line = it } != null) {
                error.append(line).append("\n")
            }

            val exitCode = process.waitFor()
            val success = exitCode == 0

            val result = if (success) output.toString() else error.toString()
            Timber.d("Command: $command, Success: $success, Result: $result")

            Pair(success, result)
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute root command: $command")
            Pair(false, e.message ?: "Unknown error")
        }
    }

    fun startRootShell(): Boolean {
        if (!isRootAvailable()) {
            return false
        }

        return try {
            if (rootProcess == null || rootOutputStream == null) {
                rootProcess = Runtime.getRuntime().exec("su")
                rootOutputStream = DataOutputStream(rootProcess!!.outputStream)
            }
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to start root shell")
            false
        }
    }

    fun executeInRootShell(command: String): Boolean {
        if (rootOutputStream == null && !startRootShell()) {
            return false
        }

        return try {
            rootOutputStream?.writeBytes("$command\n")
            rootOutputStream?.flush()
            true
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute in root shell: $command")
            false
        }
    }

    fun shutdown() {
        try {
            rootOutputStream?.writeBytes("exit\n")
            rootOutputStream?.flush()
            rootOutputStream?.close()
            rootProcess?.destroy()
            rootOutputStream = null
            rootProcess = null
        } catch (e: Exception) {
            Timber.e(e, "Error shutting down root manager")
        }
    }
}
