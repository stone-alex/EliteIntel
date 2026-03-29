# ai/hands - Keyboard Simulation & Game Control

This package translates AI command responses into actual keystrokes sent to Elite Dangerous. It loads the player's key bindings from the game's
`.binds` XML file, maps Elite key names to platform input codes, and executes key sequences with correct modifier handling.

---

## Package Structure

```
ai/hands/
├── GameController.java              # Orchestrator: owns BindingsMonitor + KeyBindingExecutor
├── BindingsLoader.java              # Reads Elite's .binds XML file from disk
├── BindingsMonitor.java             # Watches the .binds file for live changes
├── KeyBindingsParser.java           # Parses XML → Map<action, KeyBinding>
│   └── KeyBinding (inner record)    # DTO: key (string), modifiers[], hold (boolean)
├── KeyBindCheck.java                # Validates that required actions have bindings
├── KeyBindingExecutor.java          # Maps Elite key names → input codes; executes sequences
├── KeyProcessor.java                # Low-level input: AWT Robot + NativeKeyInput
├── NativeKeyInput.java              # Interface for platform-native modifier keys
├── NativeKeyInputFactory.java       # Returns correct NativeKeyInput for current OS
├── WindowsNativeKeyInput.java       # Windows implementation (Win32 SendInput)
└── LinuxX11NativeKeyInput.java      # Linux X11 implementation (XSendEvent)
```

---

## Core Classes

| Class                   | Responsibility                                                                                                                                                                                           |
|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `GameController`        | Entry point for command execution. Receives a parsed `JsonObject` from `brain/`, looks up the binding, calls `KeyBindingExecutor`. Also owns `BindingsMonitor` to reload bindings when the file changes. |
| `BindingsLoader`        | Locates and reads `{BindingsPath}/StartPreset.start` to find the active preset, then loads the corresponding `.binds` file.                                                                              |
| `BindingsMonitor`       | `WatchService`-based file monitor. When the `.binds` file is modified (e.g. player changed a key in-game), it triggers a reload and updates the in-memory bindings map.                                  |
| `KeyBindingsParser`     | SAX/DOM parse of the Elite `.binds` XML. Extracts `<Primary>` and `<Secondary>` keys per action into `KeyBinding` records. Handles `Modifier` child elements.                                            |
| `KeyBindingExecutor`    | Translates Elite string key names (`Key_A`, `Key_LeftControl`, …) to either AWT `KeyEvent.VK_*` codes or native codes (`NATIVE_BASE + offset`). Calls `KeyProcessor` to execute.                         |
| `KeyProcessor`          | Singleton. Uses `java.awt.Robot` for standard keys (AWT VK range 0–0xFFFF) and `NativeKeyInput` for modifier keys that exceed AWT's range.                                                               |
| `NativeKeyInput`        | Platform interface: `keyDown(code)`, `keyUp(code)`. Implemented for Windows and Linux X11.                                                                                                               |
| `NativeKeyInputFactory` | Detects OS via `System.getProperty("os.name")` and returns the correct `NativeKeyInput` instance.                                                                                                        |

---

## Key Execution Pipeline

```
JsonObject AI response
  { "type": "command", "action": "DeployHardpoints", "params": {} }
  └─► GameController.processAiCommand(response)
        └─► BindingsMonitor.getBinding("DeployHardpoints")
              → KeyBinding { key="Key_U", modifiers=[], hold=false }
              └─► KeyBindingExecutor.execute(binding)
                    ├─ Resolve modifier codes (e.g. Key_LeftShift → NATIVE_BASE+n)
                    ├─ Resolve key code (e.g. Key_U → KeyEvent.VK_U)
                    ├─ KeyProcessor.pressModifiers(modifierCodes)    # NativeKeyInput
                    ├─ KeyProcessor.pressKey(keyCode)                # Robot.keyPress/Release
                    └─ KeyProcessor.releaseModifiers(modifierCodes)  # NativeKeyInput
```

For `hold=true` actions (e.g. thruster boost), `pressAndHoldKey()` is used with a configurable hold duration.

---

## Key Code Mapping

Elite's `.binds` file uses string key names. These are mapped in `KeyBindingExecutor`:

| Range                                 | Handled by       | Examples                                                                 |
|---------------------------------------|------------------|--------------------------------------------------------------------------|
| AWT range (0–0xFFFF)                  | `java.awt.Robot` | `Key_A`–`Key_Z`, `Key_0`–`Key_9`, `Key_F1`–`Key_F12`, arrow keys, numpad |
| Native range (NATIVE_BASE = 0x10000+) | `NativeKeyInput` | `Key_LeftControl`, `Key_RightShift`, `Key_LeftAlt`, `Key_RightAlt`       |

Modifier keys must go through `NativeKeyInput` because AWT
`Robot` cannot reliably simulate them when the target window is a native application.

---

## Safety: Blacklisted Actions

The following action names are **never executed**, regardless of what the AI returns:

```
PrimaryFire
SecondaryFire
TriggerFieldNeutraliser
BuggyPrimaryFireButton
BuggySecondaryFireButton
Humanoid*   (all actions starting with "Humanoid")
```

This prevents accidental weapon discharge or on-foot combat actions.

---

## Bindings File Location

The
`.binds` file is the standard Elite Dangerous key binding file located in the OS-specific game config directory (e.g.
`~/.local/share/Frontier Developments/Elite Dangerous/Options/Bindings/` on Linux).
`BindingsLoader` resolves this path from config at startup.

`BindingsMonitor` watches this path continuously so that if the player remaps a key in-game without restarting EliteIntel, the new binding takes effect immediately.

---

## EventBus

| Direction | Event                | Notes                                                          |
|-----------|----------------------|----------------------------------------------------------------|
| Published | `AiVoxResponseEvent` | Error messages only (e.g. "No key binding found for action X") |

`GameController.processAiCommand()` is called directly by
`brain/` - not via the event bus - to keep the command execution path synchronous and traceable.

---

## Platform Notes

- **Linux**: `LinuxX11NativeKeyInput` uses X11 `XSendEvent` via JNI. Requires the native library on `java.library.path`.
- **Windows**: `WindowsNativeKeyInput` uses Win32 `SendInput` via JNI.
- `NativeKeyInputFactory` selects the right implementation automatically; no caller code changes needed when porting.
