import BeatSaberObjects.Objects.Obstacle;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ObstacleTest {

    @Test
    void testToString() {
        Obstacle o = new Obstacle(0, "0", 0, 0, 0);
        Assertions.assertEquals("{\"_time\":0.0,\"_lineIndex\":0,\"_type\":0,\"_duration\":0.0,\"_width\":0.0}", o.toString());
    }
}