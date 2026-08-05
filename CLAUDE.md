# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

AndroidGameEx is an Android memory editor for game modification, requiring root access. It provides a floating overlay interface for runtime memory scanning and modification of other processes.

**Key Technologies:**
- Kotlin (UI/Android layer)
- C++ (native memory scanning engine)
- JNI bridge via `NativeScanner`
- CMake 3.22.1+ for native builds
- Gradle 8.2.2 with AGP
- Minimum API 26 (Android 8.0), Target API 34

## Build Commands

```bash
# Full build (Kotlin + Native)
./gradlew build

# Debug APK (outputs to app/build/outputs/apk/debug/)
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Clean build
./gradlew clean build
```

The native library (`libnative-scanner.so`) is built automatically via externalNativeBuild during the Gradle build process.

## Dual Package Architecture

The codebase has **two parallel implementations**:

### 1. `com.techted89.gameex` (Original/Legacy)
- Entry: `ProcessSelectorActivity` (launcher activity per manifest)
- Core: `FloatingOverlayService` - implements the floating overlay UI
- Bridge: `NativeScanner.kt` - JNI interface to C++ scanner
- Utils: `RootUtils`, `ProcessUtils`, `SystemUtils`
- Feature: Lua scripting support via `GameGuardianAPI`

### 2. `com.aether.memoryeditor` (Modern/Refactored)
- Entry: `AetherApplication` - DI setup, notification channels
- Architecture: MVVM with repositories and ViewModels
- Screens: `ScannerScreen`, `ResultsScreen`, `ScriptsScreen`, `SettingsScreen`
- Note: This appears to be a redesign coexisting with the original codebase

**When modifying UI:** Determine which package the user is referencing. The manifest currently launches `ProcessSelectorActivity` (techted89 package), so that's the active entry point.

## Native Layer (C++)

Location: `app/src/main/cpp/`

**Core Files:**
- `memory_scanner.cpp` - Main memory scanning engine using `process_vm_readv`
- `lua_disassembler.cpp` - Lua bytecode tools (currently stubbed)
- `speedhack.cpp` - Speed manipulation (currently stubbed)
- `CMakeLists.txt` - Builds `libnative-scanner.so`

**Key JNI Functions (exposed in NativeScanner.kt):**
- `searchMemory(pid, query, type)` - Initial memory scan
- `filterMemory(pid, query, type)` - Refine results ("Next Scan")
- `startFuzzyScan(pid, type)` / `filterFuzzy(pid, mode, type)` - Fuzzy scanning
- `readMemory(pid, address, size)` - Direct memory read
- `getResults(limit)` - Retrieve found addresses
- `getLoadedModules(pid)` - Parse `/proc/[pid]/maps`

**Architecture Notes from docs/ARCHITECTURE.md:**
- Real implementations: memory scanning, reading, module enumeration
- Stubbed features: speedhack, hooking, Lua disassembly (require additional deps)
- All memory operations use Linux `process_vm_readv` API and require root

## Root Access Pattern

The app requires `su` to access `/proc/[pid]/mem` and `/proc/[pid]/maps`. When debugging root-related failures:
1. Check `RootUtils.kt` (techted89 package) or `RootManager` (aether package)
2. Verify the device is rooted and su binary is accessible
3. Native code logs to logcat via `__android_log_print`

## Testing Strategy

Unit tests live in `app/src/test/`. The native scanner can be tested in isolation by:
1. Calling JNI functions with a test PID (e.g., the app's own PID)
2. Checking return values (result counts, byte arrays)
3. Note: Full functionality requires root on a real device/emulator

Mock implementations exist for development without live rooted processes (see docs/ARCHITECTURE.md Safety Strategy).

## Floating Overlay Flow

1. User selects target process in `ProcessSelectorActivity`
2. App starts `FloatingOverlayService` with target PID
3. Service displays floating icon over other apps (requires SYSTEM_ALERT_WINDOW permission)
4. User interacts with overlay to scan/modify memory while the target app runs

When debugging overlay issues, check:
- `SYSTEM_ALERT_WINDOW` permission granted
- Service foreground notification active
- Target PID still valid (process not killed)

## Common Pitfalls

- **Duplicate constants in NativeScanner.kt:** The file has duplicate type constants (lines 4-16 and 22-29). Use the second set for consistency.
- **Manifest package mismatch:** Manifest references `ProcessSelectorActivity` but multiple Application classes exist. Current active: no custom Application in manifest (default behavior).
- **Native build failures:** Ensure NDK is installed via Android Studio SDK Manager. CMake version must be 3.22.1+.
- **Root errors at runtime:** Memory operations will silently fail or return 0 results if root access is denied. Check logcat for native error messages.
