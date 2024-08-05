package MapGeneration.PatternGeneration;

import BeatSaberObjects.Objects.Note;

import static DataManager.Parameters.RANDOM;
import static DataManager.Parameters.logger;

public class NextLinearNote {
    /**
     * This function creates a note based on the previous note that doesn't break parity.
     * It only creates really linear patterns
     *
     * @param previousNote the note that came before.
     * @param time         time specifies on which bpm the note should be placed.
     * @return BeatSaberObjects.Objects.Note
     */
    public static Note nextLinearNote(Note previousNote, float time) {
        Note p = previousNote; //p is much cleaner than having a thousand times previousNote

        if (p == null) {
            logger.warn("Something went wrong. A note is null :thinking: Please have a look at beat: " + time);
            System.err.println("Something went wrong. A note is null :thinking: Please have a look at beat: " + time);
            p = new Note(time, 0, 0, 1, 1);
        }

        double placement = RANDOM.nextDouble() * 100;


        //blue bottom-middle-right lane, down swing
        //2,0,1
        if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 1) {
//                if (placement < 10) return new BeatSaberObjects.Objects.Note(time,3,1,1,3);
            if (placement < 20) return new Note(time, 3, 1, 1, 5);
            else if (placement < 30) return new Note(time, 3, 2, 1, 0);
            else if (placement < 50) return new Note(time, 3, 2, 1, 5);
            else if (placement < 59) return new Note(time, 3, 0, 1, 0);
            else if (placement < 68) return new Note(time, 1, 0, 1, 0);
            else return new Note(time, 2, 2, 1, 0);
        }

        //blue middle-right lane, right swing
        //3,1,3
        else if ((p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 3)) {
            if (placement < 70) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue upper-right lane, top-right swing
        //3,2,5
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 5) {
            if (placement < 5) return new Note(time, 0, 0, 1, 6);
            else if (placement < 40) return new Note(time, 1, 0, 1, 6);
            else if (placement < 90) return new Note(time, 2, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 1);
        }

        //blue upper-right lane, top swing
        //3,2,0
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 50) return new Note(time, 2, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 1);
        }

        //blue upper-middle-right lane, top swing
        //2,2,0
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 0) {
            if (placement < 5) return new Note(time, 3, 0, 1, 7);
            else if (placement < 20) return new Note(time, 3, 0, 1, 1);
            else if (placement < 55) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue bottom-middle-right lane, bottom-left swing
        //3,1,6
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 6) {
            if (placement < 40) return new Note(time, 1, 0, 1, 6);
            else if (placement < 80) return new Note(time, 2, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue bottom-middle-left lane, bottom-left swing
        //1,0,6
        else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 6) {
            if (placement < 38) return new Note(time, 3, 2, 1, 5);
            if (placement < 43) return new Note(time, 2, 0, 1, 5);
            else if (placement < 81) return new Note(time, 3, 1, 1, 5);
            else if (placement < 85) return new Note(time, 2, 2, 1, 0);
            else return new Note(time, 2, 2, 1, 5);
        }

        //blue bottom-left lane, bottom-left swing
        //0,0,6
        else if (p._lineIndex == 0 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 6) {
            if (placement < 30) return new Note(time, 2, 2, 1, 5);
            else if (placement < 80) return new Note(time, 3, 2, 1, 5);
            else if (placement < 83) return new Note(time, 3, 1, 1, 5);
            else return new Note(time, 3, 1, 1, 3);
        }

        //blue bottom-middle-right lane, bottom-left swing
        //2,0,6
        else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 6) {
            if (placement <= 40) return new Note(time, 3, 1, 1, 5);
            if (placement <= 60) return new Note(time, 3, 2, 1, 0);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-middle-right lane, top-right swing
        //2,2,5
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 5) {
            if (placement <= 20) return new Note(time, 0, 0, 1, 6);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue bottom-right lane, bottom swing
        //3,0,1
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 1) {
            if (placement <= 40) return new Note(time, 3, 1, 1, 0);
            if (placement <= 50) return new Note(time, 3, 0, 1, 0);
            if (placement <= 60) return new Note(time, 2, 0, 1, 0);
            else return new Note(time, 3, 2, 1, 0);
        }

        //blue middle-right lane, top swing
        //3,1,0
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 5) return new Note(time, 3, 1, 1, 1);
            if (placement <= 55) return new Note(time, 3, 0, 1, 1);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue top-left-middle lane, top-left swing
        //1,2,4
        else if (p._lineIndex == 1 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 4) {
            if (placement <= 50) return new Note(time, 3, 0, 1, 7);
            else return new Note(time, 2, 0, 1, 1);
        }

        //blue middle-right lane, top-right swing
        //3,1,5
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 5) {
            if (placement <= 60) return new Note(time, 2, 0, 1, 6);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue bottom-right lane, bottom-right swing
        //3,0,7
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 7) {
            if (placement <= 10) return new Note(time, 3, 2, 1, 0);
            else return new Note(time, 2, 2, 1, 0);
        }

        //blue top-right-middle lane, bottom-right swing
        //3,1,7
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 7) {
            return new Note(time, 2, 2, 1, 4);
        }

        //blue middle-right lane, bottom-right swing
        //3,0,1
        else if (p._lineIndex == 3 && p._lineLayer == 1 && p._type != 2 && p._cutDirection == 1) {
            if (placement <= 30) return new Note(time, 3, 1, 1, 0);
            else return new Note(time, 3, 2, 1, 0);
        }

        //blue right-bottom lane, bottom swing
        //3,0,0
        else if (p._lineIndex == 3 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 35) return new Note(time, 3, 0, 1, 1);
            else if (placement <= 80) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 83) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 6);
        }

        //blue bottom-right-middle lane, bottom swing
        //3,0,0
        else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 35) return new Note(time, 3, 0, 1, 1);
            else if (placement <= 40) return new Note(time, 3, 0, 1, 7);
            else if (placement <= 80) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 83) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 6);
        }

        //blue bottom-right-middle lane,  top-right swing
        //2,0,5
        else if (p._lineIndex == 2 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 5) {
            return new Note(time, 3, 0, 1, 1);
        }

        //blue bottom-left-middle lane, top swing
        //1,0,0
        else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 0) {
            if (placement <= 33) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 66) return new Note(time, 3, 0, 1, 1);
            else return new Note(time, 3, 0, 1, 7);
        }

        //blue bottom-left-middle lane, bottom swing
        //1,0,0
        else if (p._lineIndex == 1 && p._lineLayer == 0 && p._type != 2 && p._cutDirection == 1) {
            if (placement <= 33) return new Note(time, 2, 0, 1, 1);
            else if (placement <= 55) return new Note(time, 2, 2, 1, 0);
            else if (placement <= 70) return new Note(time, 2, 2, 1, 5);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-right lane, right swing
        //3,2,3
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 3) {
            if (placement <= 20) return new Note(time, 2, 2, 1, 2);
            else if (placement <= 45) return new Note(time, 1, 2, 1, 2);
            else if (placement <= 75) return new Note(time, 1, 0, 1, 6);
            else return new Note(time, 2, 0, 1, 6);
        }

        //blue top-right-middle lane, right swing
        //2,2,3
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 3) {
            if (placement <= 5) return new Note(time, 2, 2, 1, 2);
            else if (placement <= 55) return new Note(time, 1, 2, 1, 2);
            else if (placement <= 64) return new Note(time, 0, 2, 1, 2);
            else return new Note(time, 1, 0, 1, 6);
        }

        //blue top-right-middle lane,  left swing
        //2,2,2
        else if (p._lineIndex == 2 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 2) {
            if (placement <= 40) return new Note(time, 3, 2, 1, 3);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-left-middle lane, left swing
        //1,2,2
        else if (p._lineIndex == 1 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 2) {
            if (placement <= 40) return new Note(time, 3, 2, 1, 3);
            if (placement <= 75) return new Note(time, 2, 2, 1, 3);
            else return new Note(time, 3, 2, 1, 5);
        }

        //blue top-right lane,  bottom-right swing
        //3,2,7
        else if (p._lineIndex == 3 && p._lineLayer == 2 && p._type != 2 && p._cutDirection == 7) {
            if (placement <= 20) return new Note(time, 3, 2, 1, 4);
            if (placement <= 60) return new Note(time, 2, 2, 1, 4);
            else return new Note(time, 1, 2, 1, 4);
        }


        //error catching:
        //If I forgot to add a note,it will be displayed here:
        else {
            if (p._type != 2 && (p._cutDirection == 1 || p._cutDirection == 6 || p._cutDirection == 2))
                return new Note(time, 3, 2, 1, 5);
            else if (p._type != 2 && (p._cutDirection == 7 || p._cutDirection == 3))
                return new Note(time, 2, 2, 1, 4);
            else if (p._type != 4 && (p._cutDirection == 0 || p._cutDirection == 5 || p._cutDirection == 4))
                return new Note(time, 0, 2, 1, 1);

            throw new IllegalArgumentException("There is an undetected note!");
        }
    }


}
