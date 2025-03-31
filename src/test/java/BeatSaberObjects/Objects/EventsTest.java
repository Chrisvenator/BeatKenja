package BeatSaberObjects.Objects;

import BeatSaberObjects.Objects.Events;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests for Events class")
class EventsTest {
    
    @Test
    @DisplayName("Constructor: No _floatValue provided should default _floatValue to 0")
    void testConstructorNoFloatValue() {
        // Using the varargs constructor without providing any _floatValue.
        Events event = new Events(1.0f, 2, 3);
        assertEquals(1.0f, event._time, "Time should be set correctly");
        assertEquals(2, event._type, "Type should be set correctly");
        assertEquals(3, event._value, "Value should be set correctly");
        assertEquals(0, event._floatValue, "Without _floatValue argument, _floatValue should default to 0");
    }
    
    @Test
    @DisplayName("Constructor: With _floatValue provided, _floatValue should be set to the first value")
    void testConstructorWithFloatValue() {
        Events event = new Events(2.0f, 1, 4, 7);
        assertEquals(2.0f, event._time, "Time should be set correctly");
        assertEquals(1, event._type, "Type should be set correctly");
        assertEquals(4, event._value, "Value should be set correctly");
        assertEquals(7, event._floatValue, "FloatValue should be set to the first provided value");
    }
    
    @Test
    @DisplayName("Constructor: With multiple _floatValue provided, only the first is used")
    void testConstructorMultipleFloatValues() {
        Events event = new Events(3.0f, 5, 8, 9, 10);
        assertEquals(9, event._floatValue, "Only the first provided _floatValue should be used");
    }
    
    @Test
    @DisplayName("Constructor: Passing null as _floatValue should default _floatValue to 0")
    void testConstructorNullFloatValue() {
        // Passing null explicitly to the varargs parameter.
        Events event = new Events(1.5f, 3, 2, (int[]) null);
        assertEquals(0, event._floatValue, "When null is passed for _floatValue, it should default to 0");
    }
    
    @Test
    @DisplayName("toString: Should return a correctly formatted JSON-like string")
    void testToString() {
        Events event = new Events(4.0f, 2, 6, 15);
        String expected = "{\"_time\":" + 4.0f + ",\"_type\":" + 2 + ",\"_value\":" + 6 + ",\"_floatValue\":" + 15F + "}";
        assertEquals(expected, event.toString(), "toString should return the expected JSON-like string");
    }
    
    @Test
    @DisplayName("equals: Events with same _time, _type, and _value are equal regardless of _floatValue")
    void testEqualsSameValues() {
        Events event1 = new Events(5.0f, 3, 7, 20);
        // Different _floatValue, but equals ignores it.
        Events event2 = new Events(5.0f, 3, 7, 30);
        assertTrue(event1.equals(event2), "Events with the same _time, _type, and _value should be equal even if _floatValue differs");
    }
    
    @Test
    @DisplayName("equals: Events with different _time are not equal")
    void testEqualsDifferentTime() {
        Events event1 = new Events(5.0f, 3, 7, 20);
        Events event2 = new Events(6.0f, 3, 7, 20);
        assertFalse(event1.equals(event2), "Events with different _time should not be equal");
    }
    
    @Test
    @DisplayName("equals: Events with different _type are not equal")
    void testEqualsDifferentType() {
        Events event1 = new Events(5.0f, 3, 7, 20);
        Events event2 = new Events(5.0f, 4, 7, 20);
        assertFalse(event1.equals(event2), "Events with different _type should not be equal");
    }
    
    @Test
    @DisplayName("equals: Events with different _value are not equal")
    void testEqualsDifferentValue() {
        Events event1 = new Events(5.0f, 3, 7, 20);
        Events event2 = new Events(5.0f, 3, 8, 20);
        assertFalse(event1.equals(event2), "Events with different _value should not be equal");
    }
    
    @Test
    @DisplayName("hashCode: Equal events should have the same hash code")
    void testHashCode() {
        Events event1 = new Events(5.0f, 3, 7, 20);
        Events event2 = new Events(5.0f, 3, 7, 100);
        assertEquals(event1.hashCode(), event2.hashCode(), "Equal events should have identical hash codes, even if _floatValue differs");
    }
    
    @Test
    @DisplayName("convertFlashLightsToOnLights: Should convert _value from 6 to 1")
    void testConvertFlashLightsToOnLights() {
        Events event = new Events(2.0f, 1, 6, 50);
        event.convertFlashLightsToOnLights();
        assertEquals(1, event._value, "If _value is 6, it should be converted to 1");
    }
    
    @Test
    @DisplayName("convertFlashLightsToOnLights: Should not change _value if it is not 6")
    void testConvertFlashLightsToOnLightsNoChange() {
        Events event = new Events(2.0f, 1, 5, 50);
        event.convertFlashLightsToOnLights();
        assertEquals(5, event._value, "If _value is not 6, it should remain unchanged");
    }
    
    
    
    
    @Test
    void testToStringManual() {
        Assertions.assertEquals("{\"_time\":0.0,\"_type\":0,\"_value\":0,\"_floatValue\":0.0}", new Events(0, 0, 0).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":0,\"_value\":1,\"_floatValue\":0.0}", new Events(1, 0, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":0,\"_value\":1,\"_floatValue\":0.0}", new Events(1, 0, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":2,\"_value\":1,\"_floatValue\":0.0}", new Events(1, 2, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":2,\"_value\":1,\"_floatValue\":0.0}", new Events(1, 2, 1).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":3,\"_value\":2,\"_floatValue\":0.0}", new Events(1, 3, 2).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":4,\"_value\":2,\"_floatValue\":0.0}", new Events(1, 4, 2).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":4,\"_value\":2,\"_floatValue\":0.0}", new Events(1, 4, 2).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":5,\"_value\":3,\"_floatValue\":0.0}", new Events(1, 5, 3).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":5,\"_value\":3,\"_floatValue\":0.0}", new Events(1, 5, 3).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":6,\"_value\":3,\"_floatValue\":0.0}", new Events(1, 6, 3).toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_type\":6,\"_value\":3,\"_floatValue\":0.0}", new Events(1, 6, 3).toString());
        Assertions.assertEquals("{\"_time\":0.0,\"_type\":0,\"_value\":0,\"_floatValue\":0.0}", new Events(0, 0, 0).toString());
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
