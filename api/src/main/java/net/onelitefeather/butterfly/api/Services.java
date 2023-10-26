package net.onelitefeather.butterfly.api;

import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceLoader;

public final class Services {

    private Services() {}

    /**
     * Locates a service.
     *
     * @param type the service type
     * @param <P> the service type
     * @return a service, or {@link Optional#empty()}
     * @since 4.8.0
     */
    public static <P> Optional<P> service(final Class<P> type) {
        final ServiceLoader<P> loader = ServiceLoader.load(type, type.getClassLoader());
        final Iterator<P> it = loader.iterator();
        while (it.hasNext()) {
            final P instance;
            try {
                instance = it.next();
            } catch (final Throwable t) {
                throw new IllegalStateException("Encountered an exception loading service " + type, t);
            }
            if (it.hasNext()) {
                throw new IllegalStateException("Expected to find one service " + type + ", found multiple");
            }
            return Optional.of(instance);
        }
        return Optional.empty();
    }
}
