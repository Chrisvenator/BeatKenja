import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EventsTest {

    @Test
    void testToString() {
//        new Events(1, 0, 1);
//        new Events(1, 0, 1);
//        new Events(1, 2, 1);
//        new Events(1, 2, 1);
//        new Events(1, 3, 2);
//        new Events(1, 3, 2);
//        new Events(1, 4, 2);
//        new Events(1, 4, 2);
//        new Events(1, 5, 3);
//        new Events(1, 5, 3);
//        new Events(1, 6, 3);
//        new Events(1, 6, 3);
//        System.out.println("Assertions.assertEquals(" + this.toString() + ",new Events(" + _time + ", " + _type + ", " + _value + ").toString());");

        Assertions.assertEquals("{\"_time\":0.0,\"_type\":0,\"_value\":0}", new Events(0, 0, 0).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":0,\"_value\":1}", new Events(1, 0, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":0,\"_value\":1}", new Events(1, 0, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":2,\"_value\":1}", new Events(1, 2, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":2,\"_value\":1}", new Events(1, 2, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":3,\"_value\":2}", new Events(1, 3, 2).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":4,\"_value\":2}", new Events(1, 4, 2).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":4,\"_value\":2}", new Events(1, 4, 2).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":5,\"_value\":3}", new Events(1, 5, 3).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":5,\"_value\":3}", new Events(1, 5, 3).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":6,\"_value\":3}", new Events(1, 6, 3).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":6,\"_value\":3}", new Events(1, 6, 3).toString());
        Assertions.assertEquals("{\"_time\":0.0,\"_type\":0,\"_value\":0}", new Events(0, 0, 0).toString());
    }

    @Test
    void convertFlashLightsToOnLights() {
        Events e1 = new Events(0, 0, 0);
        Events e2 = new Events(0, 0, 6);

        e1.convertFlashLightsToOnLights();
        e2.convertFlashLightsToOnLights();

        Assertions.assertEquals(0, e1._value);
        Assertions.assertEquals(1, e2._value);
    }
}