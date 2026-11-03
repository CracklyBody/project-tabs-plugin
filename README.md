<img src="src/main/resources/META-INF/pluginIcon.svg" width="64" alt="">

# Onewindow

Open projects as tabs in the main toolbar of Android Studio and other IntelliJ-based IDEs.

![Onewindow](docs/screenshot.png)

- Click a tab to switch. The window opens in the same place and size.
- Close with × or middle-click, open a project with ＋.
- Drag to reorder, right-click for more.

## Shortcuts

| Action               | macOS          | Windows / Linux         |
|----------------------|----------------|-------------------------|
| Next / previous tab  | ⌥⌘\` / ⇧⌥⌘\` | Ctrl+Alt+] / Ctrl+Alt+[ |
| Go to tab 1–8 / last | ⌃⌥1…8 / ⌃⌥9    | —                       |
| Reopen closed tab    | ⌃⌥⇧T           | —                       |

Rebind them in Settings | Keymap under Window | Onewindow. Options are in Settings | Tools | Onewindow.

Requires version 2025.3 or newer with the new UI.

## Build

```shell
./gradlew buildPlugin   # zip in build/distributions
./gradlew runIde
```

To build against a local Android Studio, put `platformLocalPath=/Applications/Android Studio.app` in `local.properties`.

## License

Proprietary. See [LICENSE](LICENSE).
