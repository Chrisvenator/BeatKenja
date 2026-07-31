# Style-Space Concept für konsistente, nicht-repetitive Maps

## Context

**Problem:** Niedriger Variance → langweilige, lineare Maps. Hohe Variance → chaotisch, unangenehm.
**Ziel:** Maps mit konsistenter Identität ("quirk"), leicht überraschend, nicht repetitiv.

**Aktueller Stand:** Ein einziges 109×109 Markov-Matrix. Variance = Dirichlet-Multinomial-Resampling dieser Matrix. State = einzelne Note (lineIndex × lineLayer × cutDirection). Kein Gedächtnis jenseits 1 Schritt.

**Kern-Einsicht:** Die Map ist boring, weil der Markov-Chain keine Identität hat — nur lokale Übergangswahrscheinlichkeiten. Ein "Style-Space" würde der Map eine *persistente Identität auf höherer Abstraktionsebene* geben: Die Matrix wird nicht global verändert, sondern *konditioniert auf einen Stil*.

---

## Das fundamentale Konzept: Style-Space

### Was ist ein "Style"?

Ein Style ist eine **Verteilung über Transition-Matrizen** — d.h. eine Art zu wählen, *welche* Übergänge bevorzugt werden.

Mathematisch: Wenn man 98.125 Maps hat, hat jede Map eine eigene 109×109 Matrix. Diese Matrizen sind Punkte in einem hochdimensionalen Raum. Ähnliche Maps → ähnliche Punkte → nahe beieinander. Das *ist* der Style-Space.

Der Style-Space muss gelernt oder konstruiert werden. Die Frage ist wie.

---

## Variante A: Manuelle Style-Achsen (kein ML, sofort implementierbar)

### Idee

Statt einen gelernten Raum zu nutzen, definiert man **20–30 messbare Eigenschaften** einer Map und verwendet diese als Style-Achsen:

| Achse | Bedeutung |
|---|---|
| `cutDirectionEntropy` | Wie viele verschiedene Schnittrichtungen → Diversität |
| `crossHandRatio` | Wie oft wechseln Hände weit auseinander |
| `horizontalBias` | Links/Rechts-Dominanz |
| `layerUsageBias` | Boden vs. Mitte vs. Oben |
| `patternLengthMean` | Durchschnittliche Länge einer Bewegungskette |
| `resetFrequency` | Wie oft kehren Hände zur Neutralstellung zurück |
| `diagonalUsage` | Anteil diagonaler Schnitte |
| `speedVariance` | Gleichmäßig schnell vs. wechselnd |

**Style-Vektor** = diese ~20 Zahlen für eine Map. Der Style-Space ist dieser 20D-Raum.

### Wie benutzt man ihn?

1. Einmalig beim Start der Map: **Wähle einen Style-Punkt** S (zufällig, oder aus einem Set von Vorlagen)
2. Suche die K nächsten Trainings-Maps zu S im Style-Space (KNN)
3. **Mixe** deren Transition-Matrizen gewichtet nach Distanz → eine neue, kohärente Matrix M_S
4. Benutze M_S für die gesamte Map

**Warum konsistent?** S wird einmal gewählt und bleibt — alle Noten entstammen derselben M_S.
**Warum nicht repetitiv?** M_S ist ein Mix aus echten Maps, enthält echte Varianz — aber *strukturierte* Varianz.

### Überraschungs-Mechanismus

- Zwischen Sektionen (via `FooteSectionDetector`): kleiner Drift in S → S' = S + δ
- δ ist klein und zufällig, aber bleibt in der "Nachbarschaft" von S
- Das gibt das Gefühl "leicht überraschend" ohne die Map-Identität zu verlieren
- Der Drift-Betrag wäre ein User-Parameter: 0 = perfekt konsistent, groß = abenteuerlich

---

## Variante B: Style-Archetypen (Cluster-basiert)

### Idee

Statt K-nächste-Nachbarn: zuerst **clustern** aller 98k Maps in K Archetypen (z.B. K=20–50 via K-Means über ihren Style-Vektor). Jeder Archetyp = ein "reiner Stil" (z.B. "Flow Heavy", "Technical Wrists", "Stream Dominant", "Cross Pattern", "Top Row Specialist").

Der Style-Space ist dann ein **Simplex** über K Eckpunkte. Ein Style-Punkt = Gewichtungsvektor über die Archetypen (barycentrische Koordinaten).

**Beispiel:**
- Pure Style 3 ("Technical Wrists") = `[0,0,1,0,...,0]`
- Mix aus Style 3 und Style 7 = `[0,0,0.6,0,0,0,0.4,0,...,0]`

**Matrix-Erzeugung:** Gewichteter Mittelwert der Archetyp-Matrizen. Lineares Mischen von Markov-Matrizen ist problemlos.

### Vorteil

- Explizit und interpretierbar: der User könnte theoretisch Archetypen labeln
- Jeder Archetyp hat einen echten Charakter aus Trainingsdaten
- Interpolation erzeugt "hybride Stile" die nirgendwo sonst existieren

### Für "consistent quirk"

Man wählt **einen Punkt** im Simplex für die ganze Map. Nahe an Eckpunkt = starke Identität. In der Mitte = generischer Mix. Die Überraschung kommt aus dem Reichtum der Übergänge *innerhalb* des Stils, nicht aus Stil-Wechseln.

---

## Variante C: Latenter Raum via PCA (datengetrieben, keine neuronalen Netze)

### Idee

Flache jede 109×109 Matrix zu einem 11.881D-Vektor. Führe **PCA** über alle 98k Maps durch. Die ersten N Hauptkomponenten (z.B. N=50–100) erklären den Großteil der Varianz.

**Ergebnis:** Jede Map hat einen 50-100D Koordinatenvektor. Das *ist* der Style-Space — gelernt aus den Daten, ohne Label, ohne Neural Net.

**Besonderheit:** Die Hauptkomponenten sind interpretierbar:
- PC1 könnte mit BPM/Dichte korrelieren
- PC2 mit Links/Rechts-Symmetrie
- PC3 mit Cut-Direction-Diversität
- usw.

### Einsatz

- Trainings-Maps sind projizierte Punkte im PCA-Raum
- Zur Generierung: wähle Koordinatenvektor → projiziere zurück in Matrix-Raum → benutze als Transition-Matrix
- Nächste-Nachbarn oder direkte Interpolation funktionieren

### Trade-offs

Pro: vollständig datengetrieben, kein manuelles Feature-Engineering
Con: PCA über 11.881D × 98.125 Maps ist rechenintensiv (einmalig offline aber groß); erfordert Batch-Processing; PCA-Punkte außerhalb der Trainings-Verteilung können unsinnige Matrizen erzeugen

---

## Variante D: Stil als Pfad, nicht Punkt (dynamisches Narrativ)

### Idee

Die bisherigen Varianten geben der Map *einen* Style-Punkt. Was wenn der Style *langsam driftet* — wie eine Geschichte, die sich entwickelt?

**Konzept:**
1. Wähle Startpunkt S₀ und Zielpunkt S_T im Style-Space
2. Die Map interpoliert linear (oder per Kurve) von S₀ nach S_T
3. Jede Sektion liegt auf diesem Pfad an einem anderen Punkt

**Warum das "consistent quirk" erzeugt:** Man hört die Transformation — der Stil wandelt sich, aber nicht sprunghaft. Es fühlt sich wie eine Map an, die "irgendwo hingeht".

**Variation:** Mehrere Keyframes S₀ → S₁ → S₂ → ... wie ein Musikvideo-Storyboard.

**Verbindung zu Sektionen:** `FooteSectionDetector` liefert bereits Sektion-Grenzen mit Intensitäts-Tier. Man könnte Sektion-Typ (ruhig/build/peak) auf Bereiche im Style-Space mappen: ruhige Sektionen → Flow-nahe Archetypen, Peak-Sektionen → Tech/Speed-nahe Archetypen.

---

## Vergleich der Varianten

| | Variante A (Manuell) | B (Archetypen) | C (PCA) | D (Pfad) |
|---|---|---|---|---|
| ML nötig | nein | nein (K-Means) | nein (PCA) | kombinierbar |
| Interpretierbar | ja | ja | teilweise | ja |
| Implementierungsaufwand | gering | mittel | hoch | mittel |
| Style-Qualität | gut (aber hand-curated) | sehr gut | hervorragend | meta-layer über A/B/C |
| Consistent quirk | ✓ | ✓ | ✓ | ✓✓ |
| Natürliche Überraschung | mäßig | gut | gut | sehr gut |

---

## Empfohlener Ansatz: Variante A + D kombiniert

**Phase 1 (sofort denkbar):**
- Definiere ~15 Style-Achsen als messbare Map-Statistiken
- Baue Style-Vektoren für alle Trainings-Maps
- Bei Generierung: wähle S → KNN → mix Matrix → generiere

**Phase 2 (für "narrative maps"):**
- Sektion-Grenzen (aus `FooteSectionDetector`) als Keyframes
- Jede Sektion bekommt einen leicht gedrifteten S_i
- Intensitäts-Tier der Sektion beeinflusst Drift-Richtung im Style-Space

**Phase 3 (optional, falls Datenbasis ausreicht):**
- PCA über gemessene Style-Vektoren (nicht Matrizen direkt) → komprimierter, rauschärmerer Raum
- Nutze als Basis für Phase 2

---

## Was die Style-Space-Idee grundlegend anders macht

**Aktuell:** Jede Note kennt nur die vorherige Note. Keine Identität.
**Mit Style-Space:** Jede Note ist aus einer *gefilterten Matrix* gezogen — gefiltert auf die Map-Identität, die beim Start gewählt wurde. Das ist wie: der Componist hat eine Handschrift, jede Phrase trägt diese Handschrift.

Die Map entsteht nicht mehr aus einem globalen Pool aller Möglichkeiten, sondern aus einem *persönlichen* Pool — dem, was dieser Stil erlaubt.

Das ist der Kern: **Style-Space als persistente Konditionierung der Transition-Matrix**.

---

## Keine Code-Änderungen — nur Konzepte

Dieser Plan enthält kein Implementierungsdetail, nur konzeptuelle Frameworks. Nächster Schritt wäre: Entscheid welche Variante weiter ausgearbeitet werden soll, dann Prototyp-Planung.
