package DataManager.Records;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public record PatMetadata(String name, double bpm, double nps, List<String> difficulty, List<String> tags, List<String> genre) implements Serializable {

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PatMetadata that = (PatMetadata) o;
        return Double.compare(bpm, that.bpm) == 0 &&
                Double.compare(nps, that.nps) == 0 &&
                Objects.equals(name, that.name) &&
                Objects.equals(difficulty, that.difficulty) &&
                Objects.equals(tags, that.tags) &&
                Objects.equals(genre, that.genre);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, bpm, nps, difficulty, tags, genre);
    }
}
