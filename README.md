<img src="src/main/resources/META-INF/pluginIcon.svg" width="80" alt="Project Tabs logo">

# Project Tabs

Shows every open project as a tab in the main toolbar of Android Studio (and any other IntelliJ-based IDE with the new
UI), like documents in a word processor or pages in a browser.

```
┌──────────────────────────────────────────────────────────────────────────────┐
│ ●●● ≡ MyApp▾ main▾ │ MyApp  × │ core-lib │ backend-sample │ ⌄ │ ＋ │ ▶ app▾ ⚙ 🔍 │
└──────────────────────────────────────────────────────────────────────────────┘
```

The IDE still opens one window per project; the plugin makes them behave like tabs of a single window.

- **Switch** by clicking a tab or with the shortcuts below. The window you switch to takes the current window's size,
  position and maximized state, so it appears in the same place.
- **Close** a project with the tab's ×, a middle-click, or the context menu (Close, Close Others, Close Tabs to the
  Right). The IDE's usual checks still run, e.g. "terminate running processes?". Closing the current tab activates
  the one to its right.
- **Open** a recent project or any folder as a new tab with **＋**.
- **Reorder** tabs by dragging; the order survives restarts.
- **Many projects?** Tabs shrink, then the rest moves into the **⌄** popup, which supports type-to-search.

## Shortcuts

All actions are in **Window | Project Tabs** and can be rebound in **Settings | Keymap**.

| Action                       | macOS                  | Windows / Linux                     |
|------------------------------|------------------------|-------------------------------------|
| Next Project Tab             | ⌥⌘\`                   | Ctrl+Alt+]                          |
| Previous Project Tab         | ⇧⌥⌘\`                  | Ctrl+Alt+[                          |
| Go to Project Tab 1–8        | ⌃⌥1 … ⌃⌥8              | — (assign in Keymap)                |
| Go to Last Project Tab       | ⌃⌥9                    | — (assign in Keymap)                |
| Reopen Closed Project Tab    | ⌃⌥⇧T                   | — (Ctrl+Alt+Shift+T is Refactor This) |
| Close Project Tab, Close Others, Close to the Right, Copy Path, Reveal | — | — |

Next/Previous Project Tab share the shortcuts of the built-in *Next/Previous Project Window* and take precedence over
them, so cycling follows the tab order. The Windows/Linux defaults for the other actions are left empty because every
candidate collides with an existing shortcut in the default, GNOME, KDE or XWin keymaps.

## Settings

**Settings | Tools | Project Tabs**

- Show project tabs in the main toolbar
- Hide tabs when only one project is open
- Move the window you switch to into the current window's place (on by default; full-screen windows are never moved)
- Open projects in a new window, so each one becomes a tab (the IDE's own *Open project in* option)
- Close button: on the active tab and on hover / on every tab / never
- New tabs open at the end / next to the current tab

## Requirements and limitations

- Android Studio 2025.3 or newer, IntelliJ IDEA 2025.3 or newer, or any IntelliJ-based IDE on build 253+.
- New UI only. With the Classic UI plugin the strip does not appear; the shortcuts and Window menu still work.
- On macOS, a project in native full screen lives in its own Space, so switching to or from it cannot keep the
  window in place.
- If the IDE is set to open projects *in this window*, opening a project replaces the current tab. The plugin offers
  once to change this, and **＋** always opens a new tab.

## Development

Requirements: JDK 21. Gradle is provided by the wrapper.

```shell
./gradlew build          # compile, unit tests, plugin zip in build/distributions
./gradlew runIde         # start a sandboxed IDE with the plugin
./gradlew verifyPlugin   # IntelliJ Plugin Verifier against the recommended IDE versions
```

By default the build downloads IntelliJ IDEA `platformVersion` from `gradle.properties`. To build against and run an
installed Android Studio instead, create an untracked `local.properties`:

```properties
platformLocalPath=/Applications/Android Studio.app
```

### Layout

| Path | What it is |
|------|------------|
| `service/TabOrdering.kt` | Pure ordering rules (insert, move, wrap-around, numbering, name disambiguation). Unit-tested. |
| `service/ProjectTabsService.kt` | App service: persisted order, recently closed projects, successor on close. Mirrors `ProjectManager.openProjects`. |
| `lifecycle/` | Project opened / closing / closed and app exit hooks that feed the service. |
| `switching/` | Bringing a window to front (with snapping), closing via the platform's *Close Project*, opening in a new window. |
| `ui/ProjectTabsToolbarAction.kt` | Custom component action in the `MainToolbarCenter` group that hosts the strip. |
| `ui/ProjectTabsStrip.kt`, `ui/ProjectTabComponent.kt` | The tab row, painting, mouse handling, drag to reorder, overflow and ＋ popups. |
| `ui/TabLayout.kt` | Pure width distribution / overflow logic. Unit-tested. |
| `actions/` | Keymap and Window menu actions, the action promoter, shared tab commands. |
| `settings/` | Persistent settings and the Settings page. |

### Before the first Marketplace release

- [ ] Choose the final plugin ID (`<id>` in `plugin.xml`, currently `dev.projecttabs`). It cannot change after the first upload.
- [ ] Check that the name "Project Tabs" is free on JetBrains Marketplace.
- [ ] Set the vendor name, e-mail and URL in `plugin.xml`.
- [ ] Provide `PUBLISH_TOKEN`, `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD` as environment variables
      for `./gradlew publishPlugin` ([plugin signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html)).
- [ ] Upload the first version manually at https://plugins.jetbrains.com/plugin/add; later releases can be published
      with `./gradlew publishPlugin`.

## License

[Apache License 2.0](LICENSE)
