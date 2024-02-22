package DataManager.Records;

import java.util.List;

public record PatMetadata(String name, double bpm, double nps, List<String> difficulty, List<String> tags, List<String> genre) {
    public String toString() {
        return this.name + ";" +
                this.bpm + ";" +
                this.nps + ";" +
                this.difficulty + ";" +
                this.tags.toString().replace("[]", "NULL").replace("[", "").replace(" ", "").replaceAll("\"", "").replace("]", "") + ";" +
                this.genre.toString().replace("[]", "NULL").replace("[", "").replace(" ", "").replaceAll("\"", "").replace("]", "") + "\n";

    }
}
