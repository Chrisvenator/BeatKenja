package DataManager.Objects;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A generic class representing a tuple of three elements, commonly known as a "triple".
 * This class can hold three objects of potentially different types: left, middle, and right.
 * The class provides standard methods like `equals`, `hashCode`, and `toString` through the use of Lombok annotations.
 *
 * @param <L> The type of the first element in the triple (left).
 * @param <M> The type of the second element in the triple (middle).
 * @param <R> The type of the third element in the triple (right).
 */
@Getter @Setter @ToString @EqualsAndHashCode
public class Triple<L, M, R> {

    /** The first element in the triple.*/
    private final L left;
    /** The second element in the triple.*/
    private final M middle;
    /** The third element in the triple.*/
    private final R right;

    /**
     * Constructs a new `Triple` with the specified values.
     *
     * @param left   The value for the left element.
     * @param middle The value for the middle element.
     * @param right  The value for the right element.
     */
    public Triple(L left, M middle, R right) {
        this.left = left;
        this.middle = middle;
        this.right = right;
    }

}
