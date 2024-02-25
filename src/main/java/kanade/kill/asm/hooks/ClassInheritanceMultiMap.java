package kanade.kill.asm.hooks;

import kanade.kill.util.EntityUtil;

import java.util.List;

public class ClassInheritanceMultiMap {
    public static void preGetByClass(net.minecraft.util.ClassInheritanceMultiMap map) {
        for (Object c : map.map.keySet()) {
            List l = (List) map.map.get(c);
            l.removeIf(EntityUtil::isDead);
        }
    }
}
