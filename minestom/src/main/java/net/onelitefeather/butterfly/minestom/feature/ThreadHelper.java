package net.onelitefeather.butterfly.minestom.feature;

import java.util.function.Supplier;

public interface ThreadHelper {
    default void syncThreadForServiceLoader(Runnable runnable) {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        ClassLoader pluginClassLoader = this.getClass().getClassLoader();
        try {
            currentThread.setContextClassLoader(pluginClassLoader);
            runnable.run();
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    default <T> T syncThreadForServiceLoader(Supplier<T> supplier) {
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        ClassLoader pluginClassLoader = this.getClass().getClassLoader();
        try {
            currentThread.setContextClassLoader(pluginClassLoader);
            return supplier.get();
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }
}
