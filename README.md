<img src="src/main/resources/META-INF/pluginIcon.svg" width="64" alt="">

<p align="center">
  <img src="docs/media/switch-tabs.gif" alt="Switch tabs" width="100%">
</p>

<p align="center">
  <img src="docs/media/open-project.gif" alt="Open a project" width="49%" align="top" vspace="2">
  <img src="docs/media/close-and-reopen.gif" alt="Close and reopen a tab" width="49%" align="top" vspace="2">
  <img src="docs/media/reorder-tabs.gif" alt="Reorder tabs" width="49%" align="top" vspace="2">
  <img src="docs/media/hidden-tabs.png" alt="Hidden tabs" width="49%" align="top" vspace="2">
  <img src="docs/media/tabs.png" alt="Tabs" width="49%" align="top" vspace="2">
  <img src="docs/media/light-theme.png" alt="Light theme" width="49%" align="top" vspace="2">
  <img src="docs/media/context-menu.png" alt="Context menu" width="49%" align="top" vspace="2">
  <img src="docs/media/settings.png" alt="Settings" width="49%" align="top" vspace="2">
</p>

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
