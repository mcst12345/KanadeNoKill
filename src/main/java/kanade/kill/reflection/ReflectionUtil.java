package kanade.kill.reflection;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionUtil {
    public static Field[] getFields(Class<?> clazz) {
        try {
            return (Field[]) EarlyMethods.getDeclaredFields0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new Field[0];
        }
    }

    @Nonnull
    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Field field : getFields(clazz)) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        throw new NoSuchFieldException(name);
    }

    public static Field[] getAllFields(Class<?> clazz) {
        List<Field> list = new ArrayList<>();
        while (clazz != Object.class) {
            Collections.addAll(list, getFields(clazz));
            clazz = clazz.getSuperclass();
        }
        Field[] re = new Field[list.size()];
        for (int i = 0; i < re.length; i++) {
            re[i] = list.get(i);
        }
        return re;
    }
}
