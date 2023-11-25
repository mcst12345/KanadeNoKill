/*
 * Copyright (c) 2003, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */


package sun.instrument;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.AccessibleObject;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;

@SuppressWarnings("unused")
public class InstrumentationImpl implements Instrumentation {
    private final TransformerManager mTransformerManager = null;
    // needs to store a native pointer, so use 64 bits
    private final long mNativeAgent = 0;
    private final boolean mEnvironmentSupportsRedefineClasses = false;
    private final boolean mEnvironmentSupportsNativeMethodPrefix = false;
    private TransformerManager mRetransfomableTransformerManager;
    private volatile boolean mEnvironmentSupportsRetransformClassesKnown = false;
    private volatile boolean mEnvironmentSupportsRetransformClasses = false;

    private InstrumentationImpl(long nativeAgent,
                                boolean environmentSupportsRedefineClasses,
                                boolean environmentSupportsNativeMethodPrefix) {
    }

    // Enable or disable Java programming language access checks on a
    // reflected object (for example, a method)
    private static void setAccessible(final AccessibleObject ao, final boolean accessible) {
    }

    public void
    addTransformer(ClassFileTransformer transformer) {
    }

    public synchronized void
    addTransformer(ClassFileTransformer transformer, boolean canRetransform) {
    }

    public synchronized boolean
    removeTransformer(ClassFileTransformer transformer) {
        if (transformer == null) {
            throw new NullPointerException("null passed as 'transformer' in removeTransformer");
        }
        TransformerManager mgr = findTransformerManager(transformer);
        if (mgr != null) {
            mgr.removeTransformer(transformer);
            if (mgr.isRetransformable() && mgr.getTransformerCount() == 0) {
                setHasRetransformableTransformers(mNativeAgent, false);
            }
            return true;
        }
        return false;
    }

    public boolean
    isModifiableClass(Class<?> theClass) {
        return false;
    }

    public boolean
    isRetransformClassesSupported() {
        return false;
    }

    public void
    retransformClasses(Class<?>... classes) {
    }

    public boolean
    isRedefineClassesSupported() {
        return false;
    }

    public void
    redefineClasses(ClassDefinition... definitions) {
    }

    @SuppressWarnings("rawtypes")
    public Class[]
    getAllLoadedClasses() {
        return null;
    }

    @SuppressWarnings("rawtypes")
    public Class[]
    getInitiatedClasses(ClassLoader loader) {
        return null;
    }

    public long
    getObjectSize(Object objectToSize) {
        return 0;
    }

    public void
    appendToBootstrapClassLoaderSearch(JarFile jarfile) {
    }

    public void
    appendToSystemClassLoaderSearch(JarFile jarfile) {
    }

    public boolean
    isNativeMethodPrefixSupported() {
        return false;
    }

    public synchronized void
    setNativeMethodPrefix(ClassFileTransformer transformer, String prefix) {
    }

    private TransformerManager
    findTransformerManager(ClassFileTransformer transformer) {
        return null;
    }

    /*
     *  Natives
     */
    private boolean
    isModifiableClass0(long nativeAgent, Class<?> theClass) {
        return false;
    }

    private boolean
    isRetransformClassesSupported0(long nativeAgent) {
        return false;
    }

    private void
    setHasRetransformableTransformers(long nativeAgent, boolean has) {

    }

    private void
    retransformClasses0(long nativeAgent, Class<?>[] classes) {

    }

    private void
    redefineClasses0(long nativeAgent, ClassDefinition[] definitions)
            throws ClassNotFoundException {

    }

    @SuppressWarnings("rawtypes")
    private Class[]
    getAllLoadedClasses0(long nativeAgent) {
        return new Class[0];
    }

    @SuppressWarnings("rawtypes")
    private Class[]
    getInitiatedClasses0(long nativeAgent, ClassLoader loader) {
        return new Class[0];
    }

    private long
    getObjectSize0(long nativeAgent, Object objectToSize) {
        return 0;
    }

    private void
    appendToClassLoaderSearch0(long nativeAgent, String jarfile, boolean bootLoader) {

    }

    /*
     *  Internals
     */

    private void
    setNativeMethodPrefixes(long nativeAgent, String[] prefixes, boolean isRetransformable) {

    }

    // Attempt to load and start an agent
    private void
    loadClassAndStartAgent(String classname,
                           String methodname,
                           String optionsString) {

    }

    // WARNING: the native code knows the name & signature of this method
    private void
    loadClassAndCallPremain(String classname,
                            String optionsString) {

    }


    // WARNING: the native code knows the name & signature of this method
    private void
    loadClassAndCallAgentmain(String classname,
                              String optionsString) {
    }

    // WARNING: the native code knows the name & signature of this method
    private byte[]
    transform(ClassLoader loader,
              String classname,
              Class<?> classBeingRedefined,
              ProtectionDomain protectionDomain,
              byte[] classfileBuffer,
              boolean isRetransformer) {

        return classfileBuffer;
    }
}
