package DataManager.Records;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * A record representing metadata for a pattern in the system.
 * This metadata includes information such as the name, beats per minute (BPM), notes per second (NPS),
 * and associated lists of difficulties, tags, and genres.
 * The record is serializable, making it suitable for use in contexts where objects need to be serialized.
 *
 * @param name       The name of the pattern.
 * @param bpm        The beats per minute associated with the pattern.
 * @param nps        The notes per second associated with the pattern.
 * @param difficulty A list of difficulty levels for the pattern.
 * @param tags       A list of tags associated with the pattern.
 * @param genre      A list of genres associated with the pattern.
 */
public record PatMetadata(String name, double bpm, double nps, List<String> difficulty, List<String> tags, List<String> genre) implements Serializable {

    /**
     * Converts the PatMetadata record to a string representation.
     * The string is formatted with fields separated by semicolons, and lists are flattened with elements separated by commas.
     * Empty lists are replaced with "NULL".
     *
     * @return A string representation of the PatMetadata record.
     */
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

    /**
     * Compares this PatMetadata record to another object for equality.
     * Two PatMetadata records are considered equal if all their fields are equal.
     *
     * @param o The object to compare to.
     * @return True if the objects are equal, false otherwise.
     */
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

    /**
     * Returns a hash code value for this PatMetadata record.
     * The hash code is computed based on all fields of the record.
     *
     * @return A hash code value for this PatMetadata record.
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, bpm, nps, difficulty, tags, genre);
    }
}
