package kanade.kill.util;

import kanade.kill.Launch;
import kanade.kill.asm.Transformer;

import java.io.File;
import java.io.FileDescriptor;
import java.net.InetAddress;
import java.security.Permission;

public class KanadeSecurityManager extends SecurityManager {
    public static final KanadeSecurityManager INSTANCE = new KanadeSecurityManager();

    private KanadeSecurityManager() {
    }

    @Override
    public boolean getInCheck() {
        return false;
    }

    @Override
    public void checkPermission(Permission var1) {
        if (getClassContext()[2].getName().equals("kanade.kill.reflection.ReflectionUtil")) {
            return;
        }
        if (var1.getName().equals("setContextClassLoader")) {
            Launch.LOGGER.warn("Someone tries to modify the context classloader.");
            if (Transformer.isModClass(getClassContext()[1].getName())) {
                throw new SecurityException("No you can't replace the contextClassLoader");
            }
        }
        if (var1.getClass().getName().equals("com.sun.tools.attach.AttachPermission")) {
            Launch.LOGGER.warn("Prevent attach.");
            throw new SecurityException("Attach is not allowed.");
        }
    }

    @Override
    public void checkPermission(Permission var1, Object var2) {
    }

    @Override
    public void checkCreateClassLoader() {
    }

    @Override
    public void checkAccess(Thread var1) {
    }

    @Override
    public void checkAccess(ThreadGroup var1) {
    }

    @Override
    public void checkExit(int var1) {
    }

    @Override
    public void checkExec(String var1) {
    }

    @Override
    public void checkLink(String var1) {
        if (!var1.contains(File.separator) || var1.contains(System.getProperty("java.home"))) {
            return;
        }
        Launch.LOGGER.warn("Prevent native lib " + var1 + " from loading.");
        throw new SecurityException();
    }

    @Override
    public void checkRead(FileDescriptor var1) {
    }

    @Override
    public void checkRead(String var1) {
    }

    @Override
    public void checkRead(String var1, Object var2) {
    }

    @Override
    public void checkWrite(FileDescriptor var1) {
    }

    @Override
    public void checkWrite(String var1) {
    }

    @Override
    public void checkDelete(String var1) {
    }

    @Override
    public void checkConnect(String var1, int var2) {
    }

    @Override
    public void checkConnect(String var1, int var2, Object var3) {
    }

    @Override
    public void checkListen(int var1) {
    }

    @Override
    public void checkAccept(String var1, int var2) {
    }

    @Override
    public void checkMulticast(InetAddress var1) {
    }

    @Override
    public void checkMulticast(InetAddress var1, byte var2) {
    }

    @Override
    public void checkPropertiesAccess() {
    }

    @Override
    public void checkPropertyAccess(String var1) {
    }

    @Override
    public boolean checkTopLevelWindow(Object var1) {
        return true;
    }

    @Override
    public void checkPrintJobAccess() {
    }

    @Override
    public void checkSystemClipboardAccess() {
    }

    @Override
    public void checkAwtEventQueueAccess() {
    }

    @Override
    public void checkPackageAccess(String var1) {
    }

    @Override
    public void checkPackageDefinition(String var1) {
    }

    @Override
    public void checkSetFactory() {
    }

    @Override
    public void checkMemberAccess(Class<?> var1, int var2) {
    }

    @Override
    public void checkSecurityAccess(String var1) {
    }
}
