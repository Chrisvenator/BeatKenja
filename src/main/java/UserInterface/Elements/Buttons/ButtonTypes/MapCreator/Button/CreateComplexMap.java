package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Events;
import BeatSaberObjects.Objects.Note;
import MapGeneration.GenerationElements.Pattern;
import MapGeneration.PatternGeneration.CommonMethods.FixErrorsInPatterns;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;
import static DataManager.Parameters.logger;

public class CreateComplexMap extends MapCreatorSubButton {
    public CreateComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_COMPLEX_MAP_BUTTON, parent);
        logger.debug("CreateComplexMap button initialized.");
    }

    public void onClick() {
        ui.manageMap();
        List<Note> notes = new ArrayList<>();
        Pattern pattern = Pattern.adjustVariance(ui.pattern);

        if ((double) Arrays.stream(ui.map._notes).filter(note -> note._cutDirection == 8).count() / ui.map._notes.length >= 0.8) {
            logger.info("Timing Map found. Creating complex map from Pattern...");

            ui.map.toBlueLeftBottomRowDotTimings();
            notes.addAll(complexPatternFromTemplate(List.of(ui.map._notes), pattern, false, false, false, null, null));
            logger.debug("First note time in original map: {}", ui.map._notes[0]._time);
            logger.debug("First note time in complex pattern: {}", notes.get(0)._time);
        } else {
            logger.info("Map Template found. Creating new map with the position of red & blue notes...");

            //Blue notes:
            notes.addAll(complexPatternFromTemplate(Arrays.stream(ui.map._notes).filter(note -> note._type == 1).toList(), pattern, true, false, false, null, null));
            logger.debug("{} notes created from blue notes {}", notes.size(), Arrays.stream(ui.map._notes).filter(note -> note._type == 0).toList().size());

            // Red notes are just inverted blue notes
            notes.addAll(complexPatternFromTemplate(
                    Arrays.stream(ui.map._notes).filter(note -> note._type == 0).toList(), pattern, true, false, false, null, null
                    ).stream().peek(Note::invertNote).toList());
            logger.debug("{} notes created from red notes {}", notes.size(), Arrays.stream(ui.map._notes).filter(note -> note._type == 1).toList().size());

            // Fix errors
            FixErrorsInPatterns.fixSimpleMappingErrors(notes);
        }

        try {
            BeatSaberMap map = new BeatSaberMap(notes);
            map._events = Arrays.stream(ui.map._events).filter(event -> event._type == 100 || event._type == 1000 || event._type == 10000).toArray(Events[]::new);
            logger.info("BPM-Events copied");
            loadNewlyCreatedMap(map);
        } catch (IllegalArgumentException ex) {
            logger.error("Exception caught: {}", ex.getMessage());
            printException(ex);
        }
    }
}
