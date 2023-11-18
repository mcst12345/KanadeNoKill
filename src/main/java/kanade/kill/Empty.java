package kanade.kill;

import java.lang.reflect.Field;

public class Empty {
    public static void main(String[] args) throws NoSuchFieldException {
        Field field = System.class.getDeclaredField("security");
        System.out.println(field.getName());
        System.out.println(field.getType());
    }
}
