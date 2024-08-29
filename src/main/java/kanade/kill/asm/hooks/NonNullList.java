package kanade.kill.asm.hooks;

import kanade.kill.util.Util;
import org.apache.commons.lang3.Validate;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;

public class NonNullList {
    public static Object remove(net.minecraft.util.NonNullList list, int p_remove_1_) {
        Object o = list.delegate.get(p_remove_1_);
        if (Util.NoRemove(o)) {
            return o;
        }
        return list.delegate.remove(p_remove_1_);
    }

    public static void clear(net.minecraft.util.NonNullList list) {
        if (list.defaultElement == null) {
            Predicate filter = o -> !Util.NoRemove(o);
            Objects.requireNonNull(filter);
            list.removeIf(filter);
        } else {
            for (int i = 0; i < list.size(); ++i) {
                if (Util.NoRemove(list.get(i))) {
                    continue;
                }
                list.set(i, list.defaultElement);
            }
        }
    }

    public static Object set(net.minecraft.util.NonNullList list, int p_set_1_, Object p_set_2_) {
        Object o = list.get(p_set_1_);
        if (Util.NoRemove(o)) {
            return o;
        }
        Validate.notNull(p_set_2_);
        return list.delegate.set(p_set_1_, p_set_2_);
    }
}
