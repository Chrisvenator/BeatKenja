package UserInterfaceFX.Viewer;

import BeatSaberObjects.Objects.Note;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;

/**
 * First-person 3D note lane for the Viewer tab.
 * <p>
 * Builds all note nodes once on {@link #setNotes} and positions them at absolute
 * Z = sec * zPerSecond (NJS). Each frame, only the root notes group's translateZ changes —
 * near/far clip culls everything outside the visible window.
 * NJO (beat offset) shifts how far ahead notes first appear without rebuilding.
 * <p>
 * Coordinate system: +X right, +Y DOWN (JavaFX 3D default), +Z into screen.
 * The camera looks along +Z; notes in the future have large positive world-Z, notes in the
 * past have negative world-Z (behind the eye).
 *
 * Lane layout: columns 0-3 → X = 0..3, layers 0-2 → Y = 0..-2 (+Y is down).
 */
public final class NoteField3D extends Region {

    // --- lane constants ---
    private static final double CELL        = 1.0;
    private static final double CAMERA_BACK = 6.0;  // eye Z behind hit plane

    // NJS / NJO — mutable, changed via setNjs / setNjoOffsetSeconds
    private double zPerSecond  = 16.0;  // world units per second; maps 1-to-1 to Beat Saber NJS
    private double njoOffsetZ  = 0.0;   // world-unit Z shift from NJO (pre-computed, no rebuild)
    private double lastPlayhead = 0.0;

    // cut-direction → Z-rotation degrees (arrow base points up = -Y)
    private static final double[] CUT_ANGLE = {
            0,    // 0 = up
            180,  // 1 = down
            270,  // 2 = left
            90,   // 3 = right
            315,  // 4 = up-left
            45,   // 5 = up-right
            225,  // 6 = down-left
            135,  // 7 = down-right
            // 8 = dot, handled separately (no arrow)
    };

    // --- shared geometry/material ---
    private static final PhongMaterial MAT_RED;
    private static final PhongMaterial MAT_BLUE;
    private static final PhongMaterial MAT_BOMB;
    private static final PhongMaterial MAT_ARROW;
    private static final TriangleMesh  ARROW_MESH;

    static {
        MAT_RED   = new PhongMaterial(Color.web("#e23b34"));
        MAT_RED.setSpecularColor(Color.web("#ff8080"));
        MAT_RED.setSpecularPower(20);

        MAT_BLUE  = new PhongMaterial(Color.web("#3b6de2"));
        MAT_BLUE.setSpecularColor(Color.web("#80a0ff"));
        MAT_BLUE.setSpecularPower(20);

        MAT_BOMB  = new PhongMaterial(Color.web("#20232a"));
        MAT_BOMB.setSpecularColor(Color.web("#888888"));
        MAT_BOMB.setSpecularPower(60);

        MAT_ARROW = new PhongMaterial(Color.WHITE);
        MAT_ARROW.setSpecularColor(Color.WHITE);
        MAT_ARROW.setSpecularPower(5);

        // Triangle pointing up (-Y). Vertices at world-space coords relative to note center.
        ARROW_MESH = new TriangleMesh();
        ARROW_MESH.getPoints().addAll(
                 0f,   -0.28f, 0f,   // tip (up)
                -0.22f, 0.16f, 0f,   // base-left
                 0.22f, 0.16f, 0f);  // base-right
        ARROW_MESH.getTexCoords().addAll(0f, 0f, 1f, 0f, 1f, 1f); // required, unused
        ARROW_MESH.getFaces().addAll(0, 0, 1, 1, 2, 2);
    }

    private final Group notesGroup = new Group();
    private final SubScene subScene;

    public NoteField3D() {
        Group world = new Group();

        PerspectiveCamera cam = new PerspectiveCamera(true);
        cam.setFieldOfView(45);
        cam.setVerticalFieldOfView(true);
        cam.setNearClip(0.1);
        cam.setFarClip(50);
        cam.setTranslateX(1.5 * CELL);
        cam.setTranslateY(-1.0 * CELL);
        cam.setTranslateZ(-CAMERA_BACK);
        cam.getTransforms().add(new Rotate(8, Rotate.X_AXIS));

        AmbientLight ambient = new AmbientLight(Color.rgb(80, 80, 90));
        PointLight key = new PointLight(Color.rgb(230, 230, 235));
        key.setTranslateX(1.5 * CELL);
        key.setTranslateY(-8 * CELL);
        key.setTranslateZ(-10);

        Group staticGeometry = buildStaticGeometry();

        world.getChildren().addAll(ambient, key, staticGeometry, notesGroup);

        subScene = new SubScene(world, 800, 480, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#0b0f1a"));
        subScene.setCamera(cam);

        getChildren().add(subScene);
        setMinSize(0, 0);
        setPrefSize(800, 480);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        subScene.setWidth(w);
        subScene.setHeight(h);
    }

    /**
     * Note jump speed: controls how fast notes fly toward the camera (world units per second).
     * Matches Beat Saber's NJS semantics — higher = faster, farther lookahead at the same FOV.
     * Triggers a full node rebuild because note Z positions depend on zPerSecond.
     *
     * @param njs note jump speed (world units / second, clamped to &ge;1)
     * @param currentNotes the currently loaded notes array (may be null)
     * @param bpm          the map's BPM
     */
    public void setNjs(double njs, Note[] currentNotes, double bpm) {
        this.zPerSecond = Math.max(1.0, njs);
        setNotes(currentNotes, bpm);
        applyGroupTranslate();
    }

    /**
     * Note jump offset: shifts how far ahead notes first spawn, in seconds (Beat Saber NJO
     * is in beats; callers must convert: offsetSec = njo / bpm * 60). Positive = notes spawn
     * farther ahead; negative = closer. No rebuild — only the group translateZ is adjusted.
     *
     * @param offsetSeconds NJO converted to seconds
     */
    public void setNjoOffsetSeconds(double offsetSeconds) {
        this.njoOffsetZ = offsetSeconds * zPerSecond;
        applyGroupTranslate();
    }

    /**
     * Rebuilds the note field from the given notes array and BPM.
     * Must be called on the JavaFX thread. Guards against null notes.
     *
     * @param notes note array from the active diff (may be null)
     * @param bpm   the map's BPM for beat→second conversion
     */
    public void setNotes(Note[] notes, double bpm) {
        notesGroup.getChildren().clear();
        if (notes == null || bpm <= 0) return;
        for (Note note : notes) {
            Group node = buildNoteNode(note, bpm);
            if (node != null) notesGroup.getChildren().add(node);
        }
    }

    /**
     * Advances the 3D playhead. One transform mutation; near/far clip culls off-screen notes.
     * Called every AnimationTimer frame.
     *
     * @param playheadSeconds current audio playback position in seconds
     */
    public void setPlayheadSeconds(double playheadSeconds) {
        this.lastPlayhead = playheadSeconds;
        applyGroupTranslate();
    }

    public void clear() {
        notesGroup.getChildren().clear();
        lastPlayhead = 0;
        njoOffsetZ   = 0;
        applyGroupTranslate();
    }

    /** Single place that computes notesGroup.translateZ from playhead + NJO offset. */
    private void applyGroupTranslate() {
        // NJO shifts the entire lane forward: notes spawn njoOffsetZ units farther out.
        // Positive NJO → notes appear earlier (farther ahead) → shift group toward camera.
        notesGroup.setTranslateZ(-lastPlayhead * zPerSecond + njoOffsetZ);
    }

    // --- private builders ---

    private Group buildNoteNode(Note note, double bpm) {
        double sec = note._time / bpm * 60.0;
        double x   =  note._lineIndex * CELL;
        double y   = -note._lineLayer  * CELL;  // +Y down → higher layer = smaller y
        double z   =  sec * zPerSecond;

        Group group = new Group();
        group.setTranslateX(x);
        group.setTranslateY(y);
        group.setTranslateZ(z);

        if (note._type == 3) {
            // Bomb: dark sphere, no arrow
            Sphere sphere = new Sphere(0.42);
            sphere.setMaterial(MAT_BOMB);
            group.getChildren().add(sphere);
        } else if (note._type == 0 || note._type == 1) {
            PhongMaterial mat = note._type == 0 ? MAT_RED : MAT_BLUE;
            Box cube = new Box(0.75, 0.75, 0.75);
            cube.setMaterial(mat);
            group.getChildren().add(cube);

            if (note._cutDirection >= 0 && note._cutDirection < 8) {
                // Directional arrow
                MeshView arrow = new MeshView(ARROW_MESH);
                arrow.setMaterial(MAT_ARROW);
                arrow.setCullFace(CullFace.NONE);
                arrow.setTranslateZ(-(0.375 + 0.02)); // just off the -Z face toward camera
                arrow.getTransforms().add(new Rotate(CUT_ANGLE[note._cutDirection], Rotate.Z_AXIS));
                group.getChildren().add(arrow);
            }
            // cutDirection == 8 (dot): no arrow; note is still rendered as a cube
        } else {
            return null; // unknown type, skip
        }

        return group;
    }

    /** Floor plane and converging column guides for depth perception. Both are static (never move). */
    private Group buildStaticGeometry() {
        Group g = new Group();

        // Floor: a flat dark box stretching the visible lane length, just below layer 0
        double laneDepth = 50 + CAMERA_BACK; // match farClip
        PhongMaterial floorMat = new PhongMaterial(Color.web("#141820"));
        Box floor = new Box(4 * CELL + 1, 0.05, laneDepth);
        floor.setMaterial(floorMat);
        floor.setTranslateX(1.5 * CELL);
        floor.setTranslateY(0.55 * CELL); // just below layer 0
        floor.setTranslateZ(laneDepth / 2.0 - CAMERA_BACK);
        g.getChildren().add(floor);

        // Column guides: 5 thin boxes at x = -0.5, 0.5, 1.5, 2.5, 3.5 running down +Z
        PhongMaterial guideMat = new PhongMaterial(Color.web("#2a2f3a"));
        for (int i = 0; i <= 4; i++) {
            Box guide = new Box(0.03, 0.03, laneDepth);
            guide.setMaterial(guideMat);
            guide.setTranslateX((i - 0.5) * CELL);
            guide.setTranslateY(-1.0 * CELL);
            guide.setTranslateZ(laneDepth / 2.0 - CAMERA_BACK);
            g.getChildren().add(guide);
        }

        return g;
    }
}
