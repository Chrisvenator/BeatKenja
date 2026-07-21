# Music Sync Research Log

Rules: Sections 0–1 edited in place at end of EVERY session (resume point). Sections 5 & 8 append-only, dated. Every VERDICT must cite a paper/benchmark number or a locally measured number — no vibes. Negative results are first-class.

---

## 0. STATUS SNAPSHOT

- **Last session:** 2026-07-05 (session 6 + follow-up) — COMPLETE. **Song Map UI shipped: TimingView card with section/intensity visualization + apply-as-bookmarks (SECTIONED-generator flags). Follow-up from first user test: fixed Timing-tab layout drift (canvas↔card width feedback loop; canvas now lives in a pref-width-0 holder Pane) and added audio preview playback (`AppLogic/AudioPreviewPlayer`, javax.sound Clip on the analysis wav: play/pause, scrub slider, click-to-seek on the canvas, playhead line).**
- **In progress:** user re-tests the Song Map UI (run `Start.java` → load map → 2·Timing → "Analyze audio…" → play/scrub → "Apply as bookmarks"). Working tree has uncommitted session-3/4/5/6 changes; user decides commit.
- **Current numbers (memorize):** Onsets n=38: **P@50 .777 / R@50 .852 / F@50 .805**. BPM n=46: **Acc1 76.1% / Acc2 91.3%** (`bpm_rework_2026-07-05.md`). Sectioning n=14: **boundary F@±3s .592** at kernel 16s/δ0.05/minSection 6s (`sweep_sectioning_2026-07-05.md`). Full suite 737 green. Benchmark rerun: `mvn test -Dtest=BaselineBenchmarkTest -Dsurefire.excludedGroups=none -Dbk.reportLabel=<label>` (needs `data/ground_truth/` + ffmpeg). Sweeps: `SuperFluxSweepTest`, `BPMSweepTest`, `SectioningSweepTest`; UI smoke: `SectionAnalysisSmokeIT`.
- **Exact next action (Session 7, pick by user feedback):** (a) boundary placement per user feedback ("a bit too late/early"): snap boundaries to the strongest onset/beat within ±1s, tune tier→flag mapping / granularity, maybe expose kernel size in Settings; (b) corpus style categorization (H5, BeatSaver tags or AI) → style-weighted benchmarks + per-style generation flags; (c) evaluator v2: bombs/obstacle-edges as ground-truth events (H4); (d) beat-grid timing tool + variable-BPM tracking (user-requested LATER feature; would also fix the 3 varBPM Acc2 artifacts); (e) S2b madmom CNN ceiling measurement. No ONNX work justified by current numbers.
- **Current position:** Stage-1 sync pipeline complete AND user-visible: onsets F .805 (S3), tempo Acc1 76.1%/Acc2 91.3% (S4), sections F .592 (S5), Song Map UI + auto-bookmarks wired to the SECTIONED generator (S6) — all pure Java, zero new deps, every DSP layer benchmark-backed. Known residuals: mapper octave convention (BPM), bookmark granularity (sections, fixed ~16s), melodic-map precision dips (onsets), no pure-Java vorbis (.egg needs ffmpeg). License deferred. Corpus lacks ambient/calm — all tuning EDM-shaped; ask user for calmer maps.

## 1. RESUMPTION CHECKLIST

- [x] Create CLAUDE.md (vision, architecture, constraints) — done 2026-07-05
- [x] Seed this log with RQs, hypotheses, benchmark protocol — done 2026-07-05
- [x] Research RQ1–RQ6, fill §4 with cited verdicts — done 2026-07-05 (RQ4/RQ6 partial, need local measurement)
- [x] Session 2: Java harness (`src/test/java/Benchmark/`) + corpus (`data/ground_truth/`, 46 maps) + baseline measured — done 2026-07-05
- [ ] Session 2b (optional): resurrect Python scripts (`OnsetGeneration/*.py`) as reference-output generators (madmom CNN ceiling measurement)
- [ ] Get MORE corpus maps from user if needed: ranked >2024, curated; current corpus lacks ambient/calm tracks (mostly EDM 200+ BPM)
- [x] Session 3: SuperFlux port + peak-picking rework — done 2026-07-05: **F@50 .670 → .805** (P .777 / R .852). HPSS not needed (max-filter killed the vibrato/hi-hat problem). Melodic-band novelty not needed for F (log filterbank already covers 30Hz–17kHz).
- [x] Session 4: BPM detector rework — done 2026-07-05: **Acc1 15.2% → 76.1%, Acc2 69.6% → 91.3%** (comb autocorr on SuperFlux ODF, range 50–420, prior 260/σ1.2). Old Acc2 folds (300→180, 200→90) gone. Remaining: 7 octave-convention misses (Acc2 ✓), 4 Acc2 fails of which 3 are varBPM-metadata eval artifacts.
- [ ] Evaluator v2 (H4): optional flag to include bombs + obstacle starts as ground-truth events; per-map style tag in manifest (fitbeat/challenge/dance/tech) for style-weighted metrics (H5)
- [ ] Categorize corpus map styles (info.dat has no genre → use BeatSaver API tags per map id, or AI classification) — feeds H5 + future style-aware generation
- [x] Investigate worst onset map 2f951 "Hello (BPM) 2023" — **RESOLVED 2026-07-05 (user):** song's real BPM is 2023 — unplayable, so the mapper maps only every X-th beat (map metadata 252.9 = 2023/8). Ground truth = sparse beat-grid subsample of an ultra-fast pulse, not audio onsets → R .521 is H5 style noise, not a detector failure. No action; down-weight once style tags exist.
- [ ] LATER (user request 2026-07-05): **beat-grid timing tool** — detect tempo (incl. BPM changes) and place notes on every beat / every X-th beat instead of onsets. Handles gimmick tempos (2f951, BPM 2023) and gives mappers a metronome-style alternative to onset maps. Depends on S4 tempo estimation + variable-BPM tracking (beat tracking, S7 candidate).
- [x] Session 5: Foote novelty sectioning prototype — done 2026-07-05: **boundary F@±3s .592 (P .624 / R .630)** vs curated mapper bookmarks (14 maps), kernel 16s / minSection 6s / threshold 0.05. H3 supported at the ~60% bar. Key fix: z-score bands before cosine (raw log-spec SSM ≈ .97 everywhere → first sweep F .146).
- [x] Session 6: Song Map UI + auto-bookmarks — done 2026-07-05. `SectionAnalysisService` (AppLogic) + Song Map card in TimingView: "Analyze audio…" (wav/mp3, ogg/egg via ffmpeg fallback) → Canvas (tier-colored section bands, novelty curve, onset ticks, boundary lines) → "Apply as bookmarks to active diff" with **confirmation dialog when manual bookmarks exist** (hard constraint honored). Tier→flag mapping feeds SECTIONED generator directly: linear/1-2/complex/complex/normal_jumps. Smoke on Oyasumi: BPM 200.0 exact, 9 sections, plausible intensity arc. Suite 737 green. **Awaiting user hands-on test.**
- [ ] Session 7+ (conditional): ONNX beat tracker — only if S4 DSP rework still fails speedcore BPM

## 2. RESEARCH QUESTIONS

| # | Question | Status |
|---|---|---|
| RQ1 | How far can pure-Java DSP go? Is the SuperFlux→CNN onset gap small enough to skip ML for onsets? | **ANSWERED (lit)** — gap genre-dependent: IDMT 0.884 (SF) vs 0.874 (CNN), MusicNet 0.855 vs 0.909. SF sufficient for percussive genres; local benchmark decides (H1). See §4.1b. |
| RQ2 | Which beat tracker fixes speedcore/ambient with permissive, ONNX-exportable weights? | **ANSWERED** — Beat This! by elimination: MIT (code+weights), ONNX export proven (beat_this_cpp), no DBN → no tempo-prior octave-error mechanism, no Java DBN port. >200 BPM behavior unpublished → benchmark locally. See §4.2c. |
| RQ3 | Foote novelty vs allin1-class segmentation? | **ANSWERED** — Foote for Stage 1 (intensity boundaries, zero deps; homologous-section weakness irrelevant for intensity tiers). allin1 rejected: NATTEN unexportable, dep-broken, madmom taint. See §4.3. |
| RQ4 | What do ranked maps actually track — onsets, beats, "mappable events"? | PARTIAL — Osu2MIR (ISMIR 2025) validates rhythm-game maps as beat ground truth + gives curation heuristics. Local measurement on ~5 maps still needed (Session 2). See §4.5. |
| RQ5 | Model weight licenses vs GPL. | **ANSWERED** — madmom weights CC BY-NC-SA (unshippable under GPL; commercial exception = contact Widmer). Beat This! MIT ✓. See §4.1c/4.2c. |
| RQ6 | ONNX Runtime Java packaging. | PARTIAL — Maven Central v1.26.0, natives bundled (Win/Linux/mac). Shaded-jar size + shade-plugin interaction: measure empirically in Session 6 spike. See §4.4c. |
| RQ7 | Genre embeddings (PANN/musicnn ONNX) for genre-aware generation. | PARKED (future phase) |

## 3. HYPOTHESES

- **H1:** SuperFlux alone closes most of the onset-quality gap vs CNN detectors. *Falsify:* benchmark SuperFlux port vs current pipeline vs madmom CNN reference on corpus; if CNN > SuperFlux by >0.05 F-measure, H1 dead. Confidence: medium-high (lit numbers close).
- **H2:** Beat tracking (not onset detection) is the real speedcore/ambient failure — current BPMDetector hard-cap 60–200 and weak-onset autocorr are the bottleneck. *Falsify:* fix onsets first, re-test BPM accuracy on speedcore/ambient tracks. Confidence: high (cap is provable from code).
- **H3:** Foote checkerboard novelty on beat-synced features suffices for intensity sectioning (±3s boundary tolerance). *Falsify:* boundary hit-rate vs hand-placed bookmarks < ~60%. **SUPPORTED (2026-07-05, S5): R .630 / P .624 / F .592 @±3s** vs curated mapper bookmarks (14 maps) — at the bar, and the residual error is largely bookmark-granularity mismatch (mappers mark 3 macro sections or 40 phrases; detector sits at ~16s structure), not missed transitions. Required per-band z-scoring of features; raw log-spec cosine fails (F .146).
- **H4 (revised 2026-07-05, user domain input + baseline data):** Ranked-map note times ≈ "mappable events" — a SUPERSET of percussive onsets. Mappers track: (a) percussive onsets, (b) **melody lines**, (c) **lyrics/vocals**, (d) deliberate in-between notes in challenge maps (cramming — musically unanchored), (e) sometimes **bombs/obstacles placed on onsets** (excluded from our ground truth → we may be dropping real onset markers). Baseline confirms: P .92 / R .54 → detector finds a *subset* (percussion) of what mappers map. Implications: (1) recall against note-times measures "mappable-event coverage", not onset correctness; (2) ODF needs melodic/vocal sensitivity (log-filterbank SuperFlux helps; consider separate melodic novelty band), (3) challenge-map cramming = ground-truth noise → per-map style weighting or corpus curation; (4) consider counting bombs/obstacle-edges as ground-truth events (flag-controlled) in evaluator v2. Confidence in usability: still medium-high with these caveats.
- **H5 (new, user input):** Map style determines ground-truth semantics. Fitbeat maps (e.g. corpus 1a32a "Oyasumi") map **body movement** to the song — note positions encode choreography, not just timing; challenge maps encode density goals. → Style categorization (per-map: dance/fitbeat/tech/challenge/vibro…) is needed BEFORE style-aware generation AND for weighting benchmark maps. *Test:* categorize corpus (info.dat/BeatSaver tags or AI), check per-style P/R spread. Confidence: high (qualitative).

## 4. APPROACH EVALUATIONS

Template per candidate: description / sources / accuracy evidence / deploy cost / license / VERDICT (PROMISING | REJECTED | NEEDS-PROTOTYPE) + reason.

### 4.1 Onset detection

#### (a) Old pipeline (baseline, REPLACED 2026-07-05)
Spectral flux, >1kHz ×2 weight, half-wave ODF, adaptive threshold mean+1.5σ (±1s), percentile gates [90/85/75/65/55], tempo-scaled min-gaps. Measured: **P .916 / R .536 / F .670** — README's "many false positives" was wrong vs mapper ground truth; the percentile gates strangled recall instead. VERDICT: SUPERSEDED by (b); code deleted (git history pre-session-3 has it).

#### (b) SuperFlux (Böck & Widmer, DAFx 2013) — pure-Java port ⭐ SHIPPED
- Spectral flux + log filterbank + **maximum filter along frequency** (vibrato/tremolo suppression) → up to 60% fewer false positives vs plain flux without missing events ([paper](https://www.dafx.de/paper-archive/2013/papers/09.dafx2013_submission_12.pdf), [reference impl](https://github.com/CPJKU/SuperFlux), BSD).
- Measured F1 vs CNN across datasets (2023 string-instrument study, ACM): IDMT SuperFlux **0.884** vs CNN 0.874; GuitarSet 0.916 vs 0.930; MusicNet 0.855 vs 0.909. → Gap is genre-dependent: negligible/reversed on percussive, ~0.05 on dense tonal.
- **Ported 2026-07-05** (`AudioAnalysis/SuperFluxOnsetDetector.java`, ~230 lines on existing `SpectrogramCalculator`): FFT 2048 / hop 256, quarter-tone triangular filterbank 30Hz–17kHz (bin-collision dedupe à la madmom `unique_filters`), log10(1+x), max-filter width 3 bands, μ frames back; peak picking = local max ±30ms + local mean (−100ms/+70ms) + δ + min-gap (paper windows kept, μ/δ/shift corpus-tuned).
- **Local measurement (corpus, n=38, Expert+): P .777 / R .852 / F@50 .805, F@25 mean ≈ .77** vs baseline F .670. Sweep (96 combos): best μ=2, δ=0.60×positive-mean-ODF, times at window center (+23.2ms); F surface flat across μ 1–4 at δ 0.6 → robust optimum, not knife-edge. `benchmark_results/sweep_superflux_2026-07-05.md`.
- **VERDICT: CONFIRMED — Stage-1 primary, shipped.** H1 supported locally (+.135 F over old pipeline in pure Java); CNN-vs-SuperFlux gap on OUR ground truth still unmeasured (needs Session 2b madmom reference).

#### (c) CNN onset detector (Schlüter & Böck, ICASSP 2014; madmom `CNNOnsetProcessor`)
- Best onset F-measure in comparative studies, esp. dense tonal music (MusicNet 0.909 vs SuperFlux 0.855).
- **BLOCKER: madmom model weights are CC BY-NC-SA 4.0** ([madmom_models](https://github.com/CPJKU/madmom_models)) — code is BSD, weights are not. NC + ShareAlike incompatible with GPL redistribution; commercial exception requires contacting Gerhard Widmer.
- **VERDICT: REJECTED for shipping** (license). PROMISING as dev-side reference generator (Python harness) to quantify the ceiling.

### 4.2 Beat tracking / tempo

#### (a) Old BPMDetector (baseline, REPLACED 2026-07-05)
Autocorr 40% + interval histogram 35% + bass spectral 25%; range 60–200; snap-to-common. Measured: **Acc1 15.2% / Acc2 69.6%** — the cap folded everything >200 into wrong metrical levels (350→175.2 at best, 300→180=3/5 at worst). VERDICT: SUPERSEDED by (e); code deleted (git history pre-session-4 has it).

#### (e) Harmonic-comb autocorrelation on SuperFlux ODF + log-normal tempo prior ⭐ SHIPPED
- Rework 2026-07-05 (`BPMDetector`, same public API): SuperFlux ODF (μ=2, FFT 2048/hop 256, identical to onset path) → zero-meaned, per-lag-normalized, rectified autocorrelation → every candidate on a log-spaced 50–420 BPM grid (~0.5% steps) scored `comb × prior`; comb = Σ_{k=1..4} AC(k·period)/k (kills non-octave folds — a 3/5 level matches one lag, not its multiples); prior = log-normal over mapper tempos; parabolic refinement in log-tempo.
- Sweep (36 combos, `BPMSweepTest`, AC cached per map): center 260/σ1.2 and center 280/σ{0.65,0.8,1.2} tie at **Acc1 35/46 (76.1%), Acc2 42/46 (91.3%)**. Chose 260/1.2 = broadest tied winner (weakest prior → most comb-driven, least corpus-locked). Full grid: `benchmark_results/sweep_bpm_2026-07-05.md`.
- **Local measurement (full harness): Acc1 15.2% → 76.1%, Acc2 69.6% → 91.3%** (`bpm_rework_2026-07-05.md`). Speedcore direct hits: SAtAN 300→299.9, KOKUSHIMUSOU 340→340.0, Party People 350→349.9, Glucagon 274→273.9. 2f951 gimmick map: 252.7 vs 252.9 metadata (finds the 2023/8 mapping grid).
- Remaining 11 Acc1 misses: 7 are pure octave picks (Acc2 ✓: 350→175 ×2, 388→194, 306→153, 132→264, 142→284, 105→210) = mapper-octave-convention ambiguity, not pulse errors; 4 Acc2 fails (1a15 260→298.5; Last Wish 210→240.2 ×2; Ashed Wings 240→322.4) — 3 of 4 are **variable-BPM maps scored against initial metadata BPM** (eval artifact, dominant audio tempo may legitimately differ).
- **VERDICT: CONFIRMED — H2 fix shipped.** Speedcore BPM failure was the 60–200 cap, as hypothesized. ONNX beat tracking (S7) now only justified by variable-BPM tracking or the octave-convention gap, not by raw tempo accuracy.

#### (b) madmom RNN + DBN (`DBNBeatTrackingProcessor`)
- Long-time SOTA. Two blockers: (1) weights CC BY-NC-SA (same as 4.1c); (2) DBN post-processing is not a NN → would need full Java port even with ONNX inference. Documented failure mode: **DBN tempo priors cause octave errors** — default min-tempo 55 BPM forces double-tempo predictions on 21% of SMC tracks; same prior mechanism is the expected speedcore failure (mirror of our hardcoded 60–200 cap).
- **VERDICT: REJECTED for shipping** (license + DBN port cost). Dev-side reference only.

#### (c) Beat This! (Foscarin et al., ISMIR 2024) ⭐
- "Accurate Beat Tracking Without DBN Postprocessing" — transformer, **best published F1 for beat + downbeat** ([repo](https://github.com/CPJKU/beat_this)). **MIT license, code AND weights.** No DBN → no tempo-prior octave-error mechanism, and no Java DBN port needed (postproc = simple peak picking).
- **ONNX export already proven**: [beat_this_cpp](https://github.com/mosynthkey/beat_this_cpp) ships pre-converted `onnx/beat_this.onnx`, opset 14 (scaled_dot_product_attention), chunked inference (1500 frames, 6-frame border) via ONNX Runtime. Also a [Rust crate](https://lib.rs/crates/beat-this). CoreML export fails (rotary-attention einsum) but ONNX path works.
- Input = mel spectrogram → Java preprocessing parity is the remaining engineering cost (regression-test against Python reference outputs).
- Open sub-question: measured behavior >200 BPM (speedcore) unpublished; half-tempo octave result may even be acceptable for mapping (map at half-time). Benchmark on our corpus.
- **VERDICT: PROMISING — Stage-2 primary.** License clean, export proven, no DBN. This resolves RQ2's "which tracker" almost by elimination.

#### (d) BeatNet (real-time, particle filtering)
- Online/real-time focus ([repo](https://github.com/mjhydri/BeatNet)); we do offline analysis — no latency constraint. Particle-filter postproc = another non-NN Java port. Accuracy below Beat This! per Beat This! paper.
- **VERDICT: REJECTED** (dominated by Beat This! for our use case).

### 4.3 Structure segmentation / intensity

#### (a) Foote checkerboard novelty (pure Java) ⭐ SHIPPED
- Canonical since 2000 ([Foote, ICME 2000](https://ccrma.stanford.edu/workshops/mir2009/references/Foote_00.pdf)); convolve checkerboard kernel along SSM diagonal → novelty peaks at block transitions. Reference impl + theory: [FMP notebooks C4S4](https://www.audiolabs-erlangen.de/resources/MIR/FMP/C4/C4S4_NoveltySegmentation.html) (AudioLabs Erlangen — excellent porting guide).
- Known weakness: cannot distinguish homologous high-similarity sections (verse1 vs verse2 boundary blur). For **intensity sectioning** (our use case) this barely matters — we care about energy-tier changes, not verse/chorus labels.
- **Ported 2026-07-05** (`AudioAnalysis/FooteSectionDetector.java`, zero deps, sits on the SuperFlux filterbank spectrogram): 0.5s coarse frames → **per-band z-score** (crucial: raw log-spec cosine ≈ .97 everywhere on full-band EDM → novelty collapses, first sweep F .146) → unit norm → cosine SSM ∈ [−1,1] → Gaussian-tapered checkerboard, normalized by kernel mass (absolute scale; flat song ⇒ ~0, no per-song max scaling) → local-max picking. Plus heuristic 5-tier intensity rating per section (loudness + onset-density rank within song; no ground truth by design).
- **Local measurement (14 curated-bookmark maps): boundary F@±3s .592 (P .624 / R .630)** at kernel 16s / minSection 6s / threshold 0.05; surface flat (16–24s kernel, δ 0.01–0.05 all ≥ .57). Best maps: p.h. .875, Tanz mit mir .846, Oyasumi .824. Residual error is mostly reference-granularity mismatch (3 macro bookmarks vs 40 phrase marks). `benchmark_results/sweep_sectioning_2026-07-05.md`.
- Ground-truth curation (Osu2MIR-style): bookmarks usable only if ≥3 and median gap ≥5s — mapper bookmarks are variously section markers, **lyrics** (SAtAN: word per beat), or choreography cues; 14/28 bookmark-bearing maps survived (constant-BPM only).
- **VERDICT: CONFIRMED — Stage-1 sectioning shipped.** H3 supported at the ~60% bar.

#### (b) Laplacian/spectral clustering segmentation (McFee & Ellis, ISMIR 2014)
- Better section *grouping* (labels homologous sections); more code (eigendecomposition — commons-math3 could do it). Adds value when we later want verse/chorus *labeling*, not just boundaries+intensity.
- **VERDICT: PARKED** — revisit if Foote boundaries suffice but labeling becomes a need.

#### (c) All-in-One (Kim & Nam 2023, `allin1`) — beats+downbeats+segments+labels in one model
- Functionally ideal on paper (labels: intro/verse/chorus/bridge…, 100 FPS). Reality: **dependency-broken** (NATTEN 0.20+ API breakage; madmom Python 3.10+ incompat per [PyPI ecosystem reports](https://libraries.io/pypi/all-in-one-fix)), requires **demucs source separation** (heavy), NATTEN custom ops → no known ONNX path, and depends on madmom (NC weights) in pipeline.
- **VERDICT: REJECTED for shipping** (unexportable + fragile + license taint). Possibly dev-side reference if it can be made to run at all.

#### (d) MSAF (Music Structure Analysis Framework)
- Python benchmark framework bundling Foote/Laplacian/etc ([repo](https://github.com/urinieto/msaf)). Not a shippable component.
- **VERDICT: dev-side reference harness only** — useful to sanity-check our Java Foote port and compare boundary algorithms on the corpus.

### 4.4 Deployment options

#### (a) Pure-Java DSP
Zero new deps, testable with existing JUnit synthetic-WAV fixtures. Ceiling: no learned models.

#### (b) Python sidecar
Plumbing exists (`BatchWavToMaps.executePythonScript`, commented out; `OnsetGenerationService` auto-pip-install). VERDICT: **REJECTED for user path** (hard constraint: users never need Python). PROMISING as dev-side reference generator.

#### (c) ONNX Runtime Java
- `com.microsoft.onnxruntime:onnxruntime` (v1.26.0, Maven Central) bundles natives for Windows/Linux/macOS x64+arm64 ([docs](https://onnxruntime.ai/docs/get-started/with-java.html)). Alternative lighter binding: [yuzawa-san/onnxruntime-java](https://github.com/yuzawa-san/onnxruntime-java).
- Real cost = preprocessing parity (Java mel-spec must byte-match the model's expected feature pipeline) — mitigate with regression tests against Python reference outputs on fixed WAVs.
- Exact shaded-jar size delta unmeasured → measure in Session 6 spike (user OK'd +60–120MB; models bundled in jar for now, downloader later).
- **VERDICT: PROMISING — Stage-2 vehicle** for Beat This!. RQ6 partially answered (packaging works; size + shade interaction to verify empirically).

### 4.5 Ground truth / benchmark corpora

#### Rhythm-game maps as beat annotations — academically validated
- **Osu2MIR** (Liu, ISMIR 2025 LBD, [arXiv:2509.12667](https://arxiv.org/abs/2509.12667), [repo](https://github.com/ziyunliu4444/osu2mir)): uses osu! beatmaps (40k+ sets) as beat/downbeat annotations. Findings transferable to Beat Saber ranked maps: (1) maps with a single timing point or widely spaced (≥5s) timing points are reliable; closely spaced timing points need curation; (2) multiple community annotations of same song are highly consistent; (3) covers underrepresented genres (anime/Vocaloid/VGM) — same distribution as Beat Saber content.
- Released subset: 741 curated annotations / 708 audios (osu2beat2025) — potential extra eval corpus alongside user's ranked maps.
- **VERDICT: PROMISING — validates H4's premise** (rhythm-game note times ≈ usable sync ground truth); adopt their curation heuristics (timing-point spacing) when filtering our corpus.

## 5. DECISION LOG (append-only)

- **2026-07-05:** Staged hybrid adopted: Stage 1 pure-Java DSP (SuperFlux+HPSS), Stage 2 ONNX only if benchmarks demand. Revisit if Session 3 benchmark shows DSP ceiling too low even for onsets.
- **2026-07-05 (user):** License = GPL perpetual → NC-licensed weights (madmom) likely unusable for shipping; prefer permissive.
- **2026-07-05 (user):** Benchmark corpus: user provides 10–30 ranked maps + audio locally (never committed).
- **2026-07-05 (user):** Auto-bookmarks vs manual: **ask each time** on conflict.
- **2026-07-05 (user):** Jar size fine (+60–120MB). Bundle models in jar now; downloader script later.
- **2026-07-05:** Beat tracker choice = **Beat This!** (MIT, ONNX-proven, no DBN) — by elimination over madmom (NC weights + DBN port) and BeatNet (dominated, particle-filter port). Revisit only if local benchmark shows it failing on speedcore AND a half-tempo result is unusable for mapping.
- **2026-07-05:** Sectioning = **Foote checkerboard novelty** pure Java (Stage 1). allin1 rejected for shipping (NATTEN, deps, license taint). Laplacian parked for future labeling needs.
- **2026-07-05:** Adopt Osu2MIR curation heuristics for corpus filtering (single/wide-spaced timing points = reliable; consider osu2beat2025 subset as secondary eval corpus).
- **2026-07-05 (user):** **License concerns deferred** — "ignore the license for now, figure it out later." Weights-license findings stay recorded (§4) but do not block prototyping. Revisit before any release.
- **2026-07-05 (user):** Corpus delivered at `data/ground_truth/` (46 maps, mostly EDM, varied mapping styles, zips kept as pristine backups). More available: ranked maps post-2024 + curated maps. Dir gitignored.
- **2026-07-05:** Onset eval uses offset=0 (raw audio-time comparison vs note times); grid-alignment offset would uniformly shift estimates and distort F-measure. True BPM passed to getPeaksFromAudio to isolate onset quality from BPM-detection quality (BPM evaluated separately).
- **2026-07-05:** 8/46 corpus maps have real mid-song BPM changes → excluded from onset F-measure v1 (piecewise beat→sec conversion not implemented yet); still used for BPM eval. 4 more have no-op changes (same BPM) → treated as constant.
- **2026-07-05 (user idea, PARKED):** categorize mapping styles per map (AI or info.dat/BeatSaver tags) → feeds future genre/style-aware generation (RQ7 adjacent).
- **2026-07-05 (S3):** SuperFlux replaces old spectral-flux pipeline in `AudioAnalysis.getPeaksFromAudio` (same public API). Params from 96-combo corpus sweep: FFT 2048/hop 256, μ=2, Expert+ δ=0.60×positive-mean ODF, onset times at STFT **window center** (+FFT_SIZE/2/SR = 23.2ms — sweep-confirmed best, physically principled). Old pipeline deleted; recover via git history if ever needed.
- **2026-07-05 (S3):** Per-difficulty amplitude percentiles [90…55] replaced by δ ladder {2.5, 1.8, 1.3, 0.9, 0.6}×positive-mean (Easy→Expert+). Only Expert+ is benchmark-backed; easier tiers are heuristic (sweep: δ 1.5→R≈.61, 2.5→R≈.42) — revisit when per-tier ground truth exists.
- **2026-07-05 (S3):** Benchmark report label now `-Dbk.reportLabel=<label>` (default `baseline`) so reruns don't clobber earlier reports of the same date.
- **2026-07-05 (user):** 2f951 "Hello (BPM) 2023" explained: real BPM 2023, unplayable → mapper maps every X-th beat (252.9 = 2023/8). New PARKED feature request: **beat-grid timing tool** (tempo + BPM-change detection → notes on every/every-X-th beat, alternative to onset timing). Do at a later date; recorded in §1 checklist.
- **2026-07-05 (S4):** BPMDetector rebuilt (same public API): harmonic-comb (Σ AC(k·τ)/k, k=1..4) scan of a log-spaced 50–420 BPM grid on the SuperFlux ODF autocorrelation, × log-normal mapper-tempo prior, parabolic refinement. Old three-method ensemble + snap-to-common deleted (git history). `AudioAnalysis.estimateBPM` now delegates to `BPMDetector.estimateTempo` (reuses the already-computed ODF).
- **2026-07-05 (S4):** Tempo prior = center 260 BPM, σ 1.2 octaves — broadest of the four sweep winners (Acc1 35/46 each); chosen over the center-280 variants to keep the prior weak (comb-driven) on material unlike the corpus. Sweep grid: `sweep_bpm_2026-07-05.md`. Rationale recorded because corpus is EDM-heavy; revisit σ when ambient/calm maps arrive.
- **2026-07-05 (S4):** BPM eval scores variable-BPM maps against their *initial* metadata BPM — 3 of 4 remaining Acc2 failures are exactly those maps. Piecewise/variable-BPM-aware BPM eval deferred to the variable-BPM work (beat-grid tool / S7).
- **2026-07-05 (S5):** Sectioning features MUST be per-band z-scored before the cosine SSM. Raw unit-normed log-spectral frames of full-band music are near-parallel (cosine ≈ .97) → novelty scale collapses → F .146. Same pipeline with z-scoring: F .592. Recorded as a first-class negative result.
- **2026-07-05 (S5):** Novelty normalized by kernel mass, not per-song max — keeps the threshold absolute (1.0 = anticorrelated-block transition, flat song ⇒ ~0) and stops noise amplification on structure-free songs.
- **2026-07-05 (S5):** `FooteSectionDetector` defaults kernel 16s / minSection 6s / threshold 0.05 (sweep optimum, flat surface). Intensity tiers = within-song rank of loudness + onset density mapped to 5 tiers — deliberate heuristic, no ground truth exists; tiers express contrast inside one song only.
- **2026-07-05 (S5):** Bookmark ground truth curated Osu2MIR-style: ≥3 bookmarks AND median gap ≥5s (excludes lyric/choreo bookmark styles like SAtAN's word-per-beat). 14/46 corpus maps usable; `GroundTruthCorpus.CorpusMap` now carries `bookmarkTimesSeconds`.
- **2026-07-05 (S6):** Auto-bookmark names = SECTIONED-generator pattern flags (tier 0→"linear", 1→"1-2", 2/3→"complex", 4→"normal_jumps"; `SectionAnalysisService.TIER_FLAGS`) so applied sections drive generation with zero extra plumbing. Mapping is a playtest-tunable heuristic. First bookmark always at beat 0 (CreateMap drops notes before the first bookmark).
- **2026-07-05 (S6):** Song Map audio input: .wav/.mp3 natively (jlayer), .ogg/.egg only via ffmpeg-on-PATH fallback (dev convenience) — no pure-Java vorbis decoder in the jar. If users need .egg directly, add a vorbis dep later (license-check it).

## 6. BENCHMARK PROTOCOL

- **Onsets:** ground truth = ranked-map note times, chords/stacks deduped to one event. Report Precision AND Recall separately (mappers undermap → precision penalized unfairly; H4). Primary metric: **F-measure @ ±50ms** (mir_eval convention); secondary @ ±25ms.
- **BPM:** Accuracy1 (exact ±4%) and Accuracy2 (half/double/×3 tolerated) vs map BPM metadata.
- **Sectioning:** boundary hit-rate @ ±3s (stretch: ±0.5s) vs hand-placed bookmarks where available.
- **Corpus:** in-repo `src/test/resources/ISeeFire.txt` + user-supplied local folder (config-pointed, e.g. `OnsetGeneration/mp3Files/`), target 10–30 maps across genres, ≥1 speedcore, ≥1 ambient. Audio never committed.
- **Harness:** JUnit `@Tag("benchmark")`, excluded from default surefire; results appended to §8 per session so trends survive interruptions.

## 7. REFERENCES

- Böck & Widmer, "Maximum Filter Vibrato Suppression for Onset Detection" (SuperFlux), DAFx 2013.
- Schlüter & Böck, "Improved Musical Onset Detection with CNNs", ICASSP 2014.
- Böck et al., madmom: https://github.com/CPJKU/madmom
- Foscarin et al., "Beat This! Accurate Beat Tracking Without DBN Postprocessing", ISMIR 2024: https://github.com/CPJKU/beat_this
- Heydari et al., BeatNet: https://github.com/mjhydri/BeatNet
- Kim & Nam, "All-in-One Metrical and Functional Structure Analysis", 2023: https://github.com/mir-aidj/all-in-one
- Foote, "Automatic Audio Segmentation Using a Measure of Audio Novelty", ICME 2000.
- McFee & Ellis, "Analyzing Song Structure with Spectral Clustering", ISMIR 2014.
- MSAF: https://github.com/urinieto/msaf
- mir_eval (metric conventions): https://github.com/mir-evaluation/mir_eval
- ONNX Runtime Java: https://onnxruntime.ai/docs/get-started/with-java.html
- TU Wien MIR (Knees, Schindler): [Knees homepage](https://www.ifs.tuwien.ac.at/~knees/), [Schindler homepage](https://www.ifs.tuwien.ac.at/~schindler/); Schindler & Knees, "Deep Learning for MIR" tutorial: https://arxiv.org/pdf/2001.05266 — curated DL-MIR overview (onset/beat/structure/tagging), good syllabus substitute.
- Osu2MIR (Liu, ISMIR 2025 LBD): https://arxiv.org/abs/2509.12667 / https://github.com/ziyunliu4444/osu2mir
- beat_this_cpp (ONNX conversion recipe + model): https://github.com/mosynthkey/beat_this_cpp
- FMP Notebooks C4S4 (Foote novelty reference impl): https://www.audiolabs-erlangen.de/resources/MIR/FMP/C4/C4S4_NoveltySegmentation.html
- madmom_models (weights license): https://github.com/CPJKU/madmom_models

## 8. SESSION NOTES (append-only)

### 2026-07-05 — Session 1
- Explored codebase (3 agents): confirmed generation is timing-only; audio info discarded post-onsets; sections = manual bookmarks; `PatMetadata` genres unused; JavaFX UI has natural viz slot in TimingView; Python scripts disabled.
- Wrote CLAUDE.md, MUSIC_SYNC_PLAN.md (repo root), this log.
- User decisions captured (see §5).
- Web research RQ1–RQ6 done, verdicts in §4. Highlights:
  - SuperFlux ≈ CNN on percussive datasets (IDMT 0.884 vs 0.874), CNN +0.05 on dense tonal (MusicNet) → pure-Java Stage 1 justified.
  - **Beat This! is the beat-tracking answer**: MIT code+weights, pre-converted ONNX exists (beat_this_cpp, opset 14, chunked 1500-frame inference), no DBN postproc → no Java DBN port, no tempo-prior octave errors.
  - madmom weights CC BY-NC-SA → unshippable under GPL; whole madmom stack demoted to dev-reference.
  - allin1 rejected: NATTEN custom ops (no ONNX), broken deps, madmom in pipeline.
  - Foote novelty (FMP C4S4 as porting guide) chosen for sectioning; homologous-section weakness irrelevant for intensity tiers.
  - **Osu2MIR (ISMIR 2025)** independently validates rhythm-game maps as beat ground truth + curation heuristics (timing-point spacing) + osu2beat2025 (741 annotations) as secondary corpus.
### 2026-07-05 — Session 2 (same day, corpus + harness + baseline)
- User delivered corpus: `data/ground_truth/` — 46 maps (mostly EDM, ranked/curated, all Expert+), BPM 105–388, **33/46 >200 BPM**, ~166 min audio. Gitignored (`/data/`). Manifest: `docs/research/CORPUS_MANIFEST.md`.
- Built Java harness (`src/test/java/Benchmark/`): `OnsetEvaluator` (mir_eval-style greedy 1:1 matching, P/R/F, BPM Acc1/Acc2, chord dedupe 25ms — 6 unit tests green), `GroundTruthCorpus` (Info.dat v2 parse, ffmpeg egg→wav cache, variable-BPM detection), `BaselineBenchmarkTest` (@Tag("benchmark"), excluded via surefire `surefire.excludedGroups` property; run: `mvn test -Dtest=BaselineBenchmarkTest -Dsurefire.excludedGroups=none`).
- **BASELINE NUMBERS** (full table: `docs/research/benchmark_results/baseline_2026-07-05.md`, runtime ~64s, wav cache in corpus dir):
  - **Onsets (n=38 constant-BPM): mean P@50ms = 0.916, R@50ms = 0.536, F@50ms = 0.670.**
  - **BPM: Accuracy1 = 7/46 (15.2%), Accuracy2 = 32/46 (69.6%).**
  - (First run had 36a4e at F=0.000 — map is v3 format (`colorNotes`), loader only read v2 `_notes`. Fixed: v3 notes + `bpmEvents` supported; 36a4e → F=0.712. Only v3 map in corpus.)
- **Interpretation:**
  - **Recall is the bottleneck, not precision.** README's "many false positives" is NOT what mapper-ground-truth shows: detected peaks mostly align with mapped notes (P .92), but detector misses ~half of what mappers map (R .54). Likely causes: 55th-percentile amplitude gate drops weak-but-mapped onsets; mappers map streams through low-accent passages. → H4 revised: vs ranked maps, mappers OVERmap relative to conservative detectors; precision-friendly, recall-hostile. Stage-1 work should prioritize **recall-oriented peak picking** (lower/adaptive gates, ODF quality via SuperFlux) — watch precision as guardrail.
  - **H2 CONFIRMED structurally: BPM Acc1 15.2%.** Dominant failure = half/third-tempo octave errors from the 60–200 cap (e.g. 350→175.2, 340→170, 260→130). Acc2 69.6% shows the tracker "finds the pulse" but lands in wrong octave — exactly the DBN-tempo-prior-class failure Beat This! avoids. 14/46 fail even Acc2 (e.g. 300→180, 200→90) = real failures beyond octave.
  - **Anomaly: map 36a4e "Insane" (105 BPM) F=0.000** while BPM detect fine — suspect global time offset (songTimeOffset?) or ground-truth conversion issue. INVESTIGATE before trusting per-map comparisons.
- **User domain input (post-baseline):** mappers map beats + **melody** + **lyrics**; challenge maps cram notes between onsets; **bombs/obstacles sometimes carry onsets**; fitbeat maps (corpus 1a32a Oyasumi) encode body movement — hand/body position matters for mapping, not just timing. → H4 revised, H5 added. Notable: Oyasumi R=.633 > corpus mean .536 — even movement-driven maps follow audio events closely.
- **What might NOT work (explicit negatives):** plain spectrogram/heatmap as user-facing viz (user intuition confirmed — communicates nothing about sections); madmom anything in shipped product (license); allin1 (unexportable); BeatNet (dominated); current BPMDetector for speedcore (structural 60–200 cap); precision-weighted onset metrics vs mapper ground truth (mappers undermap → use recall-weighted reading, H4).

### 2026-07-05 — Session 3 (SuperFlux port + peak-picking rework)
- **Implemented** `AudioAnalysis/SuperFluxOnsetDetector.java` (pure Java, sits on `SpectrogramCalculator`): quarter-tone log filterbank (30Hz–17kHz, 24 bands/octave, bin-collision dedupe), log10(1+x) compression, frequency max-filter (width 3) on reference frame μ back, positive-diff sum; Böck-2013 peak picking (local max ±30ms, local mean −100/+70ms + δ, min-gap). Rewired `AudioAnalysis.getPeaksFromAudio` (API unchanged): FFT 1024→2048, hop stays 256; percentile gates → δ ladder. 5 synthetic unit tests in default suite (bursts, vibrato suppression, min-gap, time shift, positiveMean); full suite 725 green.
- **Sweep** (`SuperFluxSweepTest`, 96 combos: μ×δ×shift, ODFs cached per map): best **μ=2, δ=0.60, shift=23ms → P .777 / R .852 / F .805**. Surface flat (μ 1–4 within .01 F at δ 0.6) → robust. Window-center shift (+23ms = FFT_SIZE/2/SR) consistently ≥ 0/12ms variants.
- **Full benchmark** (`superflux_2026-07-05.md`): **F@50 .670 → .805 (+.135), R .536 → .852 (+.32), P .916 → .777**. BPM untouched (Acc1 15.2% / Acc2 69.6%).
- **Interpretation:**
  - Recall bottleneck was the amplitude gating, confirmed: removing percentile gates + better ODF = +.32 recall at modest precision cost. H1 supported locally (CNN ceiling still unmeasured).
  - **Speedcore onsets solved**: SAtAN (300 BPM) F .924, Glucagon (274) .912, "bus to the other world" (280) .922 — the dense-EDM regime is now the detector's BEST regime. Remaining sync failure for speedcore is BPM only.
  - **Precision dips on sparse/melodic/low-BPM maps** (Religion P .560/R .946, Oyasumi .667/.959, 36a4e .604/.918): detector fires on real audio events mappers chose to skip (H4 undermapping ambiguity — these "FPs" are largely legit onsets). Easier difficulties use higher δ, so production impact is Expert+-shaped density, acceptable.
  - **Worst map: 2f951 "Hello (BPM) 2023" F .629 (R .521)** — title suggests tempo-gimmick; candidate H5 style-noise case, queued for investigation.
- **Negative results:** μ=4 strictly dominated at equal δ (too much reference smearing at 172fps); δ≥2.0 reproduces baseline-like precision regime and caps F at ~.63 → old pipeline's percision-heavy operating point was leaving ~.17 F on the table; HPSS unnecessary (max-filter alone fixed vibrato/hi-hat false positives — skipped, less code).
- Report clobber fix: `-Dbk.reportLabel`.

### 2026-07-05 — Session 4 (BPM detector rework, H2 fix)
- **User input (session start):** 2f951 "Hello (BPM) 2023" = literal BPM-2023 song (unplayable → mapper maps every X-th beat, metadata 252.9 = 2023/8). Worst-onset-map mystery resolved as H5 style noise. New parked feature: beat-grid timing tool (§1).
- **Implemented** `BPMDetector` rewrite (same API, old ensemble deleted): SuperFlux ODF (μ=2, same params as onset path) → zero-mean, per-lag-normalized, half-wave-rectified autocorrelation → log-spaced 50–420 BPM grid (~0.5% steps), score = harmonic comb (Σ_{k=1..4} AC(k·τ)/k) × log-normal tempo prior → parabolic refinement in log-tempo. 6 synthetic unit tests (pulse trains 120/300 BPM, flat/short → default, AC normalization) — tests use the explicit-prior overload so retuning constants can't break them. Full suite 731 green.
- **Sweep** (`BPMSweepTest`, 36 prior combos, AC cached per map, 49 s): four-way tie at **Acc1 35/46 (76.1%) / Acc2 42/46 (91.3%)** — center 260/σ1.2, center 280/σ{0.65,0.8,1.2}. Picked 260/1.2 (weakest prior among winners). Acc1 falls off a cliff below center 240 (180 → 45.7%): the old detector's failure was as much *prior-shaped* (range cap = implicit prior at ≤200) as algorithmic.
- **Full benchmark** (`bpm_rework_2026-07-05.md`): **Acc1 15.2% → 76.1%, Acc2 69.6% → 91.3%**; onsets byte-identical (P .777 / R .852 / F .805 — BPM path doesn't touch peak picking).
- **Interpretation:**
  - **H2 CONFIRMED + FIXED**: the 60–200 cap was the speedcore failure. Direct hits at 274/280/286.5/290/300/306(×2 fold — see below)/340/350: SAtAN 300→299.9, KOKUSHIMUSOU 340→340.0 (both copies), Party People 350→349.9. All old non-octave folds (300→180=3/5, 200→90) eliminated by the comb — a wrong metrical level matches one lag but not its harmonic multiples.
  - **Remaining Acc1 misses (11) are octave-convention, not pulse errors**: 7 land exactly ×2/÷2 off metadata (350→175 ×2, 388→194, 306→153, 132→264, 142→284, 105→210). The audio genuinely supports both levels; mapper choice (map at 350 vs 175) is a *convention* the signal alone can't decide. Closing this needs style/genre priors or per-song user choice — parked; Acc2 91.3% is the honest signal-level score.
  - **Acc2 fails (4):** 3 are variable-BPM maps scored vs initial metadata BPM (Last Wish ×2: 210 vs 240.2 detected — song may actually sit at 240 for most of its runtime; Ashed Wings 240 vs 322.4) = eval artifact more than detector error. Only 1a15 "Nisemono Chuuihou" (260→298.5, constant BPM) is a real unexplained failure — 298.5/260 ≈ 8/7, suspect strong 7-against-8 syncopation; single map, not worth chasing.
  - 2f951 gimmick map: detector finds 252.7 ≈ the 2023/8 mapping grid — Acc1 ✓ despite the gimmick.
- **Negative results:** prior center ≤220 strictly worse (Acc1 ≤65%); σ=0.4 too narrow everywhere (kills legitimate 130–160 BPM maps); snap-to-common-BPM list (old detector) unnecessary — parabolic interpolation alone lands within ±0.3 BPM of integer metadata tempos.
- **Consequence for roadmap:** S7 ONNX beat tracking is no longer motivated by tempo accuracy; only variable-BPM tracking (beat-grid tool) or octave-convention modeling would justify it.

### 2026-07-05 — Session 5 (Foote sectioning prototype, H3)
- **Implemented** `AudioAnalysis/FooteSectionDetector.java` (pure Java, zero deps): SuperFlux filterbank log spectrogram (newly exposed as `SuperFluxOnsetDetector.filteredLogSpectrogram`) → 0.5s coarse frames → per-band z-score → unit norm → cosine SSM → Gaussian-tapered checkerboard novelty (kernel-mass normalized) → local-max boundary picking; plus heuristic 5-tier intensity rating (loudness + onset-density rank within song). 4 synthetic unit tests (orthogonal blocks, homogeneous, ABA, intensity ranking). Suite 735 green.
- **Ground truth:** mapper bookmarks from the hardest diff (`GroundTruthCorpus` extended). 28/46 maps have bookmarks; semantics vary wildly — sections ("Part 2"), **lyrics** (SAtAN word-per-beat), instrument cues ("guitar-1"). Curation: ≥3 bookmarks + median gap ≥5s + constant BPM → **14 usable maps**.
- **Sweep 1 FAILED (F .146)** — raw unit-normed log-spec features: cosine ≈ .97 between all frames of full-band EDM, novelty never crossed 0.10. Diagnosis: loudness-shape dominance; between-section contrast lives in *which bands sit above their song average*, not in the raw spectrum.
- **Fix + sweep 2/3 (80 combos, novelty cached per kernel):** per-band z-scoring → **boundary F@±3s .592 (P .624 / R .630)** at kernel 16s / threshold 0.05 / minSection 6s. Surface flat (kernel 16–24s, δ 0.01–0.05 ≥ .57); δ=0 (all local maxima) drops F → threshold does real work. `sweep_sectioning_2026-07-05.md`.
- **Interpretation:** H3 SUPPORTED at its ~60% bar. Per-map spread P .88/R .88 (p.h.) down to P .18 (168de, 3 macro bookmarks vs 11 detected) — residual error dominated by **reference granularity mismatch** (mappers bookmark macro-sections OR phrases; detector sits at ~16s structure), not by missed musical transitions. For the actual use case (intensity regions for generation + viz) the detector's granularity is arguably more useful than either extreme.
- **Negative results:** raw log-spec cosine SSM unusable (F .146); per-song max novelty normalization would amplify noise on flat songs (caught in design, kernel-mass normalization instead); kernel 32s too coarse (F ≤ .524), threshold ≥0.15 recall-starves (R ≤ .53).
- **Not done (next):** auto-bookmark writer (AppLogic service, ask-user-on-conflict) + Song Map viz — S6 pairs them naturally (`Result` carries novelty curve + boundaries + tiers for drawing).

### 2026-07-05 — Session 6 (Song Map UI + auto-bookmarks)
- **Implemented** `AppLogic/SectionAnalysisService` (UI-independent): audio file → wav (mp3 via jlayer converter; ogg/egg via ffmpeg fallback with a clear error if absent) → SuperFlux filterbank spectrogram → onsets (Expert+ params) + BPM estimate (`BPMDetector.estimateTempo` on the same ODF) + Foote boundaries + intensity tiers. `toBookmarks()` converts to SECTIONED-generator bookmarks (beat 0 + one per boundary, tier-named flags, tier colors).
- **UI** (`TimingView`, new "Song Map — sections & intensity" card): "Analyze audio…" file chooser (starts in the loaded map's folder) → background Task → Canvas: tier-colored section bands (blue calm → red peak), white novelty curve, onset ticks (bottom strip), black boundary lines. Status line shows sections/BPM/onset count. "Apply as bookmarks to active diff" replaces `map.bookmarks` — **confirmation dialog first when the diff already has bookmarks** (auto-bookmarks-ask-user hard constraint). Bookmarks survive save/export (`exportAsMap` serializes `_bookmarks`).
- **Verification:** full suite 737 green; headless smoke on corpus Oyasumi (`SectionAnalysisSmokeIT`, benchmark-tagged, kept): BPM 200.0 exact, 851 onsets, 9 sections with a musically plausible intensity arc (calm intro 0s → complex 21s → peak 41s → breakdown 50s → … → calm outro 132s).
- **Open:** tier→flag mapping is a heuristic first cut (playtest to tune); no pure-Java vorbis → .egg needs ffmpeg or the source wav/mp3; user hands-on UI test pending.

### 2026-07-05 — Session 6 follow-up (first user feedback: layout drift + playback)
- **Bug (user-reported): Timing tab drifted right, grew past the window.** Cause: `songMapCanvas.widthProperty().bind(box.widthProperty().subtract(32))` — the canvas width fed back into the card's *preferred* width (Canvas contributes its size to parent pref; pixel snapping rounds up) → +1px per layout pass → unbounded growth. The view sits in a StackPane/BorderPane center with no scroll clamp, so the whole tab walked off-screen. **Fix:** canvas wrapped in a holder `Pane` with explicit `prefWidth 0` / fixed height 140; canvas w/h bound to the holder. Parent width now flows *down* only; nothing feeds back up. Lesson (JavaFX): never bind a Canvas dimension to an ancestor whose pref size it influences.
- **Feature (user-requested): timeline/slider to control the song.** `AppLogic/AudioPreviewPlayer` — javax.sound `Clip` wrapper (pure Java, zero new deps; loads whole wav into RAM, fine for one song). `SectionAnalysis` record now carries `wavFile` (source or temp conversion) so playback reuses the analysis decode; player is loaded in the same background Task. UI: ▶/⏸ button, scrub slider (drag = visual preview only, seek on release), m:ss / m:ss time label, click-to-seek on the canvas, gold playhead line; AnimationTimer redraws only while playing. `TimingView.shutdown()` (called from `AppShell.shutdown`) releases the audio line.
- **Verification:** full suite green (exit 0). `SectionAnalysisServiceTest` updated for the record's new `wavFile` component.
- **User feedback (first hands-on test, 2026-07-05):** sections look right overall, but "a few sections are either placed incorrectly or a bit too late/early." Consistent with the method's resolution: 0.5s coarse frames + 16s checkerboard kernel smear the novelty peak, so boundaries can land ±1–2s off the perceived transition (benchmark tolerance is ±3s). **Session 7 candidate (a) sharpened: snap each detected boundary to the strongest onset (or beat-grid line) within ±1s — mappers place section starts on downbeats, novelty peaks on energy change.** Also added a volume slider (0..1 → dB master gain on the Clip, remembered across loads).
- **Onset sonification (user-requested):** "Click on onsets" checkbox — `AppLogic/ClickTrackRenderer` mixes a 1.5 kHz decaying sine click (30ms, librosa-`clicks`-style) at each detected onset into a temp-wav copy; rendered once in the background on first toggle, then the player switches clean↔clicked clip in place (position and play state carry over — `AudioPreviewPlayer.loadClickTrack`/`setClickTrackEnabled`). Verified headless: click RMS-diff ≈12k at the onset, exact-zero diff everywhere else, frame count preserved. This is the ear-level verification tool for onset quality — expect user feedback on false positives/negatives per genre to feed H4/H5.
