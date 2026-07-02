package UserInterface;

import AppLogic.AppController;
import AppLogic.AppState;
import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Bookmark;
import DataManager.Logger.GuiAppender;
import UserInterface.Elements.Buttons.ButtonTypes.AdvancedMapCreatorButton;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButton;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.GlobalOpenMapInBrowser;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons.GlobalSaveMapAs;
import UserInterface.Elements.Buttons.ButtonTypes.MapCreatorButton;
import UserInterface.Elements.Buttons.ButtonTypes.MapUtilitiesButton;
import UserInterface.Elements.Buttons.ButtonTypes.ToTimingNotesButton;
import UserInterface.Elements.Buttons.MyButton;
import UserInterface.Elements.JSlider.GlobalJSlider.GlobalPatternVarianceJSlider;
import UserInterface.Elements.JSlider.MyGlobalJSlider;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static DataManager.Parameters.DARK_MODE;
import static DataManager.Parameters.DEFAULT_PATH;
import static DataManager.Parameters.ignoreDDs;
import static DataManager.Parameters.logger;
import static DataManager.Parameters.saveNewMapsToDefaultPath;
import static DataManager.Parameters.verbose;

/**
 * The `UserInterface` class is responsible for initializing and managing the graphical user interface (GUI) components of the application.
 * All map state and operations live in the AppController; this class only builds the Swing widgets
 * and reacts to controller state changes (e.g. showing the category buttons once a map is loaded).
 */
public class UserInterface extends JFrame {

    /** The UI-independent application service holding the loaded maps, pattern, and operations. */
    public final AppController controller = new AppController();
    /** Live view of all loaded map difficulties. Backed by the controller's session. */
    public final List<BeatSaberMap> map;
    /** A label that displays the current status of whether the map is successfully loaded.*/
    public final JLabel labelMapDiff;
    /** A text pane that acts as a log, displaying important events within the GUI.*/
    public final StatusCheckTextPane statusCheck;
    /** The topmost, invisible button that serves as a parent for all global buttons. This button cannot be pressed and provides a category for "default/global" buttons.*/
    public GlobalButton globalButton;

    /**
     * Constructs and initializes the user interface, setting up all graphical elements and their respective event listeners.
     * Category buttons become visible via a controller state listener as soon as a map is loaded.
     */
    public UserInterface() {
        map = controller.maps();

        //<editor-fold desc="Initialize UI Elements">
        //////////////////////////////
        //  Initialize UI Elements  //
        //////////////////////////////

        final UIElements uiElements = new UIElements(DARK_MODE, this);
        uiElements.initialize();

        labelMapDiff = uiElements.labelMapDiff();
        statusCheck = uiElements.statusTextArea();
        GuiAppender.setUserInterface(this);
        JCheckBox ignoreDDsCheckBox = uiElements.ignoreDDsCheckbox();

        globalButton = new GlobalButton(this);
        globalButton.init();
        GlobalSaveMapAs saveMapButton = new GlobalSaveMapAs(this);
        GlobalOpenMapInBrowser openMapInBrowserButton = new GlobalOpenMapInBrowser(this);
        MyGlobalJSlider globalPatternVarianceJSlider = new GlobalPatternVarianceJSlider(this);

        MapCreatorButton showMapCreatorButton = new MapCreatorButton(this);
        AdvancedMapCreatorButton advancedMapCreatorButton = new AdvancedMapCreatorButton(this);
        MyButton toTimingNotes = new ToTimingNotesButton(this);
        MyButton utilsMapUtilsButton = new MapUtilitiesButton(this);
        //</editor-fold desc="Initialize UI Elements">

        //<editor-fold desc="Event Listener">
        /////////////////////
        //  Event Listener //
        /////////////////////

        //global
        statusCheck.append("config: \nverbose: " + verbose + "\npath to WIP-Folder: " + DEFAULT_PATH + "\ndark mode:" + DARK_MODE + "\nsave new maps to WIP folder (default path): " + saveNewMapsToDefaultPath + "\n\n");
        ignoreDDsCheckBox.addActionListener(e -> statusCheck.append("\n[INFO]: ignore DDs: " + (ignoreDDs = ignoreDDsCheckBox.isSelected())));

        // Show the category buttons as soon as a map is loaded (event-driven; replaces the old polling thread)
        controller.addListener(new AppController.Listener() {
            @Override
            public void onBpmChanged(double bpm) {
                SwingUtilities.invokeLater(() -> globalButton.globalBPMField.setBPM(bpm));
            }

            @Override
            public void onStateChanged(AppState state) {
                boolean mapLoaded = state != AppState.EMPTY;
                SwingUtilities.invokeLater(() -> {
                    if (mapLoaded) labelMapDiff.setBackground(Color.GREEN);

                    showMapCreatorButton.setVisible(mapLoaded);
                    advancedMapCreatorButton.setVisible(mapLoaded);
                    toTimingNotes.setVisible(mapLoaded);
                    utilsMapUtilsButton.setVisible(mapLoaded);

                    saveMapButton.setVisible(mapLoaded);
                    openMapInBrowserButton.setVisible(mapLoaded);
                    globalPatternVarianceJSlider.setVisible(mapLoaded);
                });
            }
        });
        //</editor-fold desc="Event Listener">
    }

    /** @deprecated use {@link AppController#prepareGeneration()}; kept until all buttons are rewired. */
    @Deprecated
    public void manageMap() {
        controller.prepareGeneration();
    }

    /** @deprecated use {@link AppController#parityErrorsAsBookmarks(String)}. */
    @Deprecated
    public List<Bookmark> parityErrorsAsBookmarks(String diffName) {
        return controller.parityErrorsAsBookmarks(diffName);
    }
}