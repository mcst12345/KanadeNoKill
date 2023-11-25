package kanade.kill;

import kanade.kill.asm.Transformer;
import kanade.kill.reflection.EarlyFields;
import scala.concurrent.util.Unsafe;
import sun.instrument.InstrumentationImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

public class AgentMain {
    private static Instrumentation inst;
    private static long nativeAgent;
    public static void agentmain(String args, Instrumentation instrumentation){
        Core.LOGGER.info("Agent start.");
        inst = instrumentation;
        nativeAgent = Unsafe.instance.getLongVolatile(instrumentation, EarlyFields.mNativeAgent_offset);
        Core.LOGGER.info("Add transformer.");
        try {
            inst.addTransformer(Transformer.instance);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            System.out.println("Destroy instrumentation.");
            DestroyInstrumentation();
        } catch (UnmodifiableClassException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Instrumentation getInstrumentation(){
        return inst;
    }

    public static long getNativeAgent() {
        return nativeAgent;
    }

    private static void DestroyInstrumentation() throws UnmodifiableClassException, ClassNotFoundException {
        Class<?> clazz;
        byte[] bytes;
        try (InputStream is = Empty.class.getResourceAsStream("/sun/instrument/InstrumentationImpl.class")) {
            assert is != null;
            //6 lines below are from Apache common io.
            final ByteArrayOutputStream output = new ByteArrayOutputStream();
            final byte[] buffer = new byte[8024];
            int n;
            while (-1 != (n = is.read(buffer))) {
                output.write(buffer, 0, n);
            }
            bytes = output.toByteArray();
            clazz = Unsafe.instance.defineClass("sun.instrument.InstrumentationImpl", bytes, 0, bytes.length, InstrumentationImpl.class.getClassLoader(), InstrumentationImpl.class.getProtectionDomain());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ClassDefinition classDefinition = new ClassDefinition(clazz, bytes);
        inst.redefineClasses(classDefinition);
    }
}
