package UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Buttons;

import BeatSaberObjects.Objects.BeatSaberMap;
import BeatSaberObjects.Objects.Note;
import DataManager.FileManager;
import DataManager.Parameters;
import MapGeneration.PatternGeneration.CommonMethods.FixSwingTimings;
import MapGeneration.PatternGeneration.CommonMethods.NpsBpmConverter;
import MapGeneration.PatternGeneration.CommonMethods.Parser;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.Exceptions.WrongFileException;
import UserInterface.Elements.Buttons.ButtonTypes.GlobalButtons.GlobalButton;
import UserInterface.Elements.ElementTypes;
import UserInterface.UserInterface;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static DataManager.Parameters.AUTOLOAD_DEFAULT_MAP_for_testing;
import static DataManager.Parameters.BPM;
import static DataManager.Parameters.DEFAULT_PATH_FOR_AUTOLOAD_MAP;
import static DataManager.Parameters.FILE_CHOOSER;
import static DataManager.Parameters.MAP_FILE_FORMAT;
import static DataManager.Parameters.filePath;
import static DataManager.Parameters.logger;

public class GlobalOpenMapButton extends GlobalButton {
    @lombok.SneakyThrows
    public GlobalOpenMapButton(UserInterface ui) {
        super(ElementTypes.GLOBAL_OPEN_MAP, ui);
        setBackground(Color.cyan);
        logger.debug("GlobalOpenMapButton initialized.");

        // Autoload default map for testing purposes
        if (AUTOLOAD_DEFAULT_MAP_for_testing) {
            File f = new File(DEFAULT_PATH_FOR_AUTOLOAD_MAP);
            if (!f.exists() || f.isDirectory() || !f.isFile() || !f.canRead()) {
                logger.error("Wrong file type while initializing GlobalOpenMapButton: {}", DEFAULT_PATH_FOR_AUTOLOAD_MAP);
                throw new WrongFileException(DEFAULT_PATH_FOR_AUTOLOAD_MAP, "Wrong file type!");
            }

            loadMap(f);
            filePath = f.getParent();

            logger.info("Successfully created map from Json");
            successfullyLoaded(f.getAbsolutePath());
        }
    }

    @Override
    public void onClick() {
        int option = FILE_CHOOSER.showOpenDialog(this);
        logger.debug("File chooser opened with option: {}", option);

        if (!approveFileLoading(option)) {
            logger.info("File loading not approved.");
            return;
        }

        filePath = FILE_CHOOSER.getCurrentDirectory().toString();
        try {
            File path = FILE_CHOOSER.getSelectedFile();
            ui.map.clear();
            ui.statusCheck.clear();
            Parameters.PARITY_ERRORS_LIST.clear();

            if (path.isDirectory()) {
                File [] files = path.listFiles(MAP_FILE_FORMAT);
                if (files == null || files.length == 0) {
                    logger.error("No files found in path: {}", path);
                    System.err.println("No files found in path: " + path);
                    errorWhileLoading(new WrongFileException(path.getAbsolutePath(), "Could not find valid files in folder!"));
                    return;
                }

                //Change filePath because before, we set it to the parent dir and not the current (working) dir
                filePath = path.getAbsolutePath();
                extractAndSetGlobalBPM(path);
                for (File f : files) {
                    System.out.println("Found file: " + f.getName());
                    loadMap(f);
                }
            } else {
                if (path.getAbsolutePath().contains("Info.dat") || !path.getAbsolutePath().contains(".dat")) {
                    logger.error("Wrong file type While loading difficulty: {}", path.getName());
                    throw new WrongFileException(path.getName(), "Wrong file type!");
                } else loadMap(path);
                extractAndSetGlobalBPM(path.getParentFile());
            }

            successfullyLoaded(FILE_CHOOSER.getSelectedFile().getAbsolutePath());
        }
        catch (WrongFileException e) {
            errorWhileLoading(e);
        }
        catch (Exception e) {
            errorWhileLoading(e);
            printException(e);
        }
    }

    public void loadMap(File path){
        ui.map.add(BeatSaberMap.newMapFromJSON(path.getAbsolutePath()));
        Parameters.PARITY_ERRORS_LIST.put(path.getName(), new ArrayList<>());

        logger.info("Successfully loaded: {}/{}", path.getParent(), path.getName());
        System.out.println("Successfully loaded: " + path.getParent() + "/" + path.getName());

        extractAndSetGlobalBPM(path);
    }

    private void extractAndSetGlobalBPM (File path){
        File info = new File(path.getParentFile().getAbsolutePath() + "/info.dat");
        if (!info.exists() || !info.isFile() || !info.canRead()) {return;}

        List<String> infoFile = FileManager.readFile(info.getAbsolutePath());
        String searchString = "\"_beatsPerMinute\": ";
        for (String line : infoFile) {
            if (line.contains(searchString)){
                ui.globalButton.globalBPMField.setBPM(
                        Parser.parseValue(
                                line.substring(line.indexOf(searchString) + searchString.length(), line.lastIndexOf(",")),
                                "bpm according to info.at",
                                Double::parseDouble,
                                BPM
                        ) // Parser
                ); //set bpm
            }
        }
    }

    private void errorWhileLoading(Exception e) {
        logger.error("Error while loading Map. Map probably has the wrong format: {}", e.getMessage());
        System.err.println("[ERROR]: Map probably has the wrong format: \n" + e);
        ui.labelMapDiff.setText("There was an error while importing the map!");
        ui.labelMapDiff.setBounds(60, 20, 300, 30);
        this.setBounds(320, 20, 300, 30);
        this.setBackground(Color.RED);
        ui.mapSuccessfullyLoaded = false;
    }

    private void successfullyLoaded(String absolutePath) {
        if (Parameters.FIX_INCONSISTENT_TIMINGS_IN_FASTER_SECTIONS) ui.map.forEach(map -> {

            // Note placements must be converted to seconds instead of beats
            List<Note> notes = new ArrayList<>(List.of(ui.map.get(0)._notes));
            NpsBpmConverter.convertBeatsToSeconds(notes);

            //Plat as multiple overlaying graphs.
            FixSwingTimings.plotAsGraphs(map.difficultyFileName, Arrays.asList(map._notes));
        });

        this.setText("load another diff");
        this.setBounds(270, 20, 200, 30);
        this.setBackground(Color.GREEN);
        ui.mapSuccessfullyLoaded = true;
    }
}
