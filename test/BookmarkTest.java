import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class BookmarkTest {

    @Test
    void testToString() {
        Bookmark b01 = new Bookmark(0, "YES", null);
        Bookmark b02 = new Bookmark(1, "", null);
        Bookmark b03 = new Bookmark(2, "q3 ", new float[]{0.0f, 0.0f, 0.0f});
        Bookmark b04 = new Bookmark(3, "187621", null);
        Assertions.assertEquals("{\"_time\":0.0,\"_name\":\"YES\",\"_color\":null}", b01.toString());
        Assertions.assertEquals("{\"_time\":1.0,\"_name\":\"\",\"_color\":null}", b02.toString());
        Assertions.assertEquals("{\"_time\":2.0,\"_name\":\"q3 \",\"_color\":[0.0, 0.0, 0.0]}", b03.toString());
        Assertions.assertEquals("{\"_time\":3.0,\"_name\":\"187621\",\"_color\":null}", b04.toString());
    }

    @Test
    void testEquals() {
        Bookmark b01 = new Bookmark(0, "YES", null);
        Bookmark b02 = new Bookmark(1, "", null);
        Bookmark b03 = new Bookmark(2, "q3 ", new float[]{0.0f, 0.0f, 0.0f});
        Bookmark b031 = new Bookmark(2, "q3 ", new float[]{0.0f, 0.0f, 0.0f});
        Bookmark b04 = new Bookmark(3, "187621", null);
        Bookmark b041 = new Bookmark(3, "187621", null);

        Assertions.assertNotEquals(b01, b02);
        Assertions.assertNotEquals(b03, b02);
        Assertions.assertNotEquals(b04, b02);
        Assertions.assertEquals(b03, b031);
        Assertions.assertEquals(b04, b041);
    }
}