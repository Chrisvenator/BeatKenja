# CLAUDE.md — BeatKenja

Guidance for AI agents (and humans) working on this repository.

## Project Overview

BeatKenja is a **Beat Saber automapping toolset** written in Java 21 (Maven). It accelerates map creation — it is *not* a replacement for human mappers. It analyzes audio, generates timing maps from onsets, and places note patterns via Markov-chain models trained on 98,125 community maps.

- GUI: JavaFX (`UserInterfaceFX/`, entry `StartFX.java` via `Start.java`)
- CLI: `Start_CLI.java` (run `Start.java` with args)
- License: GPL (perpetual)

## Vision & Mapping Philosophy

> "A map cannot be bad when it has good music sync. But what truly makes it good is a consistent quirk throughout the map."

This quote drives everything. The end goal is a **genre-aware automapper** that listens to the actual song (pop, metal, speedcore, classical, …) and adapts. Guiding principles for generated maps:

- **Complex but readable.** Not linear, never unreadable.
- **Predictable but not repetitive.** Players should flow, not memorize.
- **Consistent quirk.** A map should have an identity — a recurring motif/style throughout.
- **Sync is the foundation.** Notes must land on real musical events. Everything else builds on this.

**Priority order (current):**
1. **NOW — Music sync.** Accurate onsets, beats, and tempo for *all* genres (incl. speedcore >200 BPM and ambient). See `docs/research/SYNC_RESEARCH_LOG.md` and `MUSIC_SYNC_PLAN.md`.
2. **NEXT — Sectioning.** Automatic detection of song structure and intensity (calm/build/intense/peak sections) to drive section-aware generation and visualization. Visualization exists to *serve sectioning* — help the user see high/low intensity regions at a glance — not decoration.
3. **LATER — Genre-aware generation.** Use song content (genre, energy, spectral character) to pick patterns. Today generation is timing-only.

## Build / Test / Run

```bash
mvn package          # build (Maven shade plugin produces fat jar)
mvn test             # JUnit 5 tests (audio fixtures are generated synthetically)
```

Note: `mvn` is NOT on PATH on the dev machine — builds run through IntelliJ (bundled Maven). Use the IDE run configs in `.run/`: `Build Project`, `Run all Tests`, `Start`, `Test`. For CLI, use the snap-bundled binary:
`/snap/intellij-idea-ultimate/current/plugins/maven/lib/maven3/bin/mvn`

Benchmark suite (slow, excluded from default runs, needs local `data/ground_truth/` corpus + ffmpeg):
```bash
mvn test -Dtest=BaselineBenchmarkTest -Dsurefire.excludedGroups=none -Dbk.reportLabel=<label>   # from repo root; label names the report file (default "baseline")
```

- Reproducible generation: pass `-Dbk.seed=<long>` (seeds `Parameters.RANDOM`).
- GUI: run `Start.java` with no args. CLI: with args (see `Start_CLI`).
- Future benchmark tests will be tagged `@Tag("benchmark")` and excluded from the default surefire run.

## Architecture Map

| Package | Purpose |
|---|---|
| `AudioAnalysis/` | Pure-Java DSP: radix-2 FFT spectrogram (`SpectrogramCalculator`), SuperFlux onset detection (`SuperFluxOnsetDetector`, wired in `AudioAnalysis`), BPM (`BPMDetector`, comb autocorrelation on the ODF, 50–420 BPM), section boundaries + intensity tiers (`FooteSectionDetector`, checkerboard novelty), timing offset (`TimingOffsetDetector`), MP3 decode (`Mp3ToWavConverter`, jlayer) |
| `MapGeneration/` | Pattern-based note placement: `MapGenerator`, `ComplexPattern`, `CreateMap` (sectioned), `BatchWavToMaps` (onset→timing-map pipeline). Patterns = 109×109 Markov transition matrices (`GenerationElements/Pattern`) with Dirichlet-Multinomial variance control |
| `AppLogic/` | UI-agnostic services: `AppController`, `GenerationService`, `OnsetGenerationService`, `SectionAnalysisService` (Song Map: sections/tiers/onsets/BPM of an audio file → SECTIONED bookmarks), sessions (`AppState`, `MapSession`, `DiffSession`), `GenerationContext` (static holder — legacy; prefer session objects for new per-song state) |
| `UserInterfaceFX/` | JavaFX UI. Workflow: Load → Timing → Generate → Review → Export, plus Tools (Utilities, Batch MP3, Patterns, Settings). `TimingView` hosts the "Song Map" card (section/intensity canvas + audio preview player (`AudioPreviewPlayer`, play/scrub/click-to-seek) + apply-as-bookmarks, asks before replacing manual bookmarks; the canvas sits in a pref-width-0 holder Pane — never bind a Canvas dimension to an ancestor whose pref size it influences, that loop grows the window every layout pass). `ReviewView` tabs: parity table, NPS chart, pattern heatmap, external checks |
| `BeatSaberObjects/` | Beat Saber data structures (Note, Obstacle, Event, Bookmark, …) |
| `DataManager/` | `ConfigLoader`/`Parameters` (config.json), `FileManager`, pattern I/O |
| `MapAnalysation/` | Pattern analysis + visualization (Dirichlet distributions, heatmaps, NPS plotters) |
| `OnsetGeneration/` (repo root) | **Dev-side only** Python scripts (librosa, madmom). End users must NEVER need Python |

Data flow (onset pipeline): `BatchWavToMaps.generateOnsets()` → `BPMDetector` → `TimingOffsetDetector` → `AudioAnalysis.getPeaksFromAudio()` → per-difficulty onset lists → notes → `BeatSaberMap` → `.dat` + `.ogg` + zip.

**Key gap:** after onset extraction, all audio information is discarded. Generation sees only note timings + pattern matrix + manual bookmarks. `PatMetadata` stores genres/tags but they never influence placement.

## Known Weaknesses (as of 2026-07, post-SuperFlux + BPM rework)

- Onset detection (SuperFlux, corpus-tuned): F@50ms .805 vs mapper ground truth. Residual: precision dips on sparse/melodic maps (detector fires on real events mappers skip — see H4 in the research log).
- BPM detection (comb autocorrelation on SuperFlux ODF, range 50–420): Acc1 76.1% / Acc2 91.3%. Residual: mapper octave *convention* undecidable from signal (350 vs 175); variable-BPM songs get a single global tempo; corpus lacks ambient/calm so the tempo prior is EDM-tuned.
- DSP parameters are corpus-tuned constants (`FFT 2048/hop 256`, μ=2, δ ladder, min-gaps, tempo prior 260 BPM/σ1.2) — only the Expert+ tier is benchmark-backed.
- Section detection (`FooteSectionDetector`): boundary F@±3s .592 vs mapper bookmarks — usable, but mapper bookmark granularity varies (macro sections vs phrases) and the detector is fixed at ~16s structure. Wired into TimingView "Song Map" (analyze → visualize → apply-as-bookmarks with ask-before-overwrite); tier→pattern-flag mapping is an untuned heuristic. No pure-Java vorbis: .egg/.ogg analysis needs ffmpeg on PATH.
- Old Swing `SpectrogramDisplay` is a debug-grade grayscale view (slated for deprecation).

## Roadmap & Hard Constraints

Roadmap: sync research → Java evaluation harness + benchmark baseline → SuperFlux/HPSS DSP upgrade → automatic sectioning + auto-bookmarks → "Song Map" visualization panel (TimingView) → ONNX-based beat tracking *only if measurements demand it* → genre-aware generation.

Hard constraints:
- **End users never need Python.** Windows gamers double-click a jar. Python = dev-side research harness only.
- **GPL compatibility.** CC BY-NC-SA model weights (e.g. madmom pretrained) are likely incompatible with GPL distribution — prefer permissively licensed models. Check code AND weight licenses separately.
- Jar size is not a concern (+60–120MB OK); bundle ONNX models in the jar for now, model-downloader later.
- Auto-generated section bookmarks must **ask the user** before touching manual bookmarks.

## Conventions for Agents

- **Research log discipline:** `docs/research/SYNC_RESEARCH_LOG.md` — update the STATUS SNAPSHOT and RESUMPTION CHECKLIST as the *last action of every session*. Usage limits may cut sessions short; the log is the resume point. Record negative results ("what does not work") as first-class findings. Every verdict cites a paper/benchmark number or a locally measured number.
- **Benchmark before/after** any change to sync-related code (onset, BPM, offset detection).
- Match existing code style; Javadoc on public methods is common in this repo.
