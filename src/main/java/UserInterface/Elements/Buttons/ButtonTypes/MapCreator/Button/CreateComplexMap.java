package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Events;
import BeatSaberObjects.Objects.Note;
import DataManager.Parameters;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.PatternGeneration.CommonMethods.FixErrorsInPatterns;
import MapGeneration.PatternGeneration.CommonMethods.FixSwingTimings;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static DataManager.Parameters.logger;
import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;

public class CreateComplexMap extends MapCreatorSubButton {
    public CreateComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_COMPLEX_MAP_BUTTON, parent);
        logger.debug("CreateComplexMap button initialized.");
    }

    public void onClick() {
        List<BeatSaberMap> maps = new ArrayList<>();
        ui.manageMap();
        for (BeatSaberMap uiMap : ui.map) {
            UserInterface.currentDiff = uiMap.difficultyFileName;
            List <Note> notes = new ArrayList<>(Parameters.FIX_INCONSISTENT_TIMINGS
                    ? FixSwingTimings.fixSwingAlternating(List.of(uiMap._notes), ui)
                    : List.of(uiMap._notes));

            Pattern pattern = Pattern.adjustVariance(ui.pattern);

            if ((double) Arrays.stream(uiMap._notes).filter(note -> note._cutDirection == 8).count() / uiMap._notes.length >= 0.8) {
                logger.info("Timing Map found. Creating complex map from Pattern...");
                System.out.println("Timing Map found. Creating complex map from Pattern...");

                uiMap.toBlueLeftBottomRowDotTimings();
                notes.addAll(complexPatternFromTemplate(List.of(uiMap._notes), pattern, false, false, false, null, null));
                logger.debug("First note time in original map: {}", uiMap._notes[0]._time);
                logger.debug("First note time in complex pattern: {}", notes.get(0)._time);
            }
            else {
                System.out.println("Map Template found. Creating new map with the position of red & blue notes...");
                logger.info("Map Template found. Creating new map with the position of red & blue notes...");

                //Blue notes:
                notes.addAll(complexPatternFromTemplate(Arrays.stream(uiMap._notes).filter(note -> note._type == 1).toList(), pattern, true, false, false, null, null));
                logger.debug("Notes: {}. notes created from blue notes: {}", uiMap._notes.length, Arrays.stream(uiMap._notes).filter(note -> note._type == 0).toList().size());
                System.out.println("Notes: " + uiMap._notes.length + ". notes created from blue notes: " + Arrays.stream(uiMap._notes).filter(note -> note._type == 0).toList().size());

                // Red notes are just inverted blue notes
                notes.addAll(complexPatternFromTemplate(
                        Arrays.stream(uiMap._notes).filter(note -> note._type == 0).toList(), pattern, true, false, false, null, null
                ).stream().peek(Note::invertNote).toList());
                logger.debug("Notes: {}. notes created from red notes: {}", uiMap._notes.length, Arrays.stream(uiMap._notes).filter(note -> note._type == 1).toList().size());
                System.out.println("Notes: " + uiMap._notes.length + ". notes created from red notes: " + Arrays.stream(uiMap._notes).filter(note -> note._type == 1).toList().size());

                // Fix errors
                FixErrorsInPatterns.fixSimpleMappingErrors(notes);
            }/**/

            try {
                BeatSaberMap map = new BeatSaberMap(notes);
                map._events = Arrays.stream(uiMap._events).filter(event -> event._type == 100 || event._type == 1000 || event._type == 10000).toArray(Events[]::new);
                logger.debug("BPM-Events copied");
                maps.add(map);
                logger.debug("ADDED MAP. ui.map size: " + ui.map.size());
            }
            catch (IllegalArgumentException ex) {
                logger.error("Exception caught: {}", ex.getMessage());
                printException(ex);
            }
        }
        loadNewlyCreatedMaps(maps);
    }
}
