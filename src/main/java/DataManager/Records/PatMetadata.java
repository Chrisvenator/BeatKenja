package DataManager.Records;

import java.util.ArrayList;
import java.util.List;

public record PatMetadata(double bpm, double nps, String difficulty, List<String> tags, List<String> genre) {
    public String toString() {
        return this.bpm + ";" +
                this.nps + ";" +
                this.difficulty + ";" +
                this.tags.toString().replace("[]", "NULL").replace("[", "").replace(" ", "").replaceAll("\"", "").replace("]", "") + ";" +
                this.genre.toString().replace("[]", "NULL").replace("[", "").replace(" ", "").replaceAll("\"", "").replace("]", "") + "\n";

    }
}
