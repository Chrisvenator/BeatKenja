package DataManager.Database;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class DatabaseCommonMethods{
    public static <T> ArrayList<T> checkCastFromQuery(List<?> result, Class<T> type) {
        ArrayList<T> checkedList = new ArrayList<>();
        for (Object obj : result) {
            if (type.isInstance(obj)) {
                checkedList.add(type.cast(obj));
            } else {
                throw new ClassCastException("Unexpected result type: " + obj.getClass().getName());
            }
        }
        return checkedList;
    }
}
