import BeatSaberObjects.Note;
import BeatSaberObjects.TimingNote;
import CustomWaveGenerator.CustomWaveGenerator;
import CustomWaveGenerator.WaveVisualizationFrame;
import DataManager.CreateAllNecessaryDIRsAndFiles;
import UserInterface.UserInterface;
import CustomWaveGenerator.Coordinate;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static DataManager.Parameters.*;

public class Start {

        /*
    Red: 0
    Blue: 1

    Layer - Index:          Cut direction:
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-2|       | 4 | 0 | 5 |
    |---|---|---|---|       |---|---|---|
    |   |   |   |3-1|       | 2 | 8 | 3 |
    |---|---|---|---|       |---|---|---|
    |0-0|1-0|2-0|3-0|       | 6 | 1 | 7 |
    |---|---|---|---|       |---|---|---|
     */


    public static void main(String[] args) {
        SEED = (long) (new Random().nextDouble() * 1000000000);
        RANDOM = new Random(SEED);
        System.out.println("Current seed is: " + SEED);


        CreateAllNecessaryDIRsAndFiles.createAllNecessaryDIRsAndFiles();

        UserInterface ui = new UserInterface();
        ui.setVisible(true);
    }
}