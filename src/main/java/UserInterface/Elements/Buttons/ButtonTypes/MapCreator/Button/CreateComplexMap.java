package UserInterface.Elements.Buttons.ButtonTypes.MapCreator.Button;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Events;
import BeatSaberObjects.Objects.Note;
import MapGeneration.PatternGeneration.CommonMethods.FixErrorsInPatterns;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreator.MapCreatorSubButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.ElementTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static MapGeneration.ComplexPatternFromTemplate.complexPatternFromTemplate;

public class CreateComplexMap extends MapCreatorSubButton {
    public CreateComplexMap(MyButton parent) {
        super(ElementTypes.MAP_CREATOR_CREATE_COMPLEX_MAP_BUTTON, parent);
    }

    public void onClick() {
        ui.manageMap();
        List<Note> notes = new ArrayList<>();
        if ((double) Arrays.stream(ui.map._notes).filter(note -> note._cutDirection == 8).count() / ui.map._notes.length >= 0.8) {
            System.out.println("Timing Map found. Creating complex map from Pattern...");
            ui.statusCheck.append("Timing Map found. Creating complex map from Pattern...\n");


            ui.map.toBlueLeftBottomRowDotTimings();
            notes.addAll(complexPatternFromTemplate(List.of(ui.map._notes), ui.pattern, false, false, false,null, null));
            System.out.println(ui.map._notes[0]._time);
            System.out.println(notes.get(0)._time);
        } else {
            System.out.println("Map Template found. Creating new map with the position of red & blue notes...");
            ui.statusCheck.append("Map Template found. Creating new map with the position of red & blue notes...\n");

            notes.addAll(complexPatternFromTemplate(Arrays.stream(ui.map._notes).filter(note -> note._type == 1).toList(), ui.pattern, true, false, false,null, null));
            System.out.println(notes.size() + " notes created from blue notes " + Arrays.stream(ui.map._notes).filter(note -> note._type == 0).toList().size());

            //Red notes are inverted blue notes
            notes.addAll(complexPatternFromTemplate(
                    Arrays.stream(ui.map._notes).filter(note -> note._type == 0).toList(), ui.pattern, true, false, false,null, null)
                    .stream().peek(Note::invertNote).toList());
            System.out.println(notes.size() + " notes created from red notes " + Arrays.stream(ui.map._notes).filter(note -> note._type == 1).toList().size());

            //Fix errors
            FixErrorsInPatterns.fixSimpleMappingErrors(notes);
        }

        try {
            BeatSaberMap map = new BeatSaberMap(notes);
//            map._events = ui.map._events;
            map._events = Arrays.stream(ui.map._events).filter(event -> event._type == 100 || event._type == 1000 || event._type == 10000).toArray(Events[]::new);
//            System.out.println(Arrays.toString(map._events));

            loadNewlyCreatedMap(map);

        } catch (IllegalArgumentException ex) {
            printException(ex);
        }
    }
}
