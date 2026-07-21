# Plan: Vision Docs + Music Sync Research (BeatKenja)

## Context

BeatKenja generates Beat Saber maps, but generation is timing-only: audio is analyzed once for onsets, then all spectral/energy/genre info is discarded. Onset detection has known failures (false positives, hi-hat bias, no speedcore >200 BPM, weak on ambient). User's goal: **first make maps truly synced to the music**, then (later) genre-aware, section-aware generation. Guiding quote: *"A map cannot be bad when it has good music sync. But what truly makes it good is a consistent quirk throughout the map."* Maps should be complex but readable, predictable but not repetitive.

This session = **documentation + research only, no production code**:
1. Create `CLAUDE.md` (doesn't exist) capturing vision/philosophy/architecture.
2. Create persistent research log (usage limits may interrupt — log is the resume point).
3. Conduct deep MIR research (web), record verdicts w/ citations.

## User decisions (2026-07-05)
- **License**: GPL (perpetual). ⚠️ CC BY-NC-SA model weights (madmom pretrained) likely incompatible with GPL distribution → prefer permissively licensed models (Beat This! etc.); record as license risk.
- **Benchmark corpus**: user will provide 10–30 ranked maps + audio locally (never committed).
- **Auto-section bookmarks vs manual**: UI asks each time on conflict.
- **Jar size**: +60–120MB OK. Bundle ONNX models in jar for now; model-downloader later.
- Hard constraint: **end users never need Python** (Windows gamers). Python = dev-side research harness only.

## Verified current state (exploration summary)

### Audio pipeline (`src/main/java/AudioAnalysis/`)
`BatchWavToMaps.generateOnsets()` → `BPMDetector` → `TimingOffsetDetector` → `AudioAnalysis.getPeaksFromAudio()` → 5 per-diff onset lists → beats → BeatSaberMap → .dat/ogg/zip.
- SpectrogramCalculator: pure-Java radix-2 FFT (1024, hop 256 ≈5.8ms, Hann, 44.1k mono). Fixed in 8a3fd2c post-TarsosDSP removal.
- Onsets: spectral flux (>1kHz ×2 weight) → half-wave ODF → adaptive threshold (mean+1.5σ ±1s) → percentile gates [90/85/75/65/55] + tempo-scaled min-gaps [0.150…0.065s].
- BPMDetector: autocorr 40% + interval hist 35% + bass spectral 25%; range 60–200; snap-to-common. TimingOffsetDetector: cross-corr/phase/grid-fit ±500ms.
- Python scripts exist, disabled: `OnsetGeneration/SongToOnsets.py` (librosa), `madmom_onset_generation.py` (partial superflux). `BatchWavToMaps.executePythonScript` (~line 261) commented out; `OnsetGenerationService` auto-pip-installs (scary; keep dev-only).

### Generation (`src/main/java/MapGeneration/`)
- Markov 109×109 transition matrices (from 98,125 maps), Dirichlet-Multinomial variance −50..+50. Generators: LINEAR/COMPLEX/SECTIONED/RANDOM/RANDOM_V2.
- **Inputs = note timings only** + pattern matrix + seed + local NPS (>8.0 → easyPattern) + manual bookmarks ("complex"/"linear"/"1-2"/jumps…). `PatMetadata` genres/tags stored, unused in placement. Sections = manual bookmarks only; no auto structure detection.
- `GenerationContext` = static holder (legacy per own javadoc); per-song analysis belongs on `DiffSession`/`AppState` (`AppLogic/`).

### UI (`src/main/java/UserInterfaceFX/`)
Load → Timing → Generate → Review → Export + Tools. `TimingView` = 2 conversion cards, lots of free space → natural home for song viz. `ReviewView` tabs: parity, NPS LineChart (`DynamicNpsPlotter`, JFreeChart), pattern heatmap, external checks. Old Swing `SpectrogramDisplay` → deprecate later.

### Misc
No CLAUDE.md, no ML deps, no research log. JUnit5 + synthetic WAV fixtures (`SpectrogramCalculatorTest`). Test maps: `src/test/resources/` (ISeeFire.txt, BookmarksExample.txt — bookmark serialization format exists). Maven shade plugin in play. README "Future Ideas" (lines 359–376): DL models, dynamic probability, difficulty scaling.

## Approach

### Step 1 — CLAUDE.md (new file, repo root)
Sections:
1. **Project overview** — Java 21 Maven Beat Saber automapper; JavaFX UI; entries `Start.java`/`Start_CLI.java`.
2. **Vision & mapping philosophy** — genre-aware automapper end goal; quote verbatim; complex-but-readable / predictable-not-repetitive / not linear; **priority: music sync NOW, generation quality LATER**; visualization serves sectioning (high/low intensity), not decoration.
3. **Build/test/run** — `mvn package`, `mvn test`, seed via `-Dbk.seed`; shade plugin; future `@Tag("benchmark")` tests excluded from default run.
4. **Architecture map** — one line per package (AudioAnalysis, MapGeneration, AppLogic, UserInterfaceFX, BeatSaberObjects, DataManager, MapAnalysation); Python scripts = dev-only.
5. **Known weaknesses** — onset false positives, hi-hat bias, speedcore/ambient failure, hardcoded params, audio discarded after onsets, genres unused.
6. **Roadmap** — sync research (link log) → sectioning + viz → ONNX if data demands → genre-aware generation. Constraints: no user-facing Python; GPL; models bundled in jar.
7. **Agent conventions** — update research-log STATUS SNAPSHOT before ending any session; benchmark before/after sync changes.

### Step 2 — Research log: `docs/research/SYNC_RESEARCH_LOG.md` (new dir)
Single file (interruption-resilient). Structure:
```
0. STATUS SNAPSHOT   (edit-in-place: last session, in-progress, exact next action, current leaning)
1. RESUMPTION CHECKLIST (ordered, cold-startable steps)
2. RESEARCH QUESTIONS (status: OPEN/ANSWERED/PARKED)
3. HYPOTHESES (H1..Hn: statement, falsification, confidence)
4. APPROACH EVALUATIONS (per candidate: sources, accuracy, deploy cost, license, VERDICT: PROMISING/REJECTED/NEEDS-PROTOTYPE)
5. DECISION LOG (dated, append-only)
6. BENCHMARK PROTOCOL
7. REFERENCES (papers, TU Wien MIR course links)
8. SESSION NOTES (dated, append-only)
```
Rules: every VERDICT cites paper/benchmark number or local measurement. Negative results first-class. Sections 0–1 updated as last action of every session.

Seed hypotheses:
- H1: SuperFlux alone closes most of the onset gap (lit: SuperFlux F≈0.87–0.88 vs CNN ≈0.90).
- H2: Beat tracking (not onsets) is the speedcore/ambient failure.
- H3: Foote checkerboard novelty suffices for intensity sectioning.
- H4: Ranked-map note times usable as ground truth w/ recall-weighted metrics (mappers undermap).

Research questions (ordered): RQ1 pure-Java DSP ceiling; RQ2 beat trackers for edge genres + ONNX-exportable permissive weights (Beat This! vs madmom DBN vs BeatNet; DBN post-proc needs Java port); RQ3 Foote vs allin1-class segmentation (NATTEN ops likely not ONNX-exportable — verify); RQ4 what ranked maps actually track; RQ5 model weight licenses vs GPL; RQ6 ONNX Runtime Java packaging (`com.microsoft.onnxruntime:onnxruntime`, natives, shade interactions); RQ7 (PARKED) genre embeddings for future generation.

### Step 3 — Conduct research (bulk of session)
WebSearch/WebFetch on: SuperFlux (Böck & Widmer 2013), madmom CNN onsets (Schlüter & Böck 2014), madmom DBN beat tracking, Beat This! (Foscarin 2024), BeatNet, All-in-One (Kim 2023)/allin1, Foote novelty, McFee–Ellis Laplacian segmentation, MSAF, TU Wien MIR course (Knees/Schindler) as syllabus lead. Per model: F-measure on standard datasets, ONNX exportability, code+weights license separately, CPU runtime. Record all in log w/ VERDICTs; update STATUS SNAPSHOT after each RQ.

### Recommended technical direction (recorded in log, executed in later sessions)
**Staged hybrid.** Stage 1: pure-Java DSP upgrade — SuperFlux port (~200 lines on SpectrogramCalculator: log filterbank + freq maximum filter), median-filter HPSS to kill hi-hat bias, better peak picking, de-hardcode params. Stage 2: ONNX Runtime Java only where Stage 1 measurably fails (expected: beat tracking speedcore/ambient; maybe segmentation). Python sidecar = dev benchmark harness only.
Sectioning: beat-synced self-similarity matrix → Foote checkerboard novelty → min-length-constrained peaks → intensity tiers (RMS + onset density percentiles: calm/build/normal/intense/peak) → auto-bookmarks in existing format (SECTIONED generator consumes unchanged; conflict → ask user).
Viz: "Song Map" JavaFX Canvas panel in TimingView (layers: RMS area, novelty line, tier-colored section bands, onset ticks, optional NPS polyline reusing DynamicNpsPlotter math; spectrogram = off-by-default debug layer).

### Future sessions (each ends w/ log resumable)
- S2: Java F-measure evaluator (±50ms), corpus loader (config-pointed local folder, e.g. `OnsetGeneration/mp3Files/`), baseline current pipeline; optionally resurrect Python scripts as reference generators.
- S3: SuperFlux + HPSS + peak-picking rework; measure; verdict H1/H2.
- S4: sectioning prototype + auto-bookmarks; boundary hit-rate ±3s vs hand bookmarks.
- S5: Song Map viz panel.
- S6+: ONNX beat tracker (if data demands); later genre-aware generation.

## Verification (this session)
- CLAUDE.md exists, accurate (build commands actually work: `mvn -q compile` sanity).
- `docs/research/SYNC_RESEARCH_LOG.md` exists w/ all sections, seeded RQs/hypotheses, ≥ RQ1–RQ6 researched w/ cited verdicts, STATUS SNAPSHOT + RESUMPTION CHECKLIST current.
- No production code touched.

## Benchmark protocol (recorded in log for later sessions)
Ground truth = ranked-map note times (dedupe chords→one event). Report P/R separately; primary F-measure ±50ms, secondary ±25ms. BPM: Accuracy1/Accuracy2 (half/double tolerated). Sectioning: boundary hit-rate ±3s (±0.5s stretch). Harness: JUnit `@Tag("benchmark")`, excluded by default; results appended to log per session.

## Risks
- Model weight licenses vs GPL (madmom NC weights likely unusable) → resolve RQ5 before ONNX work.
- ONNX real cost = Java preprocessing parity (mel-spec must match librosa/madmom), not inference.
- Ground-truth bias: tuning to ranked maps = "what mappers map" (desired, but genre-dependent → corpus diversity).
- Shade plugin + ONNX natives packaging surprises → test shaded jar early if adopted.
- Usage-limit interruption → STATUS SNAPSHOT discipline is mandatory last action.

## Critical files
- `CLAUDE.md` (new)
- `docs/research/SYNC_RESEARCH_LOG.md` (new)
- Reference (read-only this session): `src/main/java/AudioAnalysis/AudioAnalysis.java`, `SpectrogramCalculator.java`, `src/main/java/AppLogic/GenerationContext.java`, `src/main/java/UserInterfaceFX/Views/TimingView.java`, `pom.xml`
