package com.aether.memoryeditor.data.repository

import android.content.Context
import android.net.Uri
import com.aether.memoryeditor.data.model.DataType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import java.io.File

class ScriptRepository(private val context: Context) {

    data class ScriptInfo(
        val id: Long,
        val name: String,
        val content: String,
        val lastModified: Long,
        val path: String = ""
    )

    private val _scripts = MutableStateFlow<List<ScriptInfo>>(emptyList())
    val scripts: StateFlow<List<ScriptInfo>> = _scripts.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _consoleOutput = MutableStateFlow<List<String>>(emptyList())
    val consoleOutput: StateFlow<List<String>> = _consoleOutput.asStateFlow()

    private val scriptsDir = File(context.filesDir, "scripts")
    private var nextId = 1L

    init {
        scriptsDir.mkdirs()
        loadScripts()
    }

    private fun loadScripts() {
        val scriptFiles = scriptsDir.listFiles() ?: return
        val loadedScripts = scriptFiles.mapNotNull { file ->
            try {
                ScriptInfo(
                    id = nextId++,
                    name = file.nameWithoutExtension,
                    content = file.readText(),
                    lastModified = file.lastModified(),
                    path = file.absolutePath
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to load script: ${file.name}")
                null
            }
        }
        _scripts.value = loadedScripts
    }

    fun createScript(name: String, content: String): ScriptInfo {
        val script = ScriptInfo(
            id = nextId++,
            name = name,
            content = content,
            lastModified = System.currentTimeMillis()
        )
        saveScriptToFile(script)
        _scripts.value = _scripts.value + script
        return script
    }

    fun saveScript(script: ScriptInfo) {
        saveScriptToFile(script)
        _scripts.value = _scripts.value.map {
            if (it.id == script.id) script else it
        }
    }

    fun deleteScript(scriptId: Long) {
        val script = _scripts.value.find { it.id == scriptId } ?: return
        if (script.path.isNotEmpty()) {
            File(script.path).delete()
        }
        _scripts.value = _scripts.value.filter { it.id != scriptId }
    }

    suspend fun importScript(uri: Uri, name: String) {
        try {
            val content = context.contentResolver.openInputStream(uri)?.use {
                it.bufferedReader().readText()
            } ?: return

            createScript(name, content)
            Timber.d("Imported script: $name")
        } catch (e: Exception) {
            Timber.e(e, "Failed to import script")
        }
    }

    suspend fun exportScript(script: ScriptInfo, uri: Uri) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(script.content.toByteArray())
            }
            Timber.d("Exported script: ${script.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to export script")
        }
    }

    fun runScript(script: ScriptInfo) {
        if (_isRunning.value) {
            addConsoleOutput("Error: A script is already running")
            return
        }

        _isRunning.value = true
        addConsoleOutput("Running script: ${script.name}")

        try {
            // Placeholder for actual Lua execution
            // In a real implementation, this would use a Lua interpreter
            addConsoleOutput("Script execution not yet implemented")
            addConsoleOutput("Content:\n${script.content}")
        } catch (e: Exception) {
            addConsoleOutput("Error: ${e.message}")
            Timber.e(e, "Failed to run script")
        } finally {
            _isRunning.value = false
            addConsoleOutput("Script finished")
        }
    }

    fun stopScript() {
        _isRunning.value = false
        addConsoleOutput("Script stopped by user")
    }

    fun clearConsole() {
        _consoleOutput.value = emptyList()
    }

    private fun addConsoleOutput(message: String) {
        _consoleOutput.value = _consoleOutput.value + message
    }

    private fun saveScriptToFile(script: ScriptInfo) {
        try {
            val file = File(scriptsDir, "${script.name}.lua")
            file.writeText(script.content)
            Timber.d("Saved script: ${script.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save script: ${script.name}")
        }
    }
}
