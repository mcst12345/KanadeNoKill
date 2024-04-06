package kanade.kill.asm.hooks;

import com.google.common.collect.Sets;
import com.google.common.reflect.TypeToken;
import kanade.kill.Config;
import kanade.kill.Launch;
import kanade.kill.util.Util;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.IEventListener;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

public class EventBus {
    @SuppressWarnings("ConstantValue")
    public static boolean post(net.minecraftforge.fml.common.eventhandler.EventBus bus, Event event) {
        if (Config.disableEvent || !Util.shouldPostEvent(event)) {
            return false;
        }
        IEventListener[] listeners = event.getListenerList().getListeners(bus.busID);
        int index = 0;
        try {
            for (; index < listeners.length; index++) {
                listeners[index].invoke(event);
            }
        } catch (NullPointerException | NoSuchMethodError | NoSuchFieldError ignored) {
        } catch (Throwable throwable) {
            if (!(throwable instanceof NoSuchFieldException && !(throwable instanceof NoSuchMethodException))) {
                Launch.LOGGER.warn(throwable);
            }
        }
        return event.isCancelable() && event.isCanceled();
    }

    public static void register(net.minecraftforge.fml.common.eventhandler.EventBus bus, Object target) {
        if (bus == null || bus.listenerOwners == null || bus.listeners == null) {
            return;
        }
        if (bus.listeners.containsKey(target)) {
            return;
        }

        ModContainer activeModContainer = Loader.instance().activeModContainer();
        if (activeModContainer == null) {
            FMLLog.log.error("Unable to determine registrant mod for {}. This is a critical error and should be impossible", target, new Throwable());
            activeModContainer = Loader.instance().getMinecraftModContainer();
        }
        bus.listenerOwners.put(target, activeModContainer);
        boolean isStatic = target.getClass() == Class.class;
        @SuppressWarnings("unchecked")
        Set<? extends Class<?>> supers = isStatic ? Sets.newHashSet((Class<?>) target) : TypeToken.of(target.getClass()).getTypes().rawTypes();
        for (Method method : (isStatic ? (Class<?>) target : target.getClass()).getMethods()) {
            if (isStatic && !Modifier.isStatic(method.getModifiers()))
                continue;
            else if (!isStatic && Modifier.isStatic(method.getModifiers()))
                continue;

            for (Class<?> cls : supers) {
                try {
                    Method real = cls.getDeclaredMethod(method.getName(), method.getParameterTypes());
                    if (real.isAnnotationPresent(SubscribeEvent.class)) {
                        Class<?> eventType = getaClass(method);

                        if (!Event.class.isAssignableFrom(eventType)) {
                            throw new IllegalArgumentException("Method " + method + " has @SubscribeEvent annotation, but takes a argument that is not an Event " + eventType);
                        }

                        bus.register(eventType, target, real, activeModContainer);
                        break;
                    }
                } catch (NoSuchMethodException e) {
                    // Eat the error, this is not unexpected
                }
            }
        }
    }

    private static Class<?> getaClass(Method method) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation, but requires " + parameterTypes.length +
                            " arguments.  Event handler methods must require a single argument."
            );
        }

        return parameterTypes[0];
    }
}
