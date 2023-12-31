package kanade.kill.reflection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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

    @Nonnull
    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Method method : getMethods(clazz)) {
            if (method.getName().equals(name) && arrayContentsEq(parameterTypes, method.getParameterTypes())) {
                return method;
            }
        }
        throw new NoSuchMethodError(name);
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

    public static Method[] getAllMethods(Class<?> clazz) {
        List<Method> list = new ArrayList<>();
        while (clazz != Object.class) {
            Collections.addAll(list, getMethods(clazz));
            clazz = clazz.getSuperclass();
        }
        Method[] re = new Method[list.size()];
        for (int i = 0; i < re.length; i++) {
            re[i] = list.get(i);
        }
        return re;
    }

    private static Method[] getMethods(Class<?> clazz) {
        try {
            return (Method[]) EarlyMethods.getDeclaredMethods0.invoke(clazz, false);
        } catch (IllegalAccessException | InvocationTargetException e) {
            return new Method[0];
        }
    }

    public static Object invoke(Method method, @Nullable Object obj, @Nullable Object... args) {
        if (method == null) {
            return null;
        }
        EarlyMethods.invoke0.setAccessible(true);
        method.setAccessible(true);
        try {
            return EarlyMethods.invoke0.invoke(null, method, obj, args != null ? args : new Object[0]);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getName(Class<?> clazz) {
        return (String) ReflectionUtil.invoke(EarlyMethods.getName0, clazz);
    }

    private static boolean arrayContentsEq(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }

        if (a2 == null) {
            return a1.length == 0;
        }

        if (a1.length != a2.length) {
            return false;
        }

        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }

        return true;
    }
}
