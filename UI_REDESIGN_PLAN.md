# BeatKenja UI Redesign Plan

> Status: **Draft v2 — decisions locked** (2026-07-02). Implementation not started — this is the design document.
>
> Decisions from review:
> - Database is gone (was painful to set up) → Advanced Map Creator loses its DB-driven genre/tag source; keep only what works without DB, behind an "experimental" section.
> - ArcViewer integration: wanted. See §4.4 — clone+build not possible (Unity), strategy is auto-downloaded desktop release now, optional JCEF embed later. We will probably build our own custom viewer later. 
> - Visual section-timeline editor + integrated map editor: **later**, tracked in §7 future ideas. Design must not block it.
> - Batch stays. Per-diff tabs with individual Generate + **"Apply to all diffs"** button that copies current settings/action to all loaded diffs.

---

## 1. Current state (findings)

### 1.1 Tech stack reality check
- The UI is **Java Swing, not JavaFX**. `UserInterface extends JFrame`; all controls are `JButton`/`JLabel`/`JCheckBox`/`JSlider`.
- JavaFX is only used for `javafx.util.Pair` and the WebView-based Markdown/Readme viewer (`Start.java:97`, `Elements/Frames/MarkdownViewer.java`). All JavaFX modules are already in `pom.xml`.
- Charts (NPS plot, pattern heatmap) use **JFreeChart** in separate popup windows.
- Layout: `setLayout(null)` with hardcoded absolute pixel bounds for every control, centralized in `Elements/ElementTypes.java`. Fixed 1200x800 window; not resize-safe.

### 1.2 Architecture of the current UI
- Entry: `Start.java` → `new UserInterface()` (GUI) or `Start_CLI` (headless). CLI already proves the **generation core is UI-independent** — important for any rewrite.
- Custom widget system: `MyButton extends JButton` with a `childElements` list; clicking a parent toggles child visibility. Categories: Map Utilities, Map to Timing Notes, Map Creator, Advanced Map Creator, plus an invisible `GlobalButton` that "owns" global controls.
- The 4 category buttons become visible via a **1-second polling thread** watching `mapSuccessfullyLoaded` (`UserInterface.java:111-146`).
- State is global/static: `Parameters.*`, `UserInterface.patternVariance`, `UserInterface.currentDiff`. Buttons mutate UI state of *other* controls directly (e.g. `GlobalOpenMapButton` re-bounds and recolors itself and the label on load/error).
- Feedback channel: a `StatusCheckTextPane` log dump inside the main window (log4j `GuiAppender` writes into it).

### 1.3 Concrete pain points (verified in code + screenshot `_wiki/Map_loaded_successfully.png`)
| # | Pain point | Evidence |
|---|-----------|----------|
| P1 | Controls overlap when everything is visible | "SAVE MAP AS" covers "select Difficulty"; "Open Map in Brow…" covers the NPS field; screenshot shows clipped/truncated labels ("one han…", "rand. V2") |
| P2 | Hidden functionality — sub-buttons appear only after clicking parent; nothing indicates they exist | `MyButton.onClick()` visibility toggling |
| P3 | No workflow guidance. The actual flow (load → timing notes → generate → save) is invisible; wiki says "It is a lot of information at once" | `_wiki/JarUiTutorial.md` |
| P4 | 15px-high sub-buttons, cryptic labels, inconsistent ad-hoc colors (cyan/green/orange/pink/gray) | `ElementTypes.java`, screenshot |
| P5 | Generated map exists only in memory; user must *know* to press SAVE; save overwrites files (backup optional in dialog) | `GlobalSaveMapAs.onClick()` |
| P6 | Load button doubles as status indicator (turns green/red, changes its own bounds & text) — button ≠ status | `GlobalOpenMapButton.successfullyLoaded()/errorWhileLoading()` |
| P7 | NPS plot pops up as separate window on every load; advanced feature forced on beginners | `GlobalOpenMapButton.successfullyLoaded()`, wiki: "You will probably not need it" |
| P8 | Polling thread for visibility; busy-wait design; no reactive state | `UserInterface.java` `@SuppressWarnings("BusyWait")` |
| P9 | Dark mode is a hand-rolled per-component `if (darkMode)` foreground/background patch | `UIElements.java` |
| P10 | No progress indication during generation (only timeout via `runWithTimeout`); log pane is the only feedback | `MapCreatorSubButton.runWithTimeout()` |
| P11 | Config only editable by hand in `config.json`; no settings UI | `DataManager/Config`, `Parameters.java` |

---

## 2. Main user flows

Flows extracted from README, wiki, and button code:

1. **Load** — open one difficulty (.dat/.json) or a whole map folder (all diffs); BPM auto-read from `info.dat`.
2. **Convert to timing notes** — 1-color (required input format for generation!) or 2-color. Pre-processing step.
3. **Generate** — from timing notes: Linear / Complex / bookmark-sectioned "Create Map" / Random / one-handed variants. Parameters: pattern file (.pat or .dat), variance slider, BPM, seed, "Ignore DDs".
4. **Advanced generate** (experimental) — genre/tag/difficulty/BPM/NPS-informed.
5. **Utilities** — no-arrow conversion, delete note type, fix placements (grid snap), convert flashing lights.
6. **Batch onsets** — MP3 folder → timing maps via Python/librosa pipeline.
7. **Review** — NPS distribution plot, pattern heatmap, parity error warnings (can be saved as editor bookmarks).
8. **Export** — SAVE MAP AS (overwrite + optional backup); "Open Map in Browser" (zip + web previewer).

### The critical path (what 90% of sessions look like)
```
Load diff ──► (optional: to 1-color timing notes) ──► pick generator + tweak params ──► generate ──► check warnings ──► SAVE
```
The current UI renders this path as scattered buttons with no order, no state indication, and no result preview. **That is the core problem to fix.**

---

## 3. Proposed UI

### 3.1 Design concept: workflow-oriented single window
Replace the "button soup" with a **stepped sidebar workflow** — the UI itself teaches the process. One persistent window, no surprise popups; charts and viewers become tabs/panels.

### 3.2 Wireframe — main window

```
┌────────────────────────────────────────────────────────────────────────────┐
│ ⬡ BeatKenja      [ Open… ▾ ]  [ Recent ▾ ]                    [ ⚙ ] [ ? ]  │ ← toolbar
├──────────────┬─────────────────────────────────────────────────────────────┤
│  WORKFLOW    │  Song: „Example Song“   BPM 192   Seed 433472794 [↻]        │ ← map header
│              │  Diffs: (Easy)(Normal)(Hard)(Expert)(●ExpertPlus)           │   (chips = loaded
│ ● 1 Load     │─────────────────────────────────────────────────────────────│    diffs, click to
│ ○ 2 Timing   │                                                             │    switch)
│ ○ 3 Generate │   ┌──────────────────────────────┐  ┌─────────────────────┐ │
│ ○ 4 Review   │   │                              │  │ PARAMETERS          │ │
│ ○ 5 Export   │   │   STEP CONTENT AREA          │  │ Pattern: default ▾  │ │
│              │   │                              │  │ [Load .pat/.dat…]   │ │
│──────────────│   │   (changes per step,         │  │ Variance ────○──── │ │
│  TOOLS       │   │    see 3.3)                  │  │ ☐ Ignore DDs        │ │
│  Utilities   │   │                              │  │ ☐ One-handed        │ │
│  Batch MP3   │   │                              │  │ [Visualize pattern] │ │
│  Patterns    │   └──────────────────────────────┘  └─────────────────────┘ │
├──────────────┴─────────────────────────────────────────────────────────────┤
│ ✓ Map generated (ExpertPlus) — 3 parity warnings [Show]      [ Log ▴ ]     │ ← status bar,
└────────────────────────────────────────────────────────────────────────────┘   expandable log
```

Rules:
- Steps 2–5 disabled (grayed, with tooltip why) until a map is loaded → replaces the polling thread with event-driven enablement.
- Status bar replaces color-morphing buttons: load state, generation result, parity warnings, expandable log drawer (keeps `StatusCheckTextPane` value without dominating the window).
- Parameters panel is always visible during Generate — no hidden toggles.

**Per-diff tabs (decision):** the diff chips in the map header are real tabs. Each tab holds its own settings + generation result (own `DiffSession`: generator choice, pattern, variance, seed, result, parity warnings). Steps 2–5 operate on the active tab. Every step view gets an **[Apply to all diffs]** button that replays the current tab's settings/action onto all loaded diffs (batch generate runs as one background task with per-diff progress). Export view keeps the batch table across all diffs — so folder-load → generate-all → save-all stays a 3-click flow.

### 3.3 Step content

**1 Load** — drop zone ("Drop .dat / map folder here or click to browse"), recent files list, per-diff load status table. On success auto-advance to step 3 (timing conversion optional, offered as hint if map isn't 1-color).

**2 Timing** — two cards: "→ 1-color timing notes (required for generation)" / "→ 2-color". Preview of note count before/after. Explains *why* this step exists (currently only in README).

**3 Generate** — generator picker as **cards with descriptions + the existing GIFs** (assets/ already has linear.gif, complex.gif, …):
```
┌ Linear ─────────┐ ┌ Complex ─────────┐ ┌ Sectioned (bookmarks) ┐ ┌ Random ─┐
│ [gif]           │ │ [gif]            │ │ [gif]                 │ │ [gif]   │
│ Simple, no DDs  │ │ Varied, may DD   │ │ Uses map bookmarks:   │ │ Chaos   │
│                 │ │                  │ │ linear|complex|jumps… │ │         │
└─[ Generate ]────┘ └─[ Generate ]─────┘ └─[ Generate ]──────────┘ └─────────┘
   Advanced (experimental) ▾  → difficulty/BPM/NPS controls, collapsed
```
Progress indicator while generating (replaces silent `runWithTimeout`).

Advanced section note: the DB is gone, so genre/tag dropdowns (formerly BeatSaver-DB-driven) are dropped. Only the parameters that work standalone (target difficulty, BPM, NPS) survive, collapsed under "Advanced (experimental)". Dead DB code paths get removed during stage 1.

**4 Review** — embedded tabs instead of popup windows: NPS distribution chart, pattern heatmap, **parity warnings table** (beat, type, jump-to). "Save warnings as bookmarks" action here.

**5 Export** — explicit, safe:
- Target table across **all** diffs (not just active tab): destination path per diff, editable, checkbox per row.
- Default: **backup ON** (invert current default).
- Buttons: [Save all] [Save selected] [Export .zip] [Preview in ArcViewer] (see §4.4).

**Tools (sidebar, modal-free panels):** Utilities (no-arrow, delete type, fix placements, lights) with the loaded map as target; Batch MP3→timings with folder pickers + per-file progress list; Pattern manager (load/visualize .pat).

**Settings (⚙):** editor for `config.json` (paths, dark mode, previewer URL, parity-bookmark flags) — removes hand-editing (P11).

### 3.4 Component hierarchy (target)

```
AppShell
├── ToolBar            (Open, Recent, Settings, Help)
├── SideBar
│   ├── WorkflowNav    (5 steps, state-aware enable/disable)
│   └── ToolsNav       (Utilities, Batch MP3, Patterns)
├── MapHeader          (song, BPM field, seed field, DiffTabBar)
├── ContentArea        (swaps per nav selection; steps 2–4 bound to active DiffSession)
│   ├── LoadView
│   ├── TimingView         [Apply to all diffs]
│   ├── GenerateView ────── ParameterPanel + [Apply to all diffs]
│   ├── ReviewView ──────── NpsChart | PatternHeatmap | ParityTable
│   ├── ExportView         (batch table over all DiffSessions)
│   ├── PreviewerView      (ArcViewer launcher/embed, §4.4)
│   ├── UtilitiesView | BatchMp3View | PatternsView
│   └── SettingsView
└── StatusBar          (state chip, warnings, expandable LogDrawer)

State model: MapSession (song, BPM, paths) ─ 1..n ─► DiffSession
(generator, pattern, variance, seed, generated result, parity warnings).
"Apply to all diffs" = copy active DiffSession settings → run action per DiffSession.
```

### 3.5 State/flow diagram

```
            ┌─────────┐  open file/folder   ┌──────────┐
            │  EMPTY  │ ───────────────────►│  LOADED  │◄─── load another
            └─────────┘                     └────┬─────┘
                 ▲                               │ generate (worker thread,
                 │ close/reset                   ▼  progress events)
            ┌────┴────┐   save/export      ┌──────────┐
            │  SAVED  │◄───────────────────│ GENERATED│──► review (charts,
            └─────────┘   (backup default) └──────────┘    parity table)
```
One observable `AppState` (loaded maps, current diff, pattern, params, generation result) drives all view enablement — kills the polling thread, the static mutable fields, and cross-component mutation.

---

## 4. Technology recommendation: JavaFX (proper), not web split

### 4.1 Options considered

| Criterion | A) Swing + FlatLaf facelift | B) **JavaFX rewrite (recommended)** | C) Web UI + local server (Spring Boot + React/Svelte) |
|---|---|---|---|
| Effort | Low–medium | Medium | High |
| Fixes structural problems (P2,P3,P8) | Only with same rework effort as B | Yes — properties/bindings replace polling; layout managers replace null-layout | Yes |
| Look & feel ceiling | Mediocre | Good (CSS theming, e.g. AtlantaFX; real dark mode → P9) | Best |
| Local file access (WIP folders, overwrite .dat, spawn Python) | Native | Native | Awkward: browser sandbox → everything through the server; path-based workflow survives only because server is local, but file dialogs/drag-drop of *folders* get clunky |
| Charts (NPS, heatmap) | JFreeChart popups | Built-in `javafx.scene.chart` / canvas, embeddable in tabs (P7) | Any JS lib |
| Markdown/help viewer | Needs JavaFX WebView anyway (already does) | Native WebView | Native |
| Packaging/distribution | Single jar (status quo) | Single jar / jlink image — user base already runs `java -jar` | Jar + embedded frontend build + port management + "open browser" dance |
| New skills/stacks | None | JavaFX (deps already in pom, WebView already used) | Two stacks, JS toolchain, HTTP API design |
| Future web previewer integration | Poor | WebView can embed ArcViewer/bs-viewer directly | Native |
| Risk | Wasted effort on dead-end architecture | Moderate, staged migration possible (Swing and JavaFX can coexist via `JFXPanel`/`SwingNode` during transition) | Big-bang; long time without shippable UI |

### 4.2 Why B
1. **This is a file-system-heavy, single-user desktop tool.** It overwrites .dat files in place, opens OS folders, and launches a local Python pipeline. A browser frontend fights all of that; JavaFX gets it for free.
2. **The dependencies are already there** (`javafx-controls`, `javafx-web`, `javafx-fxml` in pom.xml) and the codebase already boots the JavaFX platform at startup.
3. **The generation core is already UI-independent** (proven by `Start_CLI`). The rewrite touches only the `UserInterface` package — the risky-sounding "rewrite" is actually confined.
4. Web split (README's old "Spring" TODO) pays off only if remote/multi-user access or an embedded 3D map previewer becomes a hard requirement. Neither is on the current roadmap; the browser previewer works fine via the existing zip-export handoff (and can even be embedded in a WebView tab).

Mitigation for the future: stage 1 below introduces an `AppController` service layer between UI and core. If a web UI is ever wanted, that layer becomes the HTTP API surface — nothing from the JavaFX investment blocks it.

### 4.3 Concrete stack
- Java 21 + JavaFX 21 (already configured)
- **AtlantaFX** for theming (modern flat look, proper light/dark switch — kills P9)
- FXML + CSS for views, plain classes for view-models (MVVM-light); `Property`/`ObservableList` bindings for state → no polling
- `javafx.concurrent.Task` for generation/batch jobs → progress bars + cancellation (P10)
- Charts: `javafx.scene.chart.LineChart` for NPS; `Canvas` for the pattern heatmap; drop JFreeChart from the UI path
- Markdown/help: existing WebView approach (map previewer: see §4.4)

### 4.4 ArcViewer integration (decision: yes, embedded as far as technically sensible)

Facts (verified 2026-07-02 against github.com/AllPoland/ArcViewer):
- ArcViewer is a **Unity** app (C#/ShaderLab), shipped as browser version (Unity WebGL, hosted on GitHub Pages) and as **desktop releases** (extract-and-run).
- "Clone the repository and run it embedded" is **not practical**: building from source requires the Unity Editor (6000.0.x) build chain — not something BeatKenja can automate on user machines.
- **JavaFX WebView cannot render it**: WebView (WebKit) has no WebGL support, so the hosted browser version won't run inside the app's own WebView.
- License GPL-3.0: launching it as an external program or pointing a browser at it is unproblematic. *Bundling* its binaries inside the BeatKenja release would drag in GPL obligations → avoid by downloading at runtime.

Strategy, two phases:

**Phase 1 (stage 4): auto-managed desktop ArcViewer — recommended default**
1. `ArcViewerManager` checks `./tools/ArcViewer/`; if missing/outdated, downloads the latest desktop release via GitHub Releases API (`releases/latest`), unzips, remembers version. One-click consent dialog on first use ("Download ArcViewer (~X MB) from GitHub?").
2. [Preview in ArcViewer] exports the current map (zip with info.dat + diffs + song, reusing the existing zip-export code from `GlobalOpenMapInBrowser`) and launches the ArcViewer exe. If CLI/file-association map loading works (to verify during impl), pass the zip path; otherwise ArcViewer opens and the user drags the zip in (BeatKenja opens the folder alongside).
3. Fallback (no download consent / offline): browser handoff to the hosted version, as today.

**Phase 2 (optional, later): true in-app embed via JCEF**
- `jcefmaven` (Java Chromium Embedded Framework) supports WebGL → hosted ArcViewer can render inside a `PreviewerView` tab.
- Map delivery: tiny local HTTP server serving the exported zip + ArcViewer's `?url=http://localhost:…/map.zip` loading. (Mixed-content/CORS behavior must be verified; localhost is usually exempt.)
- Cost: JCEF pulls a ~100–200 MB Chromium runtime at first start. That is why it's phase 2, opt-in, and not the default.

**Alternative previewers considered (verified 2026-07-02):**
| Tool | Tech | Verdict |
|---|---|---|
| skystudioapps bs-viewer | web | already the configurable default (`Parameters.mapViewerURL`); stays as browser-handoff option |
| supermedium/beatsaver-viewer | A-Frame/three.js (WebGL) | self-hostable (`npm start`), supports `?id=`/`?zip=`/`?time=` — but stale, v3/v4 format support unconfirmed → not worth integrating over ArcViewer |
| Beatmapper (bsmg/beatmapper) | TypeScript web **editor**, MIT | editor not previewer; browser-only, no backend. Kept as candidate for the future "integrated editor" idea (§7) — MIT license makes deep integration/fork viable |

Previewer choice stays a config entry (`mapViewerURL`), so any of these remain one settings-change away.

### 4.5 Map-check web tools in the Review step (new)

Two DOM-based (non-WebGL) web tools catch "funkily generated" maps and complement BeatKenja's internal parity checker:
- **BeatSaber-MapCheck** (kivalevan.me/BeatSaber-MapCheck): TypeScript, lightweight, extensive pre-release error/stat overview; self-hostable (`npm run build && npm run serve`).
- **bs-parity** (galaxymaster2.github.io/bs-parity): plain JS/DOM map visualizer + parity algorithm that tracks actual parity, not just cut direction; loads maps from file, URL, or BeatSaver ID; self-hostable (static copy).

Integration plan (stage 4, alongside ReviewView):
1. ReviewView gets an **"External checks"** tab hosting these in a **JavaFX WebView** — feasible because neither needs WebGL (unlike the 3D previewers). Caveat to verify at impl: WebView's JS engine must cope with their modern JS; if not → fall back to browser handoff with the exported map auto-served.
2. Map delivery: same tiny local HTTP server as ArcViewer phase 2 — export zip, open tool with `?url=http://localhost:…/map.zip` (bs-parity supports URL loading; MapCheck URL-param support to verify, else its file picker + auto-opened folder).
3. Both are static sites → optionally vendor a local copy under `./tools/` for offline use (licenses permitting; check at impl).

Result: Review step = internal parity table (instant, inline) + NPS chart + heatmap + one-click external second opinion (MapCheck/bs-parity) + visual previewer (ArcViewer). Covers "did it generate funky?" from four angles without leaving the app.

---

## 5. Implementation stages (for later; NOT part of this task)

1. **Decouple** — extract `AppController` + `MapSession`/`DiffSession` model from `UserInterface` statics; define observable state + events. Remove dead DB code paths (genre/tag dropdown sources). Core logic untouched otherwise. (Enables everything else; also benefits CLI.)
2. **Shell** — JavaFX `AppShell` with sidebar, status bar, log drawer, AtlantaFX theming, settings view reading/writing `config.json`.
3. **Critical path** — Load → per-diff tabs → Timing → Generate (cards + parameter panel + "Apply to all diffs") → Export (batch table, backup-by-default). At this point the old Swing window can be deleted.
4. **Review + Preview** — embedded NPS chart, heatmap, parity table with bookmark export; `ArcViewerManager` (auto-download desktop release + launch, §4.4 phase 1); "External checks" tab with MapCheck + bs-parity in WebView (§4.5).
5. **Secondary tools** — Utilities, Batch MP3 (with progress list), pattern manager.
6. **Polish** — drag & drop, recents, keyboard shortcuts, i18n if wanted.

Each stage ships a runnable app; stages 4–6 are independent of each other.

---

## 6. Decisions log (was: open questions)

| Question | Decision (2026-07-02) |
|---|---|
| Advanced Map Creator DB features | DB removed from project. Drop genre/tag sources; keep standalone advanced params under collapsed "experimental" section. |
| ArcViewer | Integrate. Clone+build impossible (Unity); phase 1 = auto-download desktop release + launch (§4.4); phase 2 optional JCEF in-app embed. |
| Visual section timeline / integrated editor | Later. Design keeps a slot for it (Generate step content area can host a timeline; see §7). Not in current scope. |
| Batch vs. per-diff | Both. Per-diff tabs with individual Generate + "Apply to all diffs" per step; Export stays a batch table over all diffs. |

## 7. Future ideas (explicitly out of scope now)
- **Visual section timeline editor** in Generate: horizontal beat timeline showing bookmarks/sections, drag to place `linear|complex|jumps|doubles…` sections instead of naming bookmarks in an external editor. Fits into GenerateView's content area without layout changes.
- **Integrated light map/bookmark editor** building on the same timeline component. Alternative route: embed/fork **Beatmapper** (MIT, browser-only web editor, bsmg/beatmapper) via JCEF instead of building an editor from scratch.
- **JCEF-embedded ArcViewer** (§4.4 phase 2).