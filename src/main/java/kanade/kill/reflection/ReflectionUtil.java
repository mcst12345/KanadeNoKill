package kanade.kill.reflection;

import kanade.kill.util.memory.MemoryHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionUtil {
    public static Field[] getFields(Class<?> clazz) {
        return clazz.getDeclaredFields0(false);
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

    @Nonnull
    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Constructor<?> constructor : getConstructors(clazz)) {
            //System.out.println(Arrays.toString(constructor.getParameterTypes()));
            //System.out.println(Arrays.toString(parameterTypes));
            if (arrayContentsEq(parameterTypes, constructor.getParameterTypes())) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        throw new NoSuchMethodError();
    }

    public static Constructor<?>[] getConstructors(Class<?> clazz) {
        return clazz.getDeclaredConstructors0(false);
    }

    public static Field[] getAllFields(Class<?> clazz) {
        if (clazz == null) {
            return new Field[0];
        }
        List<Field> list = new ArrayList<>();
        while (clazz != Object.class && clazz != null) {
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
        return clazz.getDeclaredMethods0(false);
    }

    public static Object invoke(Method method, @Nullable Object obj, @Nullable Object... args) {
        boolean Static = Modifier.isStatic(method.getModifiers());
        return sun.reflect.NativeMethodAccessorImpl.invoke0(method, Static ? null : obj, args != null ? args : new Object[0]);
    }

    public static String getName(Class<?> clazz) {
        //try {
        //    return (String) ReflectionUtil.invoke(EarlyMethods.getName0, clazz);
        //} catch (NullPointerException t){
        //    return "";
        //}
        if (clazz == null) {
            return "";
        }
        return MemoryHelper.getClassName(clazz).replace('/', '.');
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

    public static boolean isExtend(Class<?> a, Class<?> b) {
        if (a == null) {
            return false;
        }
        if (b == Object.class) {
            return true;
        }
        try {
            while (a.getSuperclass() != Object.class) {
                a = a.getSuperclass();
                if (a == b) {
                    return true;
                }
            }
        } catch (Throwable t) {
            return false;
        }
        return false;
    }

    public static boolean isExtend(Class<?> a, String b) {
        if (b == null || b.isEmpty()) {
            return false;
        }
        if (b.equals("java.lang.Object")) {
            return true;
        }
        while (a.getSuperclass() != Object.class) {
            a = a.getSuperclass();
            String name = getName(a);
            if (name.equals(b)) {
                return true;
            } else if (name.isEmpty()) {
                return false;
            }
        }
        return false;
    }


}
