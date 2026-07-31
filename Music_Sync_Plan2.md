# BeatKenja — Generation Architecture Plan: Sync-Grounded, Quality-Weighted, Style-Conditioned Higher-Order Markov

> Plan only. No code. Concepts + sequencing + verification.

---

## Context

**Problem.** Generated maps are boring and linear at low variance; chaotic and unpleasant at high variance. The current engine is a single 1st-order Markov chain over single notes (109×109 matrix), with one global variance knob (Dirichlet-Multinomial resampling). It has no *identity* — every note only knows the previous note, drawn from a global pool of all possibilities. That is why maps feel either flat or random, never "consistent but surprising."

**Goal.** Maps that are complex-but-readable, predictable-but-not-repetitive, with a *consistent quirk* (a recurring motif/identity across the whole map), and grounded in music sync.

**Decisions made this session:**
1. **Priority = Music sync FIRST.** Note-placement quality is bounded by timing quality; sync correlates strongest with map quality. Style-space generation is designed here but sequenced *after* sync hardening.
2. **Corpus = ranked-weighted, all-in.** Ingest all ~95k maps, weight each map's contribution by quality (ranked ≫ unranked, scaled by star rating). Max data, quality-biased.
3. **Engine = higher-order Markov.** Upgrade state from single-note to previous-2-notes (+ timing context). Richer local structure, less repetitive. Sparser matrix — 95k maps + backoff smoothing handle it.

**Hard constraints (from CLAUDE.md):**
- End users double-click a jar. **No Python, no downloads, no API calls at runtime.** All corpus ingestion + matrix/archetype training is **dev-side offline**; the jar ships pre-computed artifacts baked into `resources/`.
- GPL: prefer permissive assets. ScoreSaber/BeatLeader API *data* (ranked flag, stars) is fine (facts, dev-side). No CC-BY-NC model weights.
- Sync work stays tracked in `MUSIC_SYNC_PLAN.md` / `docs/research/SYNC_RESEARCH_LOG.md` — this doc references it, does not duplicate it.

---

## Current-State Facts (from exploration)

- **Loader** `BeatSaberMap.newMapFromJSON` (`src/main/java/BeatSaberObjects/Objects/BeatSaberMap.java:136`): V2 full (Gson), V3 **notes-only** (manual `colorNotes` b,x,y,c,d), **V1 + V4 = stubs** → this is the "new format we can't load" gap.
- **Train data** (`train/`): 7 maps, all V2 (`_notes` / `_lineIndex,_lineLayer,_type,_cutDirection`), mix of `_version` 2.0.0 and 2.2.0.
- **info.dat** V2: `_beatsPerMinute`, `_difficultyBeatmapSets[]` → `_beatmapCharacteristicName` (Standard/90/360/Lawless/Lightshow/NoArrows) → `_difficultyBeatmaps[]` (`_difficulty`,`_difficultyRank`,`_beatmapFilename`,`_noteJumpMovementSpeed`). Loading currently iterates `.dat` files, does **not** parse info.dat for difficulty→file mapping.
- **No ranked/quality/weight concept anywhere** in production code — greenfield.
- **State machinery**: `Pattern.java` (109×109 `count`/`probabilities`), `ComplexPattern.complexPattern` (generation loop), `GenerationContext.patternVariance` (single knob), `PatMetadata` (genre/tags — stored, never used in placement).
- JSON: `org.json` for version detection + V3, **Gson** for V2 deserialization.

---

## Architecture Overview (target)

Four layers, bottom-up. Each is a phase.

```
[ Song audio ] --sync--> onsets/BPM/sections/intensity   (PHASE 1, priority)
      |                                                    |
      | (north-star bridge, PHASE 4)                       | timings feed generation
      v                                                    v
[ Style-space coordinate S ] --conditions--> [ Higher-order Markov engine ] --> notes
      ^                                                    ^
      |                                                    |
[ Style-space: archetypes + axes ]        [ Quality-weighted higher-order matrices ]
      ^                                                    ^
      +----------------- built dev-side from ---------- [ 95k-map corpus, ranked-weighted ]
                                                          (PHASE 0 infra)
```

- **Phase 0** — Corpus + data infrastructure (multi-format loader, 95k ingestion, quality weighting). Dev-side pipeline → baked artifacts.
- **Phase 1 (NOW)** — Music sync hardening (tracked in existing sync plan; the foundation).
- **Phase 2** — Higher-order Markov engine.
- **Phase 3** — Style-space (Variant A axes + archetypes + Variant D drift) layered on Phase 2, driven by Phase 1 sections.
- **Phase 4 (north star)** — Song→style bridge: program "listens and acts accordingly."

---

## PHASE 0 — Corpus & Data Infrastructure

Everything downstream is bounded by how much clean, well-labeled data we can ingest. This is dev-side; output is artifacts baked into the jar.

### 0.1 Multi-format map loading (fixes the "new format" gap)

Two loading needs, different depth:
- **Ingestion loader (light)** — for the 95k corpus we only need *notes + timing + characteristic + BPM*. So even notes-only V3/V4 parsing suffices for training. Priority: breadth over completeness.
- **Editing loader (full)** — for user-loaded maps: V3 chains/arcs/bombs/obstacles, V4 split files. Lower priority; can trail.

Concept: introduce a **`MapPackage` abstraction** — parse `Info.dat`/`info.dat` (both casings) → enumerate characteristic sets → difficulties → `.dat` files → audio file. Version-detect each difficulty file and dispatch to a per-version notes extractor. Normalize *everything* to the existing internal `Note` model (`_time,_lineIndex,_lineLayer,_type,_cutDirection`) so the rest of the system is version-agnostic.
- V2: reuse existing Gson path.
- V3: extend the existing notes-only parser (`b,x,y,c,d` → Note); add `a` (angle offset) awareness later.
- **V4 (the gap):** new schema — info + separate difficulty file with `colorNotes` (`b,x,y,c,d,a`) and `bombNotes`; lightshow split out. Write a V4 notes extractor.
- Represent quantized time as beats consistently (V2 `_time` is in beats; confirm V4 units on real files).

### 0.2 Quality weighting (ranked-weighted, all-in)

- **Weight per map** `w = base(ranked?) × f(stars) × g(optional signals)`:
  - Ranked base ≫ unranked (e.g. ranked weight order-of-magnitude higher).
  - Within ranked: scale by star rating (harder-ranked maps often cleaner patterns — but confirm; may want per-difficulty normalization so a 10★ doesn't swamp mid maps).
  - Optional later signals: upvotes/score, mapper reputation.
- **Source (dev-side, offline):** map hash → ScoreSaber + BeatLeader API → ranked flag + stars. Pre-fetched once during ingestion, cached to a local manifest. **Never at runtime.**
- **Where the weight applies:** transition counts accumulate `+= w` instead of `+= 1`. High-quality maps dominate learned probabilities. Same weights gate/weight the style-space clustering (0.3 / Phase 3) so bad maps don't pollute archetypes.
- **Quality floor** for style archetypes (e.g. ranked-only or w above threshold); full weighted corpus for the raw transition statistics.

### 0.3 Ingestion pipeline (dev-side)

Batch job: walk corpus → `MapPackage` load → per color, per characteristic, per difficulty-tier → accumulate weighted higher-order counts (Phase 2) + per-map style vectors (Phase 3). Output baked artifacts:
- Higher-order transition tables (serialized, compact) per (color, characteristic, difficulty-tier).
- Style archetype set + axis normalization stats.
- Bundled in `src/main/resources/`. Jar-size increase acceptable per constraints.

---

## PHASE 1 — Music Sync (PRIORITY / NOW)

Sync is the foundation; do not build generation on shaky timing. **This phase is owned by the existing sync plan** — no duplication here. Relevant to *this* doc:
- Onset/BPM/section outputs are the timing grid generation fills.
- `FooteSectionDetector` section boundaries + intensity tiers are the input to Phase 3 style-drift (Variant D) and the cheapest path to the Phase 4 bridge (intensity → style, no genre classifier needed).
- **Action here:** keep sync work benchmarked (F@50ms, BPM Acc1/Acc2) per the research-log discipline; treat its outputs as the contract Phase 2/3 consume.

---

## PHASE 2 — Higher-Order Markov Engine

Core lever for "surprising but not random." Local structure gets memory beyond one note.

### 2.1 State definition

- **From:** state = single note (108 placements + base).
- **To:** state = **(note_{t-1}, note_{t-2})** per color → context space 108² ≈ 11,664 contexts × 109 successors.
- **Timing context (recommended):** bucket the inter-note gap (e.g. stream / normal / sparse) into the state, because what is *readable* depends on speed. State = (prev note, prev-prev note, gap-bucket). This directly serves "readable, not unreadable."

### 2.2 Sparsity + backoff (why 95k matters)

- 2nd-order is sparse; many contexts unseen or low-count.
- **Katz backoff / interpolated smoothing:** when a 2nd-order context has too few weighted observations, fall back to 1st-order, then to the base distribution. Blend by observation count.
- 95k weighted maps × ~hundreds of notes each → enough mass for common contexts; backoff covers the tail.
- Keep the existing single-note matrix as the **backoff floor** — reuses proven machinery.

### 2.3 Variance control, re-homed

- Keep Dirichlet-Multinomial variance, but apply **per higher-order context row**, not globally. Higher order already reduces randomness (more context = more determined), so the variance knob now tunes *surprise within a known context* rather than global chaos.
- This is the mechanical fix for "raise variance → chaos": variance now perturbs a context-specific distribution, so surprises stay in-style.

---

## PHASE 3 — Style-Space (Variant A + D), Layered on Phase 2

Gives the map its *identity*. The transition engine is now **conditioned on a style coordinate chosen once per map** (A), optionally drifting across sections (D). This is the answer to the user's "X-dimensional space where each location = a style."

### 3.1 Style axes (Variant A — manual, interpretable, no ML needed)

Define ~15–25 measurable per-map statistics as style axes. Examples:
- `cutDirectionEntropy`, `diagonalUsage`, `resetFrequency`, `crossHandRatio`, `horizontalBias`, `layerUsageBias` (floor/mid/top), `patternLengthMean`, `speedVariance`, `doubleFrequency`, `window/inverse ratio`.
- Each map → a style vector in this ~20-D space. Quality-weighted. **This is the "100-dimensional space" the user imagined** — points near each other = similar feel.

### 3.2 Archetypes (Variant B, folded in)

- Cluster the quality-floored corpus (K-Means, K≈20–50) → named archetypes ("Flow-heavy", "Tech-wrist", "Stream-dominant", "Cross-pattern"…).
- Style-space becomes a **simplex over K archetypes**; a style point = weights over archetypes (barycentric coords).
- Cross-check clusters against unused `PatMetadata` genre/tags for validation/labeling.

### 3.3 Conditioning the engine

- A chosen style point S → select K-nearest corpus maps (or archetype mix) → **build a style-conditioned higher-order matrix** M_S by quality-weighted blend of their transition tables (linear mixing of Markov matrices is safe).
- Whole map generated from M_S → **consistent quirk** by construction: every note drawn from the same identity-filtered pool, not the global pool.
- Surprise = Phase 2 variance *within* M_S. In-style, bounded.

### 3.4 Narrative drift (Variant D — the "best result" combo the user wants)

- Map is not one point but a **path**: keyframes S₀ → S₁ → … along the song.
- **Drive keyframes from Phase 1 sections + intensity tiers:** calm section → flow-near archetype; peak → tech/speed-near. Drift between keyframes is small and continuous → the style *transforms* without jumping → "map that goes somewhere."
- User parameter: drift magnitude. 0 = perfectly consistent; larger = more adventurous. This *is* the "leicht überraschend, aber nicht zu viel" knob, at the identity level instead of the note level.

---

## PHASE 4 — Song → Style Bridge (North Star, hard, later)

The user's "program listens to the song and acts accordingly." Deferred but written down; correlates strongest with quality.

- **Cheap first step (reuse Phase 1):** section **intensity tier → style-space region** (no genre classifier). This is achievable as soon as Phase 3 drift exists — intensity already computed by `FooteSectionDetector`.
- **Full version:** audio → descriptors (genre, energy, spectral character, rhythmic density) → learned mapping to a style coordinate. Pick the archetype/path automatically from the song.
- **Risk/notes:** hardest part of the whole system; genre classification + audio→style regression is a research effort of its own. Keep as a separate track once Phase 2–3 measurably work. Do **not** block generation on it.

---

## Sequencing (recommended)

1. **Phase 1 sync** (priority, ongoing, own plan) — keep benchmarking; freeze the timing contract.
2. **Phase 0.1 loader** (V4 + info.dat + `MapPackage`) — unblocks corpus. Highest-leverage code work.
3. **Phase 0.2/0.3** — quality weighting + dev-side ingestion → baked artifacts.
4. **Phase 2** — higher-order engine + backoff, on existing single-note as floor.
5. **Phase 3** — style axes → archetypes → conditioning → section drift.
6. **Phase 4** — intensity→style cheap bridge, then full audio→style (research track).

Phases 0.1 and 1 can proceed in parallel (independent code areas).

---

## Verification

- **Loader (0.1):** round-trip test — load V2/V3/V4 sample maps, assert note counts + timings match a known reference; add V4 fixtures to `train/` once available. Confirm `train/` 7 maps + a synthetic V3/V4 file all normalize to the internal `Note` model.
- **Weighting (0.2):** unit-check that ranked maps produce higher matrix mass than unranked at equal note counts; verify a bad-map's contribution is measurably down-weighted.
- **Higher-order (2):** measure repetition + surprise metrics on generated maps vs single-note baseline (e.g. n-gram novelty, self-similarity). Backoff coverage stat: % of contexts served at 2nd vs 1st vs base order.
- **Style-space (3):** generate the *same song* under different style points → maps should be internally consistent yet distinct from each other. Human A/B: "has an identity?" / "surprising but not random?" Quantify with the Phase-3 axis stats (a generated map's own style vector should sit near its target S).
- **Drift (3.4):** style vector measured per section should track the keyframe path.
- **Benchmark discipline:** any sync-touching change → before/after F@50ms + BPM Acc per CLAUDE.md. Generation changes → the repetition/surprise metrics above, seeded via `-Dbk.seed`.
- **End-user constraint check:** confirm zero runtime Python/API/download — jar ships all artifacts.

---

## Open Questions (for later, not blocking the plan)

- Star-rating normalization: per-difficulty or global? (10★ maps shouldn't swamp mid-tier style-space.)
- V4 time units + exact `colorNotes`/`bombNotes` schema — confirm on real V4 files (none in current `train/`).
- Higher-order: 2nd-order enough, or add gap-bucket to state now vs later?
- Archetype count K — tune empirically against genre/tag validation.

---

*No code written. Concepts + sequencing only, per request. Decisions incorporated: sync-first, ranked-weighted all-in corpus, higher-order engine, Variant A+D style-space, music→style as north star.*
