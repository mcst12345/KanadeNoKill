//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package sun.misc;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilePermission;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.CodeSigner;
import java.security.Permission;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;

import sun.net.util.URLUtil;
import sun.net.www.ParseUtil;
import sun.security.action.GetPropertyAction;

public class URLClassPath {
    static final String USER_AGENT_JAVA_VERSION = "UA-Java-Version";
    static final String JAVA_VERSION = AccessController.doPrivileged(new GetPropertyAction("java.version"));
    private static final boolean DEBUG = AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debug")) != null;
    private static final boolean DEBUG_LOOKUP_CACHE = AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.debugLookupCache")) != null;
    private static final boolean DISABLE_JAR_CHECKING;
    private static final boolean DISABLE_ACC_CHECKING;
    private static final boolean DISABLE_CP_URL_CHECK;
    private static final boolean DEBUG_CP_URL_CHECK;
    private ArrayList<URL> path;
    Stack<URL> urls;
    ArrayList<Loader> loaders;
    HashMap<String, Loader> lmap;
    public URLStreamHandler jarHandler;
    private boolean closed;
    private final AccessControlContext acc;
    private static volatile boolean lookupCacheEnabled;
    private URL[] lookupCacheURLs;
    private ClassLoader lookupCacheLoader;

    public URLClassPath(URL[] var1, URLStreamHandlerFactory var2, AccessControlContext var3) {
        this.path = new ArrayList();
        this.urls = new Stack();
        this.loaders = new ArrayList();
        this.lmap = new HashMap();
        this.closed = false;

        for (int var4 = 0; var4 < var1.length; ++var4) {
            this.path.add(var1[var4]);
        }

        this.push(var1);
        if (var2 != null) {
            this.jarHandler = var2.createURLStreamHandler("jar");
        }

        if (DISABLE_ACC_CHECKING) {
            this.acc = null;
        } else {
            this.acc = var3;
        }

    }

    public URLClassPath(URL[] var1) {
        this(var1, (URLStreamHandlerFactory) null, (AccessControlContext) null);
    }

    public URLClassPath(URL[] var1, AccessControlContext var2) {
        this(var1, (URLStreamHandlerFactory) null, var2);
    }

    public synchronized List<IOException> closeLoaders() {
        if (this.closed) {
            return Collections.emptyList();
        } else {
            LinkedList var1 = new LinkedList();
            Iterator var2 = this.loaders.iterator();

            while (var2.hasNext()) {
                Loader var3 = (Loader) var2.next();

                try {
                    var3.close();
                } catch (IOException var5) {
                    var1.add(var5);
                }
            }

            this.closed = true;
            return var1;
        }
    }

    public synchronized void addURL(URL var1) {
        if (!this.closed) {
            synchronized (this.urls) {
                if (var1 != null && !this.path.contains(var1)) {
                    this.urls.add(0, var1);
                    this.path.add(var1);
                    if (this.lookupCacheURLs != null) {
                        disableAllLookupCaches();
                    }

                }
            }
        }
    }

    public URL[] getURLs() {
        synchronized (this.urls) {
            return (URL[]) this.path.toArray(new URL[this.path.size()]);
        }
    }

    public URL findResource(String var1, boolean var2) {
        int[] var4 = this.getLookupCache(var1);

        Loader var3;
        for (int var5 = 0; (var3 = this.getNextLoader(var4, var5)) != null; ++var5) {
            URL var6 = var3.findResource(var1, var2);
            if (var6 != null) {
                return var6;
            }
        }

        return null;
    }

    public Resource getResource(String var1, boolean var2) {
        if (DEBUG) {
            System.err.println("URLClassPath.getResource(\"" + var1 + "\")");
        }

        int[] var4 = this.getLookupCache(var1);

        Loader var3;
        for (int var5 = 0; (var3 = this.getNextLoader(var4, var5)) != null; ++var5) {
            Resource var6 = var3.getResource(var1, var2);
            if (var6 != null) {
                return var6;
            }
        }

        return null;
    }

    public Enumeration<URL> findResources(final String var1, final boolean var2) {
        return new Enumeration<URL>() {
            private int index = 0;
            private int[] cache = URLClassPath.this.getLookupCache(var1);
            private URL url = null;

            private boolean next() {
                if (this.url != null) {
                    return true;
                } else {
                    do {
                        Loader var1x;
                        if ((var1x = URLClassPath.this.getNextLoader(this.cache, this.index++)) == null) {
                            return false;
                        }

                        this.url = var1x.findResource(var1, var2);
                    } while (this.url == null);

                    return true;
                }
            }

            public boolean hasMoreElements() {
                return this.next();
            }

            public URL nextElement() {
                if (!this.next()) {
                    throw new NoSuchElementException();
                } else {
                    URL var1x = this.url;
                    this.url = null;
                    return var1x;
                }
            }
        };
    }

    public Resource getResource(String var1) {
        return this.getResource(var1, true);
    }

    public Enumeration<Resource> getResources(final String var1, final boolean var2) {
        return new Enumeration<Resource>() {
            private int index = 0;
            private int[] cache = URLClassPath.this.getLookupCache(var1);
            private Resource res = null;

            private boolean next() {
                if (this.res != null) {
                    return true;
                } else {
                    do {
                        Loader var1x;
                        if ((var1x = URLClassPath.this.getNextLoader(this.cache, this.index++)) == null) {
                            return false;
                        }

                        this.res = var1x.getResource(var1, var2);
                    } while (this.res == null);

                    return true;
                }
            }

            public boolean hasMoreElements() {
                return this.next();
            }

            public Resource nextElement() {
                if (!this.next()) {
                    throw new NoSuchElementException();
                } else {
                    Resource var1x = this.res;
                    this.res = null;
                    return var1x;
                }
            }
        };
    }

    public Enumeration<Resource> getResources(String var1) {
        return this.getResources(var1, true);
    }

    synchronized void initLookupCache(ClassLoader var1) {
        if ((this.lookupCacheURLs = getLookupCacheURLs(var1)) != null) {
            this.lookupCacheLoader = var1;
        } else {
            disableAllLookupCaches();
        }

    }

    static void disableAllLookupCaches() {
        lookupCacheEnabled = false;
    }

    private static native URL[] getLookupCacheURLs(ClassLoader var0);

    private static native int[] getLookupCacheForClassLoader(ClassLoader var0, String var1);

    private static native boolean knownToNotExist0(ClassLoader var0, String var1);

    synchronized boolean knownToNotExist(String var1) {
        return this.lookupCacheURLs != null && lookupCacheEnabled ? knownToNotExist0(this.lookupCacheLoader, var1) : false;
    }

    private synchronized int[] getLookupCache(String var1) {
        if (this.lookupCacheURLs != null && lookupCacheEnabled) {
            int[] var2 = getLookupCacheForClassLoader(this.lookupCacheLoader, var1);
            if (var2 != null && var2.length > 0) {
                int var3 = var2[var2.length - 1];
                if (!this.ensureLoaderOpened(var3)) {
                    if (DEBUG_LOOKUP_CACHE) {
                        System.out.println("Expanded loaders FAILED " + this.loaders.size() + " for maxindex=" + var3);
                    }

                    return null;
                }
            }

            return var2;
        } else {
            return null;
        }
    }

    private boolean ensureLoaderOpened(int var1) {
        if (this.loaders.size() <= var1) {
            if (this.getLoader(var1) == null) {
                return false;
            }

            if (!lookupCacheEnabled) {
                return false;
            }

            if (DEBUG_LOOKUP_CACHE) {
                System.out.println("Expanded loaders " + this.loaders.size() + " to index=" + var1);
            }
        }

        return true;
    }

    private synchronized void validateLookupCache(int var1, String var2) {
        if (this.lookupCacheURLs != null && lookupCacheEnabled) {
            if (var1 < this.lookupCacheURLs.length && var2.equals(URLUtil.urlNoFragString(this.lookupCacheURLs[var1]))) {
                return;
            }

            if (DEBUG || DEBUG_LOOKUP_CACHE) {
                System.out.println("WARNING: resource lookup cache invalidated for lookupCacheLoader at " + var1);
            }

            disableAllLookupCaches();
        }

    }

    private synchronized Loader getNextLoader(int[] var1, int var2) {
        if (this.closed) {
            return null;
        } else if (var1 != null) {
            if (var2 < var1.length) {
                Loader var3 = (Loader) this.loaders.get(var1[var2]);
                if (DEBUG_LOOKUP_CACHE) {
                    System.out.println("HASCACHE: Loading from : " + var1[var2] + " = " + var3.getBaseURL());
                }

                return var3;
            } else {
                return null;
            }
        } else {
            return this.getLoader(var2);
        }
    }

    private synchronized Loader getLoader(int var1) {
        if (this.closed) {
            return null;
        } else {
            while (this.loaders.size() < var1 + 1) {
                URL var2;
                synchronized (this.urls) {
                    if (this.urls.empty()) {
                        return null;
                    }

                    var2 = (URL) this.urls.pop();
                }

                String var3 = URLUtil.urlNoFragString(var2);
                if (!this.lmap.containsKey(var3)) {
                    Loader var4;
                    try {
                        var4 = this.getLoader(var2);
                        URL[] var5 = var4.getClassPath();
                        if (var5 != null) {
                            this.push(var5);
                        }
                    } catch (IOException var6) {
                        continue;
                    } catch (SecurityException var7) {
                        if (DEBUG) {
                            System.err.println("Failed to access " + var2 + ", " + var7);
                        }
                        continue;
                    }

                    this.validateLookupCache(this.loaders.size(), var3);
                    this.loaders.add(var4);
                    this.lmap.put(var3, var4);
                }
            }

            if (DEBUG_LOOKUP_CACHE) {
                System.out.println("NOCACHE: Loading from : " + var1);
            }

            return (Loader) this.loaders.get(var1);
        }
    }

    private Loader getLoader(final URL var1) throws IOException {
        try {
            return (Loader) AccessController.doPrivileged(new PrivilegedExceptionAction<Loader>() {
                public Loader run() throws IOException {
                    String var1x = var1.getFile();
                    if (var1x != null && var1x.endsWith("/")) {
                        return (Loader) ("file".equals(var1.getProtocol()) ? new FileLoader(var1) : new Loader(var1));
                    } else {
                        return new JarLoader(var1, URLClassPath.this.jarHandler, URLClassPath.this.lmap, URLClassPath.this.acc);
                    }
                }
            }, this.acc);
        } catch (PrivilegedActionException var3) {
            throw (IOException) var3.getException();
        }
    }

    private void push(URL[] var1) {
        synchronized (this.urls) {
            for (int var3 = var1.length - 1; var3 >= 0; --var3) {
                this.urls.push(var1[var3]);
            }

        }
    }

    public URL checkURL(URL var1) {
        try {
            check(var1);
            return var1;
        } catch (Exception var3) {
            return null;
        }
    }

    static void check(URL var0) throws IOException {
        SecurityManager var1 = System.getSecurityManager();
        if (var1 != null) {
            URLConnection var2 = var0.openConnection();
            Permission var3 = var2.getPermission();
            if (var3 != null) {
                try {
                    var1.checkPermission(var3);
                } catch (SecurityException var6) {
                    if (var3 instanceof FilePermission && var3.getActions().indexOf("read") != -1) {
                        var1.checkRead(var3.getName());
                    } else {
                        if (!(var3 instanceof SocketPermission) || var3.getActions().indexOf("connect") == -1) {
                            throw var6;
                        }

                        URL var5 = var0;
                        if (var2 instanceof JarURLConnection) {
                            var5 = ((JarURLConnection) var2).getJarFileURL();
                        }

                        var1.checkConnect(var5.getHost(), var5.getPort());
                    }
                }
            }
        }

    }

    static {
        String var0 = (String) AccessController.doPrivileged(new GetPropertyAction("sun.misc.URLClassPath.disableJarChecking"));
        DISABLE_JAR_CHECKING = var0 != null ? var0.equals("true") || var0.equals("") : false;
        var0 = (String) AccessController.doPrivileged(new GetPropertyAction("jdk.net.URLClassPath.disableRestrictedPermissions"));
        DISABLE_ACC_CHECKING = var0 != null ? var0.equals("true") || var0.equals("") : false;
        var0 = (String) AccessController.doPrivileged(new GetPropertyAction("jdk.net.URLClassPath.disableClassPathURLCheck", "true"));
        DISABLE_CP_URL_CHECK = var0 != null ? var0.equals("true") || var0.isEmpty() : false;
        DEBUG_CP_URL_CHECK = "debug".equals(var0);
        lookupCacheEnabled = "true".equals(VM.getSavedProperty("sun.cds.enableSharedLookupCache"));
    }

    private static class FileLoader extends Loader {
        private File dir;

        FileLoader(URL var1) throws IOException {
            super(var1);
            if (!"file".equals(var1.getProtocol())) {
                throw new IllegalArgumentException("url");
            } else {
                String var2 = var1.getFile().replace('/', File.separatorChar);
                var2 = ParseUtil.decode(var2);
                this.dir = (new File(var2)).getCanonicalFile();
            }
        }

        URL findResource(String var1, boolean var2) {
            Resource var3 = this.getResource(var1, var2);
            return var3 != null ? var3.getURL() : null;
        }

        Resource getResource(final String var1, boolean var2) {
            try {
                URL var4 = new URL(this.getBaseURL(), ".");
                final URL var3 = new URL(this.getBaseURL(), ParseUtil.encodePath(var1, false));
                if (!var3.getFile().startsWith(var4.getFile())) {
                    return null;
                } else {
                    if (var2) {
                        URLClassPath.check(var3);
                    }

                    final File var5;
                    if (var1.indexOf("..") != -1) {
                        var5 = (new File(this.dir, var1.replace('/', File.separatorChar))).getCanonicalFile();
                        if (!var5.getPath().startsWith(this.dir.getPath())) {
                            return null;
                        }
                    } else {
                        var5 = new File(this.dir, var1.replace('/', File.separatorChar));
                    }

                    return var5.exists() ? new Resource() {
                        public String getName() {
                            return var1;
                        }

                        public URL getURL() {
                            return var3;
                        }

                        public URL getCodeSourceURL() {
                            return FileLoader.this.getBaseURL();
                        }

                        public InputStream getInputStream() throws IOException {
                            return new FileInputStream(var5);
                        }

                        public int getContentLength() throws IOException {
                            return (int) var5.length();
                        }
                    } : null;
                }
            } catch (Exception var6) {
                return null;
            }
        }
    }

    static class JarLoader extends Loader {
        private JarFile jar;
        private final URL csu;
        private JarIndex index;
        private MetaIndex metaIndex;
        private URLStreamHandler handler;
        private final HashMap<String, Loader> lmap;
        private final AccessControlContext acc;
        private boolean closed = false;
        private static final JavaUtilZipFileAccess zipAccess = SharedSecrets.getJavaUtilZipFileAccess();

        JarLoader(URL var1, URLStreamHandler var2, HashMap<String, Loader> var3, AccessControlContext var4) throws IOException {
            super(new URL("jar", "", -1, var1 + "!/", var2));
            this.csu = var1;
            this.handler = var2;
            this.lmap = var3;
            this.acc = var4;
            if (!this.isOptimizable(var1)) {
                this.ensureOpen();
            } else {
                String var5 = var1.getFile();
                if (var5 != null) {
                    var5 = ParseUtil.decode(var5);
                    File var6 = new File(var5);
                    this.metaIndex = MetaIndex.forJar(var6);
                    if (this.metaIndex != null && !var6.exists()) {
                        this.metaIndex = null;
                    }
                }

                if (this.metaIndex == null) {
                    this.ensureOpen();
                }
            }

        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                this.ensureOpen();
                this.jar.close();
            }

        }

        JarFile getJarFile() {
            return this.jar;
        }

        private boolean isOptimizable(URL var1) {
            return "file".equals(var1.getProtocol());
        }

        private void ensureOpen() throws IOException {
            if (this.jar == null) {
                try {
                    AccessController.doPrivileged(new PrivilegedExceptionAction<Void>() {
                        public Void run() throws IOException {
                            if (URLClassPath.DEBUG) {
                                System.err.println("Opening " + JarLoader.this.csu);
                                Thread.dumpStack();
                            }

                            JarLoader.this.jar = JarLoader.this.getJarFile(JarLoader.this.csu);
                            JarLoader.this.index = JarIndex.getJarIndex(JarLoader.this.jar, JarLoader.this.metaIndex);
                            if (JarLoader.this.index != null) {
                                String[] var1 = JarLoader.this.index.getJarFiles();

                                for (int var2 = 0; var2 < var1.length; ++var2) {
                                    try {
                                        URL var3 = new URL(JarLoader.this.csu, var1[var2]);
                                        String var4 = URLUtil.urlNoFragString(var3);
                                        if (!JarLoader.this.lmap.containsKey(var4)) {
                                            JarLoader.this.lmap.put(var4, null);
                                        }
                                    } catch (MalformedURLException var5) {
                                    }
                                }
                            }

                            return null;
                        }
                    }, this.acc);
                } catch (PrivilegedActionException var2) {
                    throw (IOException) var2.getException();
                }
            }

        }

        static JarFile checkJar(JarFile var0) throws IOException {
            if (System.getSecurityManager() != null && !URLClassPath.DISABLE_JAR_CHECKING && !zipAccess.startsWithLocHeader(var0)) {
                IOException var1 = new IOException("Invalid Jar file");

                try {
                    var0.close();
                } catch (IOException var3) {
                    var1.addSuppressed(var3);
                }

                throw var1;
            } else {
                return var0;
            }
        }

        private JarFile getJarFile(URL var1) throws IOException {
            if (this.isOptimizable(var1)) {
                FileURLMapper var4 = new FileURLMapper(var1);
                if (!var4.exists()) {
                    throw new FileNotFoundException(var4.getPath());
                } else {
                    return checkJar(new JarFile(var4.getPath()));
                }
            } else {
                URLConnection var2 = this.getBaseURL().openConnection();
                var2.setRequestProperty("UA-Java-Version", URLClassPath.JAVA_VERSION);
                JarFile var3 = ((JarURLConnection) var2).getJarFile();
                return checkJar(var3);
            }
        }

        JarIndex getIndex() {
            try {
                this.ensureOpen();
            } catch (IOException var2) {
                throw new InternalError(var2);
            }

            return this.index;
        }

        Resource checkResource(final String var1, boolean var2, final JarEntry var3) {
            final URL var4;
            try {
                var4 = new URL(this.getBaseURL(), ParseUtil.encodePath(var1, false));
                if (var2) {
                    URLClassPath.check(var4);
                }
            } catch (MalformedURLException var6) {
                return null;
            } catch (IOException var7) {
                return null;
            } catch (AccessControlException var8) {
                return null;
            }

            return new Resource() {
                private Exception dataError = null;

                public String getName() {
                    return var1;
                }

                public URL getURL() {
                    return var4;
                }

                public URL getCodeSourceURL() {
                    return JarLoader.this.csu;
                }

                public InputStream getInputStream() throws IOException {
                    return JarLoader.this.jar.getInputStream(var3);
                }

                public int getContentLength() {
                    return (int) var3.getSize();
                }

                public Manifest getManifest() throws IOException {
                    SharedSecrets.javaUtilJarAccess().ensureInitialization(JarLoader.this.jar);
                    return JarLoader.this.jar.getManifest();
                }

                public Certificate[] getCertificates() {
                    return var3.getCertificates();
                }

                public CodeSigner[] getCodeSigners() {
                    return var3.getCodeSigners();
                }

                public Exception getDataError() {
                    return this.dataError;
                }

                public byte[] getBytes() throws IOException {
                    byte[] var1x = super.getBytes();
                    CRC32 var2 = new CRC32();
                    var2.update(var1x);
                    if (var2.getValue() != var3.getCrc()) {
                        this.dataError = new IOException("CRC error while extracting entry from JAR file");
                    }

                    return var1x;
                }
            };
        }

        boolean validIndex(String var1) {
            String var2 = var1;
            int var3;
            if ((var3 = var1.lastIndexOf("/")) != -1) {
                var2 = var1.substring(0, var3);
            }

            Enumeration var6 = this.jar.entries();

            String var4;
            do {
                if (!var6.hasMoreElements()) {
                    return false;
                }

                ZipEntry var5 = (ZipEntry) var6.nextElement();
                var4 = var5.getName();
                if ((var3 = var4.lastIndexOf("/")) != -1) {
                    var4 = var4.substring(0, var3);
                }
            } while (!var4.equals(var2));

            return true;
        }

        URL findResource(String var1, boolean var2) {
            Resource var3 = this.getResource(var1, var2);
            return var3 != null ? var3.getURL() : null;
        }

        Resource getResource(String var1, boolean var2) {
            if (this.metaIndex != null && !this.metaIndex.mayContain(var1)) {
                return null;
            } else {
                try {
                    this.ensureOpen();
                } catch (IOException var5) {
                    throw new InternalError(var5);
                }

                JarEntry var3 = this.jar.getJarEntry(var1);
                if (var3 != null) {
                    return this.checkResource(var1, var2, var3);
                } else if (this.index == null) {
                    return null;
                } else {
                    HashSet var4 = new HashSet();
                    return this.getResource(var1, var2, var4);
                }
            }
        }

        Resource getResource(String var1, boolean var2, Set<String> var3) {
            int var6 = 0;
            LinkedList var7 = null;
            if ((var7 = this.index.get(var1)) == null) {
                return null;
            } else {
                do {
                    int var8 = var7.size();
                    String[] var5 = (String[]) var7.toArray(new String[var8]);

                    while (var6 < var8) {
                        String var9 = var5[var6++];

                        JarLoader var10;
                        final URL var11;
                        try {
                            var11 = new URL(this.csu, var9);
                            String var12 = URLUtil.urlNoFragString(var11);
                            if ((var10 = (JarLoader) this.lmap.get(var12)) == null) {
                                var10 = (JarLoader) AccessController.doPrivileged(new PrivilegedExceptionAction<JarLoader>() {
                                    public JarLoader run() throws IOException {
                                        return new JarLoader(var11, JarLoader.this.handler, JarLoader.this.lmap, JarLoader.this.acc);
                                    }
                                }, this.acc);
                                JarIndex var13 = var10.getIndex();
                                if (var13 != null) {
                                    int var14 = var9.lastIndexOf("/");
                                    var13.merge(this.index, var14 == -1 ? null : var9.substring(0, var14 + 1));
                                }

                                this.lmap.put(var12, var10);
                            }
                        } catch (PrivilegedActionException var16) {
                            continue;
                        } catch (MalformedURLException var17) {
                            continue;
                        }

                        boolean var18 = !var3.add(URLUtil.urlNoFragString(var11));
                        if (!var18) {
                            try {
                                var10.ensureOpen();
                            } catch (IOException var15) {
                                throw new InternalError(var15);
                            }

                            JarEntry var19 = var10.jar.getJarEntry(var1);
                            if (var19 != null) {
                                return var10.checkResource(var1, var2, var19);
                            }

                            if (!var10.validIndex(var1)) {
                                throw new InvalidJarIndexException("Invalid index");
                            }
                        }

                        Resource var4;
                        if (!var18 && var10 != this && var10.getIndex() != null && (var4 = var10.getResource(var1, var2, var3)) != null) {
                            return var4;
                        }
                    }

                    var7 = this.index.get(var1);
                } while (var6 < var7.size());

                return null;
            }
        }

        URL[] getClassPath() throws IOException {
            if (this.index != null) {
                return null;
            } else if (this.metaIndex != null) {
                return null;
            } else {
                this.ensureOpen();
                this.parseExtensionsDependencies();
                if (SharedSecrets.javaUtilJarAccess().jarFileHasClassPathAttribute(this.jar)) {
                    Manifest var1 = this.jar.getManifest();
                    if (var1 != null) {
                        Attributes var2 = var1.getMainAttributes();
                        if (var2 != null) {
                            String var3 = var2.getValue(Name.CLASS_PATH);
                            if (var3 != null) {
                                return this.parseClassPath(this.csu, var3);
                            }
                        }
                    }
                }

                return null;
            }
        }

        private void parseExtensionsDependencies() throws IOException {
            ExtensionDependency.checkExtensionsDependencies(this.jar);
        }

        private URL[] parseClassPath(URL var1, String var2) throws MalformedURLException {
            StringTokenizer var3 = new StringTokenizer(var2);
            URL[] var4 = new URL[var3.countTokens()];
            int var5 = 0;

            while (var3.hasMoreTokens()) {
                String var6 = var3.nextToken();
                URL var7 = URLClassPath.DISABLE_CP_URL_CHECK ? new URL(var1, var6) : tryResolve(var1, var6);
                if (var7 != null) {
                    var4[var5] = var7;
                    ++var5;
                } else if (URLClassPath.DEBUG_CP_URL_CHECK) {
                    System.err.println("Class-Path entry: \"" + var6 + "\" ignored in JAR file " + var1);
                }
            }

            if (var5 == 0) {
                var4 = null;
            } else if (var5 != var4.length) {
                var4 = (URL[]) Arrays.copyOf(var4, var5);
            }

            return var4;
        }

        static URL tryResolve(URL var0, String var1) throws MalformedURLException {
            return "file".equalsIgnoreCase(var0.getProtocol()) ? tryResolveFile(var0, var1) : tryResolveNonFile(var0, var1);
        }

        static URL tryResolveFile(URL var0, String var1) throws MalformedURLException {
            int var2 = var1.indexOf(58);
            boolean var3;
            if (var2 >= 0) {
                String var4 = var1.substring(0, var2);
                var3 = "file".equalsIgnoreCase(var4);
            } else {
                var3 = true;
            }

            return var3 ? new URL(var0, var1) : null;
        }

        static URL tryResolveNonFile(URL var0, String var1) throws MalformedURLException {
            String var2 = var1.replace(File.separatorChar, '/');
            if (isRelative(var2)) {
                URL var3 = new URL(var0, var2);
                String var4 = var0.getPath();
                String var5 = var3.getPath();
                int var6 = var4.lastIndexOf(47);
                if (var6 == -1) {
                    var6 = var4.length() - 1;
                }

                if (var5.regionMatches(0, var4, 0, var6 + 1) && var5.indexOf("..", var6) == -1) {
                    return var3;
                }
            }

            return null;
        }

        static boolean isRelative(String var0) {
            try {
                return !URI.create(var0).isAbsolute();
            } catch (IllegalArgumentException var2) {
                return false;
            }
        }
    }

    private static class Loader implements Closeable {
        private final URL base;
        private JarFile jarfile;

        Loader(URL var1) {
            this.base = var1;
        }

        URL getBaseURL() {
            return this.base;
        }

        URL findResource(String var1, boolean var2) {
            URL var3;
            try {
                var3 = new URL(this.base, ParseUtil.encodePath(var1, false));
            } catch (MalformedURLException var7) {
                throw new IllegalArgumentException("name");
            }

            try {
                if (var2) {
                    URLClassPath.check(var3);
                }

                URLConnection var4 = var3.openConnection();
                if (var4 instanceof HttpURLConnection) {
                    HttpURLConnection var5 = (HttpURLConnection) var4;
                    var5.setRequestMethod("HEAD");
                    if (var5.getResponseCode() >= 400) {
                        return null;
                    }
                } else {
                    var4.setUseCaches(false);
                    InputStream var8 = var4.getInputStream();
                    var8.close();
                }

                return var3;
            } catch (Exception var6) {
                return null;
            }
        }

        Resource getResource(final String var1, boolean var2) {
            final URL var3;
            try {
                var3 = new URL(this.base, ParseUtil.encodePath(var1, false));
            } catch (MalformedURLException var6) {
                throw new IllegalArgumentException("name");
            }

            final URLConnection var4;
            try {
                if (var2) {
                    URLClassPath.check(var3);
                }

                var4 = var3.openConnection();
                if (var4 instanceof JarURLConnection) {
                    JarURLConnection var5 = (JarURLConnection) var4;
                    this.jarfile = URLClassPath.JarLoader.checkJar(var5.getJarFile());
                }

                InputStream var8 = var4.getInputStream();
            } catch (Exception var7) {
                return null;
            }

            return new Resource() {
                public String getName() {
                    return var1;
                }

                public URL getURL() {
                    return var3;
                }

                public URL getCodeSourceURL() {
                    return Loader.this.base;
                }

                public InputStream getInputStream() throws IOException {
                    return var4.getInputStream();
                }

                public int getContentLength() throws IOException {
                    return var4.getContentLength();
                }
            };
        }

        Resource getResource(String var1) {
            return this.getResource(var1, true);
        }

        public void close() throws IOException {
            if (this.jarfile != null) {
                this.jarfile.close();
            }

        }

        URL[] getClassPath() throws IOException {
            return null;
        }
    }
}
