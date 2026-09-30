package net.onelitefeather.butterfly.minestom;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Child-first class loader that mimics an extension class loader: Butterfly and Togglz classes come from
 * this loader, everything else from the parent. Requests for blocked prefixes fail.
 */
final class IsolatedClassLoader extends URLClassLoader {

    private static final List<String> OWN_PREFIXES = List.of("net.onelitefeather.butterfly.", "org.togglz.");

    private final List<String> blockedPrefixes;
    private final List<String> requested = Collections.synchronizedList(new ArrayList<>());

    IsolatedClassLoader(URL[] urls, ClassLoader parent, String... blockedPrefixes) {
        super(urls, parent);
        this.blockedPrefixes = List.of(blockedPrefixes);
    }

    static URL locationOf(Class<?> type) {
        return type.getProtectionDomain().getCodeSource().getLocation();
    }

    List<String> requestedClasses() {
        return List.copyOf(requested);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        requested.add(name);
        if (blockedPrefixes.stream().anyMatch(name::startsWith)) {
            throw new ClassNotFoundException(name + " is blocked in this class loader");
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null && OWN_PREFIXES.stream().anyMatch(name::startsWith)) {
                loaded = findClass(name);
            }
            if (loaded == null) {
                return super.loadClass(name, resolve);
            }
            if (resolve) resolveClass(loaded);
            return loaded;
        }
    }
}
