package kanade.kill.thread;

import kanade.kill.Launch;
import kanade.kill.asm.Transformer;
import kanade.kill.util.FieldInfo;
import kanade.kill.util.ObjectUtil;
import kanade.kill.util.Util;

import java.lang.reflect.Field;

public class FieldSaveThread extends Thread {
    @Override
    public void run() {
        while (true) {
            while (!Transformer.getFields().isEmpty()) {
                FieldInfo fieldinfo = (FieldInfo) Transformer.getFields().poll();
                Field field = fieldinfo.toField();
                if (field == null || Util.cache.containsKey(field)) {
                    continue;
                }
                Launch.LOGGER.info("Field:" + field.getName() + ":" + field.getType().getName());
                try {
                    Util.cache2.put(field, ObjectUtil.clone(ObjectUtil.getStatic(field)));
                } catch (Throwable t) {
                    if (t instanceof StackOverflowError) {
                        Launch.LOGGER.warn("Too deep. Ignoring this field.");
                    } else {
                        throw new RuntimeException(t);
                    }
                }
            }
        }
    }
}
