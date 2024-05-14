package DataManager.Records;

import java.util.List;

public record PatMetadata(String name, double bpm, double nps, List<String> difficulty, List<String> tags, List<String> genre) {
    public String toString() {
        String s = this.name + ";";

        s += bpm <= 99 ? " " : "";
        s += bpm <= 9 ? " " : "";
        s += this.bpm + ";";

        s += nps <= 9 ? " " : "";
        s += this.nps + ";";

        s += this.difficulty + ";" +
                this.tags.toString().replace("[]", "NULL").replace("[", "").replace(" ", "").replaceAll("\"", "").replace("]", "") + ";" +
                this.genre.toString().replace("[]", "NULL").replace("[", "").replace(" ", "").replaceAll("\"", "").replace("]", "") + "\n";

        s = s.replaceAll("\\.0;", ";");
        return s;
    }
}
