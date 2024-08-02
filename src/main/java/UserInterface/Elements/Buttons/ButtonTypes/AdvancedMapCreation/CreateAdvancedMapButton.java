package UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation;

import BeatSaberObjects.Objects.BeatSaberMap;
import DataManager.Records.PatMetadata;
import MapGeneration.GenerationElements.Pattern;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.TextFields.CreateAdvancedMapBPM;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreation.TextFields.CreateAdvancedMapNPS;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Exceptions.MapDidntComputeException;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static DataManager.Parameters.logger;
import static DataManager.Parameters.verbose;
import static MapGeneration.PatternGeneration.AdvancedComplexMap.createAdvancedComplexPattern;

/**
 * Button to create an advanced Beat Saber map. This class handles user input,
 * manages genres, tags, and difficulties, and triggers the map creation process.
 */
public class CreateAdvancedMapButton extends MapCreatorSubButton {
    public static final List<String> genres = new ArrayList<>();
    public static final List<String> tags = new ArrayList<>();
    public static final List<String> difficulties = new ArrayList<>();
    private final CreateAdvancedMapNPS npsField;
    private final CreateAdvancedMapBPM bpmField;


    /**
     * Constructs the button for creating an advanced Beat Saber map.
     * Initializes default values for genres, tags, and difficulties.
     *
     * @param parent   the parent button
     * @param npsField the text field for notes per second (NPS) input
     * @param bpmField the text field for beats per minute (BPM) input
     */
    public CreateAdvancedMapButton(MyButton parent, CreateAdvancedMapNPS npsField, CreateAdvancedMapBPM bpmField) {
        super(ElementTypes.ADVANCED_MAP_CREATOR_CREATE_MAP_BUTTON, parent);
        genres.add("NULL");
        genres.add("NULL");
        tags.add("NULL");
        tags.add("NULL");
        difficulties.add("StandardExpertPlus");

        this.npsField = npsField;
        this.bpmField = bpmField;
        logger.debug("CreateAdvancedMapButton initialized.");
    }

    /**
     * Called when the button is clicked. This method manages the map creation process,
     * handling input validation, error checking, and generating the advanced map.
     */
    @Override
    public void onClick() {
        ui.manageMap();
        Arrays.stream(ui.map._notes).forEach(note -> note._cutDirection = 8);
//        ui.pattern.visualizeAsHeatmap();
//        ui.pattern.visualizeAsHeatmapNormalized("");
//        ui.pattern.visualizeAsHeatmapNormalizedLogarithmically();
//        ui.pattern.visualizeAsHeatmapTruncated();

        try {
            int nps = Objects.equals(npsField.getText(), "nps") ? 4 : Integer.parseInt(npsField.getText());
            int bpm = Objects.equals(bpmField.getText(), "bpm") ? 120 : Integer.parseInt(bpmField.getText());

            logger.info("Settings for Advanced Complex Map: Genres: {}, Tags: {}, Difficulty: {}, NPS: {}, BPM: {}", genres, tags, difficulties, nps, bpm);

            logger.debug("Original map: {}", ui.map.exportAsMap());

            // Create the advanced Beat Saber map using the specified parameters and patterns
            BeatSaberMap map = new BeatSaberMap(
                    createAdvancedComplexPattern(
                            Arrays.stream(ui.map._notes).toList(),
                            Pattern.adjustVariance(ui.pattern),
                            false,
                            true,
                            null,
                            null,
                            new PatMetadata("",
                                    bpm,
                                    nps,
                                    difficulties,
                                    tags,
                                    genres
                            )
                    )
            );

            // Check for unplaced notes and inform the user if there are too many errors
            int unplacedNotes = map.exportAsMap().split("\"_cutDirection\":8").length;
            if (unplacedNotes >= 20) {
                String warningMessage = "There are " + unplacedNotes + " error Notes. You will have to clean them manually. Do you really want to continue? It is recommended to try again";
                logger.warn(warningMessage);
                JOptionPane.showMessageDialog(null, warningMessage, "Too many errors Info", JOptionPane.INFORMATION_MESSAGE);
            }

            // Check if the map computation succeeded
            if (map.equals(ui.map)) {
                logger.error("The map is the same as the original map! Please try again");
                throw new MapDidntComputeException("Something went wrong Map didn't compute...");
            }
            else loadNewlyCreatedMap(map);

            logger.debug("New map: {}", map.exportAsMap());

        } catch (NumberFormatException e) {
            logger.error("Please enter valid numbers for NPS and BPM. Exception: {}", e.getMessage());
            JOptionPane.showMessageDialog(null, "Please enter valid numbers for NPS and BPM.", "Input Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException | MapDidntComputeException ex) {
            printException(ex);
        }

    }
}
