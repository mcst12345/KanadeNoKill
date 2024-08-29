package miku.lib.reflection;

import miku.lib.utils.NoPrivateOrProtected;
import sun.reflect.NativeMethodAccessorImpl;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReflectionHelper {

    private static final Tool sm = new Tool();

    private static class Tool extends SecurityManager {
        @Override
        public Class<?>[] getClassContext() {
            return super.getClassContext();
        }
    }

    public static Class<?> getCallerClass() {
        Class<?>[] list = sm.getClassContext();
        return list[3];
    }

    static {
        try {
            NoPrivateOrProtected.FuckAccess(Class.class);
            NoPrivateOrProtected.FuckAccess(Class.forName("sun.reflect.NativeMethodAccessorImpl"));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field[] getFields(Class<?> clazz) {
        if (clazz == null) {
            return new Field[0];
        }
        try {
            return clazz.getDeclaredFields0(false);
        } catch (Throwable e) {
            return new Field[0];
        }
    }

    public static Field getField(Class<?> clazz, String name) throws NoSuchFieldException {
        for (Field field : getFields(clazz)) {
            if (field.getName().equals(name)) {
                return field;
            }
        }
        throw new NoSuchFieldException(name);
    }

    public static Method getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Method method : getMethods(clazz)) {
            if (method.getName().equals(name) && arrayContentsEq(parameterTypes, method.getParameterTypes())) {
                return method;
            }
        }
        throw new NoSuchMethodError(name);
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... parameterTypes) throws NoSuchMethodError {
        for (Constructor<?> constructor : getConstructors(clazz)) {
            if (arrayContentsEq(parameterTypes, constructor.getParameterTypes())) {
                constructor.setAccessible(true);
                return constructor;
            }
        }
        throw new NoSuchMethodError();
    }

    public static Constructor<?>[] getConstructors(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructors0(false);
        } catch (Throwable e) {
            return new Constructor[0];
        }
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

    public static Method[] getMethods(Class<?> clazz) {
        try {
            return clazz.getDeclaredMethods0(false);
        } catch (Throwable e) {
            return new Method[0];
        }
    }

    public static Object invoke(Method method, Object obj, Object... args) {
        boolean Static = Modifier.isStatic(method.getModifiers());
        try {
            return NativeMethodAccessorImpl.invoke0(method, Static ? null : obj, args != null ? args : new Object[0]);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static String getName(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        try {
            return clazz.getName0();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
