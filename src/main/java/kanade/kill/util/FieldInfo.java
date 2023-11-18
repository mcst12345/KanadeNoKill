package kanade.kill.util;

import kanade.kill.reflection.EarlyMethods;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class FieldInfo {
    private final String clazz;
    private final String field;

    public FieldInfo(ClassNode cn, FieldNode fn) {
        this.clazz = cn.name.replace('/', '.');
        this.field = fn.name;
    }

    public boolean equals(Object o) {
        if (o instanceof FieldInfo) {
            FieldInfo fn = (FieldInfo) o;
            return fn.clazz.equals(this.clazz) && fn.field.equals(this.field);
        }
        return false;
    }

    @Nullable
    public Field toField() {
        try {
            Class<?> Clazz = Class.forName(clazz);
            for (Field field1 : (Field[]) EarlyMethods.getDeclaredFields0.invoke(Clazz)) {
                if (field1.getName().equals(field)) {
                    return field1;
                }
            }
        } catch (ClassNotFoundException e) {
            return null;
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
