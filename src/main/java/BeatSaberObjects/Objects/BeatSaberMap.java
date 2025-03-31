package BeatSaberObjects.Objects;

import BeatSaberObjects.BeatsaberObject;
import DataManager.FileManager;
import com.google.gson.Gson;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static DataManager.Parameters.*;


import java.io.File;
import java.util.*;

    /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */

public class BeatSaberMap extends BeatsaberObject {
    public String originalJSON;
    public String _version = "2.2.0";
    public Events[] _events = new Events[0];
    public Note[] _notes;
    public Obstacle[] _obstacles = new Obstacle[0];
    public List<Bookmark> bookmarks = new ArrayList<>();
    public String difficultyFileName = "NULL";

    // <editor-fold desc="constructor methods">

    public BeatSaberMap(Note[] notes) {
        this._notes = notes;
    }

    public BeatSaberMap(Note[] notes, String originalJSON) {
        this._notes = notes;
        this.originalJSON = originalJSON;
        calculateBookmarks();
    }

    public BeatSaberMap(List<Note> notes) {
        this._notes = notes.toArray(new Note[0]);
    }

    public BeatSaberMap(List<Note> notes, Events[] events) {
        this._notes = notes.toArray(new Note[0]);
        this._events = events;
    }

    public BeatSaberMap(List<Note> notes, String originalJSON) {
        this._notes = notes.toArray(new Note[0]);
        this.originalJSON = originalJSON;
        calculateBookmarks();
    }

    // </editor-fold desc="constructor methods">

    public void convertToDDMap() {
        for (Note n : _notes) {
            n._cutDirection = 1;
            if (n._lineLayer == 2) {
                float random = RANDOM.nextFloat() * 100;
                if (random < 10) n._lineLayer = 2;
                if (random < 45) {
                    n._lineLayer = 1;
                    n._lineIndex = n._type == 1 ? 3 : 0;
                }
                if (random < 100) n._lineLayer = 0;
            }
        }
    }

    public void createBombResets() {
        List<Note> bombs = new ArrayList<>(List.of(_notes));
        for (int i = 0; i < _notes.length - 1; i++) {
            if (_notes[i]._time != _notes[i + 1]._time && _notes[i + 1]._time - _notes[i]._time >= (double) 1 / 8) {
                float timing = _notes[i]._time + ((_notes[i + 1]._time - _notes[i]._time) / 2);
                bombs.add(new Note(timing, 0, 0, 3, 1));
                bombs.add(new Note(timing, 1, 0, 3, 1));
                bombs.add(new Note(timing, 2, 0, 3, 1));
                bombs.add(new Note(timing, 3, 0, 3, 1));
            }
        }

        this._notes = bombs.toArray(new Note[0]);
    }

    public void deleteEverySecondNote() {
        List<Note> notes = new ArrayList<>(List.of(_notes));
        for (int i = notes.size() - 1; i >= 0; i--) {
            if (i % 2 != 0) {
                notes.remove(i);
            }
        }
        this._notes = notes.toArray(new Note[0]);
    }

    //Make the note timing divisible by 64, so ScoreSaber is not flagging that as "unsure".
    public void fixPlacements(double precision) {
        for (Note n : _notes) {
            n._time = Math.round(n._time / precision) * (float) precision;
        }
    }

    /**
     * Creates a new {@link BeatSaberMap} object from a JSON file specified by the file path.
     * The function reads the file, parses the JSON content, and processes the map data based on the version number.
     *
     * @param filePath the path to the JSON file containing the BeatSaberMap data.
     * @return a {@link BeatSaberMap} object. If the file is not found, the version is not supported, or any error occurs during parsing, an empty BeatSaberMap is returned.
     * <p>
     * The function behavior based on the map version is as follows:
     * - Version 1 and Version 4: Logs an error message indicating that these versions are not supported and returns an empty map.
     * - Version 2: Uses Gson to parse the entire JSON into a BeatSaberMap object, sets the original JSON, and calculates bookmarks.
     * - Version 3: Parses only the color notes from the JSON, sorts them by time, creates the BeatSaberMap with these notes, sets the version and original JSON, and calculates bookmarks.
     * - Default: Logs an error message indicating an unknown map version format and returns an empty map.
     * <p>
     * The function logs errors to the standard error stream if the file is not found, if unsupported or unknown map versions are encountered, or if the version number is not found in the JSON.
     * @see BeatSaberMap
     * @see Note
     * @see Bookmark
     */
    public static BeatSaberMap newMapFromJSON(String filePath) {
        File diffFile = new File(filePath);
        if (!diffFile.exists() || !diffFile.isFile()) {
            logger.error("Warning parsing BeatSaberMap from Json: File not found: {}. Skipping...", filePath);
            System.err.println("[INFO]: Warning parsing BeatSaberMap from Json: File not found: " + filePath + ". Skipping...");
            return new BeatSaberMap(new ArrayList<>());
        }
        if (diffFile.getName().contains("Lightshow")) {
            logger.error("Warning parsing newMapFromJSON: Lightshow maps are not supported. Skipping...");
            System.err.println("[INFO]: Warning parsing newMapFromJSON: Lightshow maps are not supported. Skipping...");
            return new BeatSaberMap(new ArrayList<>());
        }


        // Read and print the JSON content for debugging
        String jsonString = String.join("", FileManager.readFile(diffFile.getAbsolutePath()));

        JSONObject mapInfoJson;
        String versionNumber;

        try {
            mapInfoJson = new JSONObject(jsonString);
        } catch (JSONException e) {
            logger.error("Couldn't convert JSON. Something went wrong for the file: {}", filePath);
            System.err.println("[INFO]: Error parsing BeatSaberMap from Json: " + filePath);
            return new BeatSaberMap(new ArrayList<>());
        }


        try {
            versionNumber = mapInfoJson.getString("_version");
        } catch (JSONException e) {
            try {
                versionNumber = mapInfoJson.getString("version");
            } catch (JSONException e2) {
                logger.error("Error parsing BeatSaberMap from Json: Version number not found in the map file!");
                System.err.println("[ERROR]: Error parsing BeatSaberMap from Json: Version number not found in the map file!");
                return new BeatSaberMap(new ArrayList<>());
            }
        }

        switch (versionNumber.charAt(0)) {
            case '1' -> {
                logger.error("Error parsing BeatSaberMap from Json: Map Version format V1 is not supported!");
                System.err.println("[ERROR]: Error parsing BeatSaberMap from Json: Map Version format V1 is not supported!");
                return new BeatSaberMap(new ArrayList<>());
            }
            case '2' -> {
                BeatSaberMap map;
                try {
                    Gson gson = new Gson();
                    map = gson.fromJson(mapInfoJson.toString(), BeatSaberMap.class);
                    map.difficultyFileName = diffFile.getName();
                } catch (Exception e) {
                    logger.error("Couldn't convert into BeatSaberMap. Something went wrong!");
                    return new BeatSaberMap(new ArrayList<>());
                }

                map.originalJSON = jsonString;
                map.calculateBookmarks();

                return map;
            }
            case '3' -> {
                logger.info("Detected version 3 Map file format. Omitting Chains, Arcs, Events, Bombs and obstacles...");
                System.out.println("Detected version 3 Map file format. Omitting Chains, Arcs, Events, Bombs and obstacles...");

                //Parse Notes
                JSONArray notes = mapInfoJson.getJSONArray("colorNotes");
                List<Note> noteList = new ArrayList<>();
                for (int i = 0; i < notes.length(); i++) {
                    JSONObject note = notes.getJSONObject(i);
                    noteList.add(new Note(
                            note.optFloat("b", 0.0f),  // Default value of 0.0 if "b" not found
                            note.optInt("x", 0),       // Default value of 0 if "x" not found
                            note.optInt("y", 0),       // Default value of 0 if "y" not found
                            note.optInt("c", 0),       // Default value of 0 if "c" not found
                            note.optInt("d", 8)        // Default value of 0 if "d" not found
                    ));
                }

                noteList.sort(Comparator.comparingDouble(n -> n._time));

                //Create Map
                BeatSaberMap map = new BeatSaberMap(noteList);
                map.difficultyFileName = diffFile.getName();
                map._version = versionNumber;
                map.originalJSON = jsonString;
                map.calculateBookmarks();
                return map;
            }
            case '4' -> {
                logger.error("Error parsing BeatSaberMap from Json: Map Version format V4 is not supported yet!");
                System.err.println("[ERROR]: Error parsing BeatSaberMap from Json: Map Version format V4 is not supported yet!");
                return new BeatSaberMap(new ArrayList<>());
            }
            default -> {
                logger.error("Error parsing BeatSaberMap from Json: Unknown Map Version format: {}", versionNumber);
                System.err.println("[ERROR]: Error parsing BeatSaberMap from Json: Unknown Map Version format: " + versionNumber);
            }
        }
        return new BeatSaberMap(new ArrayList<>());
    }

    public BeatSaberMap setOriginalJson(String originalJSON) {
        this.originalJSON = originalJSON;
        calculateBookmarks();
        return this;
    }

    public void convertAllFlashLightsToOnLights() {
        for (Events e : _events) {
            e.convertFlashLightsToOnLights();
        }
    }

    //removes all notes of Type "removeType"
    public void makeOneHanded(int removeType) {
        List<Note> notes = new ArrayList<>();
        for (Note n : _notes) {
            if (n._type != removeType) {
                notes.add(n);
            }
        }
        Note[] n = new Note[notes.size()];
        for (int i = 0; i < notes.size(); i++) {
            n[i] = notes.get(i);
        }

        this._notes = n;
    }

    public void makeNoArrows() {
        for (Note n : _notes) {
            n._cutDirection = 8;
        }
    }

    //This function is only here to make the List in a List into a one-dimensional array, so that it is compatible with
    //the other functions
    public void toTimingNotes() {

        List<List<Note>> note = mapToTimingNotesAsList(_notes);
        List<Note> list = new ArrayList<>();

        //traversing every note
        for (List<Note> l : note) {
            list.addAll(l);
        }

        //returning the List as an Array
        this._notes = list.toArray(new Note[0]);
    }

    /**
     * This function takes all the notes of a map and converts it to a List inside a List in which all the notes are saved
     * as a dot on the leftmost lane.
     * If there are more notes on the same beat, then the notes are being converted into stacks
     * Red Notes are only created if there is a blue and a red note on the same beat. They are saved on the second lane
     * BeatSaberObjects.Objects.Note that there can only be a maximum of six Notes in one Beat or else the script will not create a seventh note;
     *
     * @param notes The notes of the map
     * @return A List of all Notes. If there are more notes on the same beat, then they are being saved in a List inside the List
     */
    private static List<List<Note>> mapToTimingNotesAsList(Note[] notes) {
        //Here is a List, where all grids are being saved.
        List<List<Note>> timings = new ArrayList<>(List.of(new ArrayList<>(List.of(new Note(notes[0]._time, notes[0]._type == 0 ? 1 : 0, 0, notes[0]._type, 8)))));

        for (Note n : notes) {
            //the first note must be set manually
            if (notes[0] == n) continue;

            //retrieving the grid
            List<Note> grid = timings.get(timings.size() - 1);

            //if grid exists
            if (grid.get(0)._time == n._time) {
                int ctBlue = 0;
                int ctRed = 0;
                for (Note note : grid) {
                    if (note._type == 0) ctRed++;
                    if (note._type == 1) ctBlue++;
                }

                //It will only create a note, when there are up to 6 notes already saved.
                //It will create it in the desired lane: Red lane 1; Blue lane 0
                Note newNote = new Note(n._time, n._type == 0 ? 1 : 0, n._type == 0 ? ctRed : ctBlue, n._type, 8);
                if (ctBlue < 3 && ctRed < 3) grid.add(newNote);
            } else {
                //creating a new grid
                Note newNote = new Note(n._time, n._type == 0 ? 1 : 0, 0, n._type, 8);
                timings.add(new ArrayList<>(List.of(newNote)));
            }
        }

        //When there is only one red note, then we will be converting this red note into a blue one.
        // (It makes copying someone else's map way harder.)
        for (List<Note> l : timings) {
            if (l.size() == 1) {
                l.get(0)._lineIndex = 0;
                l.get(0)._type = 1;
            }
        }
        return timings;
    }

    //This function makes timings from a map.
    //Every note is converted into a blue dot block on the leftmost lane
    //WARNING: If there is more than 1 note in the same beat, then all but one are erased (for example stacks)
    //If you want to keep them, then have a look at "mapToTimingNotesArray" or "mapToTimingNotesList"
    public void toBlueLeftBottomRowDotTimings() {
        _events = new Events[0];
        Note[] timings = new Note[_notes.length];
        int numberOfNulls = 0;

        //traversing every note
        for (int i = 0; i < _notes.length; i++) {

            if (_notes[i]._type != 1 && _notes[i]._type != 0) {
                numberOfNulls++;
                continue;
            }
            //when the note exists, then DON'T place another one on top of it
            if (i >= 1 && (_notes[i - 1]._time == _notes[i]._time || _notes[i]._time - _notes[i - 1]._time <= (float) 1 / 8)) {
                if (timings[i - 1] != null) timings[i - 1].amountOfStackedNotes++;
//                else System.err.println("this one is null: " + timings[i-1] + " - " + numberOfNulls);
                numberOfNulls++;
                continue;
            }

            //else:
            timings[i] = new Note(_notes[i]._time);
        }

        //Since there may be null values, we need to remove them
        Note[] toReturn = new Note[_notes.length - numberOfNulls];
        int ct = 0;
        for (Note n : timings) {
            if (n != null) {
                toReturn[ct] = n;
                ct++;
            }
        }

        this._notes = toReturn;
        this._obstacles = new Obstacle[0];
    }

    /**
     * Calculates and returns a list of bookmarks from the original JSON string.
     * The function processes the JSON data based on the map version specified by the first character of the _version string.
     *
     * @return a list of {@link Bookmark} objects. If the original JSON is null or doesn't contain the expected bookmarks structure,
     * an empty list is returned.
     * <p>
     * The function behavior based on the map version is as follows:
     * - Version 1 and Version 4: Logs an error message indicating that these versions are not supported and returns an empty list.
     * - Version 2: Parses the bookmarks from the "_customData" section of the JSON. Supports bookmarks with both RGBA and RGB color formats.
     * - Version 3: Parses the bookmarks from the "customData" section of the JSON. Supports bookmarks with RGBA color format.
     * - Default: Logs an error message indicating an unknown map version format and returns an empty list.
     * <p>
     * The Bookmark objects are created with the following attributes extracted from the JSON:
     * - Time of the bookmark.
     * - Name of the bookmark.
     * - Color array, which defaults to [0, 0, 0] if not provided or if any error occurs during parsing.
     * <p>
     * The function logs errors to the standard error stream if unsupported or unknown map versions are encountered.
     * @see Bookmark
     */
    public List<Bookmark> calculateBookmarks() {
        if (this.originalJSON == null) return new ArrayList<>();
        if (!this.originalJSON.contains("\"_bookmarks\":[")) return new ArrayList<>();

        List<Bookmark> l = new ArrayList<>();
        JSONObject json = new JSONObject(this.originalJSON);

        JSONArray bookmarks = getBookmarksArray(json);
        if (bookmarks == null) {
            logger.warn("Bookmarks not found in the JSON. Skipping bookmarks...");
            System.err.println("[ERROR]: Error calculating Bookmarks: Bookmarks not found in the JSON. Skipping bookmarks...");
            return new ArrayList<>();
        }

        switch (_version.charAt(0)) {
            case '1', '4' -> {
                logger.error("Error calculating Bookmarks: Map Version format V{} is not supported! Ignoring bookmarks...", _version.charAt(0));
                System.err.println("[ERROR]: Error calculating Bookmarks: Map Version format V" + _version.charAt(0) + " is not supported! Ignoring bookmarks...");
            }
            case '2', '3' -> {
                for (int i = 0; i < bookmarks.length(); i++)
                    if (!addBookmarkToList(bookmarks.getJSONObject(i), l)) return new ArrayList<>();
            }
            default -> {
                logger.error("Error calculating Bookmarks: Unknown Map Version format: {}", _version);
                System.err.println("[ERROR]: Error calculating Bookmarks: Unknown Map Version format: " + _version);
            }
        }
        this.bookmarks = l;
        return l;
    }

    private boolean addBookmarkToList(JSONObject bookmark, List<Bookmark> l) {
        try {
            l.add(new Bookmark(bookmark.getFloat("b"), bookmark.getString("n"), extractColor(bookmark)));
        } catch (JSONException e) {
            try {
                l.add(new Bookmark(bookmark.getFloat("_time"), bookmark.getString("_name"), extractColor(bookmark)));
            } catch (Exception e1) {
                logger.warn("Couldn't calculate Bookmarks: Error parsing bookmark color: " + e1.getMessage() + ". Skipping bookmarks...");
                System.err.println("[ERROR]: Error calculating Bookmarks: Error parsing bookmark color: " + e1.getMessage() + ". Skipping bookmark...");
                return false;
            }
        }
        return true;
    }

    /**
     * Extracts the color array from a bookmark JSONObject.
     * The function attempts to retrieve the color array from the "_color" key, then the "c" key, and defaults to [0, 0, 0] if not found.
     * The color array is returned as a float array containing the RGB values.
     *
     * @param bookmark The JSONObject containing the bookmark data.
     * @return a float array containing the RGB color values extracted from the JSONObject.
     */
    private float[] extractColor(JSONObject bookmark) {
        try {
            JSONArray color = bookmark.optJSONArray("_color");
            if (color == null) color = bookmark.optJSONArray("c");
            if (color == null) return new float[]{0, 0, 0}; // Default color if not found

            float[] colorArray = new float[color.length()];
            for (int j = 0; j < color.length(); j++) {
                colorArray[j] = (float) color.getDouble(j);
            }
            return colorArray;
        } catch (JSONException e) {
            return new float[]{0, 0, 0}; // Default color on error
        }
    }

    /**
     * Retrieves the bookmarks array from a JSONObject by trying multiple possible paths.
     *
     * @param json The root JSONObject.
     * @return The JSONArray containing the bookmarks data, or null if the bookmarks are not found.
     */
    private static JSONArray getBookmarksArray(JSONObject json) {
        // Define possible JSON paths where bookmarks may be stored
        String[] possiblePaths = new String[]{
                "_bookmarks", // Direct path for some formats
                "_customData._bookmarks", // Nested inside _customData
                "customData._bookmarks",
                "_customData.bookmarks",
                "customData.bookmarks"
        };

        // Attempt to retrieve the JSONArray from one of the specified paths
        for (String path : possiblePaths) {
            try {
                return getJSONArrayByPath(json, path);
            } catch (JSONException ignore) {
                // Continue trying other paths
            }
        }

        logger.warn("Warning: Bookmarks not found. Skipping... (Path not found in JSON structure(?).)");
        System.err.println("[INFO]: Warning: Bookmarks not found. Skipping... (Path not found in JSON structure.)");
        return null;
    }

    /**
     * Retrieves a JSONArray from a JSONObject following a specified path.
     *
     * @param json The root JSONObject.
     * @param path A dot-separated string specifying the path to the JSONArray.
     * @return The JSONArray located at the specified path within the JSONObject.
     * @throws JSONException If the path is invalid or if any segment of the path does not lead to a JSONObject.
     */
    private static JSONArray getJSONArrayByPath(JSONObject json, String path) throws JSONException {
        String[] keys = path.split("\\.");
        JSONObject current = json;

        // Traverse the JSONObject hierarchy according to the path
        for (int i = 0; i < keys.length - 1; i++) {
            current = current.getJSONObject(keys[i]);
        }

        // Retrieve and return the JSONArray at the final segment of the path
        return current.getJSONArray(keys[keys.length - 1]);
    }

// <editor-fold desc="override methods">

    @Override
    public String toString() {
        return "\"_notes\":" + Arrays.toString(_notes);
    }

    public String exportAsV3Map() {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"version\":\"3.3.0\",");
        json.append("\"bpmEvents\":[],");
        json.append("\"rotationEvents\":[],");


        //Notes
        json.append("\"colorNotes\":[");
        for (Note n : _notes) {
            json.append(n.toV3String()).append(",");
        }
        if (_notes.length > 0) json = new StringBuilder(json.substring(0, json.length() - 1));
        json.append("],");

        json.append("\"bombNotes\":[],");
        json.append("\"obstacles\":[],");
        json.append("\"sliders\":[],");
        json.append("\"burstSliders\":[],");
        json.append("\"waypoints\":[],");
        json.append("\"basicBeatmapEvents\":[],");
        json.append("\"colorBoostBeatmapEvents\":[],");
        json.append("\"lightColorEventBoxGroups\":[],");
        json.append("\"lightRotationEventBoxGroups\":[],");
        json.append("\"lightTranslationEventBoxGroups\":[],");
        json.append("\"vfxEventBoxGroups\":[],");
        json.append("\"_fxEventsCollection\":{\"_il\":[],\"_fl\":[]},");
        json.append("\"basicEventTypesWithKeywords\":{\"d\":[]},");
        json.append("\"useNormalEventsAsCompatibleEvents\":true,");


        //Bookmarks
        json.append("\"customData\":{\"bookmarks\":[");
        for (Bookmark b : bookmarks) {
            json.append("{\"b\":").append(b._time).append(",\"n\":\"").append(b._name).append("\",\"c\":").append(Arrays.toString(b._color)).append("},");
        }
        if (!bookmarks.isEmpty()) json = new StringBuilder(json.substring(0, json.length() - 1));
        json.append("]},");

        json.append("}");

        json = new StringBuilder(json.toString().replaceAll(" ", "")
                .replaceAll("\\.0,", ",")
                .replaceAll("\\.0]", "]")
                .replaceAll("\\.0}", "}")
                .replace("\n", ""));


        return json.toString();
    }

    public String exportAsMap() {
        String json = "";
        json += "{";
        json += "\"_version\":\"2.2.0\",";
        json += "\"_notes\":" + (_notes == null ? "[]" : Arrays.toString(_notes)) + ",";
        json += "\"_obstacles\":" + (_obstacles == null ? "[]" : Arrays.toString(_obstacles)) + ",";
        json += "\"_events\":" + (_events == null ? "[]" : Arrays.toString(_events)) + ",";
        json += "\"_waypoints\":[]";
        if (bookmarks != null && !bookmarks.isEmpty()) {
            json += ",\"_customData\":{";
            json += "\"_bookmarks\":" + bookmarks;
            json += "}";
        }
        json += "}";

        json = json.replaceAll(" ", "")
                .replaceAll("\\.0,", ",")
                .replaceAll("\\.0]", "]")
                .replaceAll("\\.0}", "}")
                .replace("\n", "");

        return json;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeatSaberMap that = (BeatSaberMap) o;

        return _version.equals(that._version) &&
                Arrays.equals(_events, that._events) &&
                Arrays.equals(_notes, that._notes) &&
                Arrays.equals(_obstacles, that._obstacles) &&
                Objects.equals(bookmarks, that.bookmarks);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(originalJSON, _version, bookmarks);
        result = 31 * result + Arrays.hashCode(_events);
        result = 31 * result + Arrays.hashCode(_notes);
        result = 31 * result + Arrays.hashCode(_obstacles);
        return result;
    }
// </editor-fold desc="override methods">

    // <editor-fold desc="Getter & Setter">
    
    public Note [] getNotes() {
        return _notes;
    }
    
    // </editor-fold desc="Getter & Setter">
    
}

    /*
    Red: 0
    Blue: 1

    Index - Layer:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */







