package com.aether.memoryeditor.di

import android.content.Context
import com.aether.memoryeditor.data.repository.ProcessRepository
import com.aether.memoryeditor.data.repository.ScanRepository
import com.aether.memoryeditor.native.MemoryScanner
import com.aether.memoryeditor.native.NativeBridge
import com.aether.memoryeditor.ui.viewmodel.ScannerViewModel
import com.aether.memoryeditor.ui.viewmodel.ScriptViewModel
import com.aether.memoryeditor.ui.viewmodel.SettingsViewModel

class AppModule(private val context: Context) {

    private val nativeBridge: NativeBridge by lazy {
        NativeBridge()
    }

    private val memoryScanner: MemoryScanner by lazy {
        MemoryScanner(nativeBridge)
    }

    val scanRepository: ScanRepository by lazy {
        ScanRepository(memoryScanner)
    }

    val processRepository: ProcessRepository by lazy {
        ProcessRepository(context)
    }

    fun provideScannerViewModel(): ScannerViewModel {
        return ScannerViewModel(scanRepository, processRepository)
    }

    fun provideScriptViewModel(): ScriptViewModel {
        return ScriptViewModel()
    }

    fun provideSettingsViewModel(): SettingsViewModel {
        return SettingsViewModel(context)
    }

    fun provideContext(): Context = context
}
