package UserInterface.Elements;

import DataManager.Parameters;
import UserInterface.Elements.Buttons.ButtonType;
import UserInterface.Elements.JSlider.SliderTypes;
import UserInterface.Elements.TextFields.TextFieldType;

import javax.swing.*;

/** Defines all Values for all Types of UI Elements */
public class ElementTypes {
    //Button Types
    public static final ButtonType MAP_CREATOR_BUTTON = new ButtonType("Map creator", 650, 200, 190, 30, false);
    public static final ButtonType MAP_CREATOR_CREATE_MAP_BUTTON = new ButtonType("Create Map", 650, 180, 190, 15, false);
    public static final ButtonType MAP_CREATOR_CREATE_COMPLEX_MAP_BUTTON = new ButtonType("Create Complex Map", 650, 160, 190, 15, false);
    public static final ButtonType MAP_CREATOR_CREATE_LINEAR_MAP_BUTTON = new ButtonType("Create Linear Map", 650, 140, 190, 15, false);
    public static final ButtonType MAP_CREATOR_CREATE_ONE_HANDED_COMPLEX_MAP_BUTTON = new ButtonType("complex", 750, 120, 90, 15, false);
    public static final ButtonType MAP_CREATOR_CREATE_RANDOM_MAP_V2_BUTTON = new ButtonType("rand. V2", 750, 100, 90, 15, false);
    public static final ButtonType MAP_CREATOR_CREATE_RANDOM_MAP_BUTTON = new ButtonType("random", 650, 100, 90, 15, false);
    public static final ButtonType MAP_CREATOR_CREATE_ONE_HANDED_SIMPLE_LINEAR_MAP_BUTTON = new ButtonType("one handed simple linear", 650, 120, 90, 15, false);

    //Advanced Map Creator Button Types
    public static final ButtonType ADVANCED_MAP_CREATOR_BUTTON = new ButtonType("Advanced Map creator", 850, 200, 190, 30, false);
    public static final ButtonType ADVANCED_MAP_CREATOR_CREATE_MAP_BUTTON = new ButtonType("Create Advanced Map",850, 180, 190, 15, false);
    public static final ButtonType ADVANCED_MAP_CREATOR_SET_GENRES =        new ButtonType("select Genre 1",     850, 140, 140, 15, false);
    public static final ButtonType ADVANCED_MAP_CREATOR_SET_GENRES_2 =      new ButtonType("select Genre 2",     850, 160, 140, 15, false);
    public static final ButtonType ADVANCED_MAP_CREATOR_SET_TAGS =          new ButtonType("select Tag 1",       850, 100, 140, 15, false);
    public static final ButtonType ADVANCED_MAP_CREATOR_SET_TAGS_2 =        new ButtonType("select Tag 2",       850, 120, 140, 15, false);
    public static final ButtonType ADVANCED_MAP_CREATOR_SET_Difficulty =    new ButtonType("select Difficulty",  850,  80, 140, 15, false);
    public static final TextFieldType ADVANCED_MAP_CREATOR_BPM = new TextFieldType("bpm", 1000, 140, 40, 15, false);
    public static final TextFieldType ADVANCED_MAP_CREATOR_NPS = new TextFieldType("nps", 1000, 160, 40, 15, false);



    //To Timing Notes Button Types
    public static final ButtonType TIMING_NOTES_TO_TIMING_NOTES = new ButtonType("Map to timing Notes", 450, 200, 190, 30, false);
    public static final ButtonType TIMING_NOTES_TO_BLUE_ONLY_TIMING_NOTES = new ButtonType("To 1 color timing notes", 450, 180, 190, 15, false);
    public static final ButtonType TIMING_NOTES_TO_2_COLOR_TIMING_NOTES = new ButtonType("To 2 color timing notes", 450, 160, 190, 15, false);

    //Map Utilities Button Types
    public static final ButtonType MAP_UTILITIES_MAP_UTILS_BUTTON = new ButtonType("Map Utilities", 250, 200, 190, 30, false);
    public static final ButtonType MAP_UTILITIES_FIX_PLACEMENTS_BUTTON = new ButtonType("Fix Placements", 250, 180, 145, 15, false);
    public static final TextFieldType MAP_UTILITIES_FIX_PLACEMENTS_TEXT_FIELD = new TextFieldType(1 / Parameters.PLACEMENT_PRECISION + "", 400, 180, 40, 15, false);
    public static final ButtonType MAP_UTILITIES_DELETE_NOTE_TYPES_BUTTON = new ButtonType("Delete Note Type", 250, 160, 145, 15, false);
    public static final TextFieldType MAP_UTILITIES_DELETE_NOTE_TYPES_TEXT_FIELD = new TextFieldType("0", 400, 160, 40, 15, false);
    public static final ButtonType MAP_UTILITIES_CONVERT_ALL_FLASHING_LIGHTS_BUTTON = new ButtonType("Convert All FlashingLight", 250, 140, 190, 15, false);
    public static final ButtonType MAP_UTILITIES_MAKE_NO_ARROW_MAP_BUTTON = new ButtonType("Make into no arrow map", 250, 120, 190, 15, false);

    //Map Checks
    public static final ButtonType MAP_CHECKS_MAP_CHECKS = new ButtonType("Map Checks", 850, 200, 190, 30, false);

    //Global Button Types
    public static final ButtonType GLOBAL_SAVE_MAP_AS = new ButtonType("SAVE MAP AS", 750, 50, 150, 50, false);
    public static final ButtonType GLOBAL_OPEN_MAP_IN_BROWSER = new ButtonType("Open Map in Browser", 975, 70, 150, 50, false);
    public static final ButtonType GLOBAL_OPEN_MAP = new ButtonType("click here and select your desired difficulty", 200, 20, 100, 30, true);
    public static final ButtonType GLOBAL_CONVERT_MP3s = new ButtonType("Convert MP3s to timing maps", 500, 20, 200, 30, true);
    public static final ButtonType GLOBAL_OPEN_FOLDER = new ButtonType("open folder", 705, 22, 110, 26, true);
    public static final ButtonType GLOBAL_LOAD_PATTERNS_BUTTON = new ButtonType("Load Patterns File", 270, 70, 140, 30, true);
    public static final ButtonType GLOBAL_SHOW_PATTERNS_BUTTON = new ButtonType("Visualize Pattern", 275, 105, 130, 15, true);
//    public static final ButtonType GLOBAL_SHOW_PATTERNS_BUTTON = new ButtonType("Show", 415, 75, 70, 20, true);

    //Global Text Field Types
    public static final TextFieldType GLOBAL_SEED_FIELD = new TextFieldType(String.valueOf(Parameters.SEED), 1000, 20, 100, 20, true);
    public static final TextFieldType GLOBAL_BPM_FIELD = new TextFieldType(String.valueOf(Parameters.BPM), 100, 50, 100, 20, true);
    public static final SliderTypes GLOBAL_PATTERN_VARIANCE_SLIDER = new SliderTypes(JSlider.HORIZONTAL, 50, 70, 200, 50, true, -50, 50, 0);
}
