package net.onelitefeather.butterfly.minestom.feature;

import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.manager.FeatureManagerBuilder;
import org.togglz.core.repository.composite.CompositeStateRepository;
import org.togglz.core.repository.file.FileBasedStateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.spi.FeatureManagerProvider;
import org.togglz.core.user.thread.ThreadLocalUserProvider;

import java.io.File;

public final class SingletonFeatureManagerProvider implements FeatureManagerProvider {

    private static FeatureManager featureManager;
    private static final File FLAGS = new File("flags.properties");

    @Override
    public FeatureManager getFeatureManager() {
        if (featureManager == null) {
            featureManager = createManager(FLAGS);
        }

        return featureManager;
    }

    /** A {@code null} file means defaults only, backed by the in-memory repository. */
    static FeatureManager createManager(File flagsFile) {
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        // the builder discovers activation strategies through ServiceLoader and the context class loader
        thread.setContextClassLoader(SingletonFeatureManagerProvider.class.getClassLoader());
        try {
            return new FeatureManagerBuilder()
                    .featureEnum(ButterflyFeatures.class)
                    .stateRepository(flagsFile == null
                            ? new InMemoryStateRepository()
                            : new CompositeStateRepository(
                                    new FileBasedStateRepository(flagsFile),
                                    new InMemoryStateRepository()
                            ))
                    .userProvider(new ThreadLocalUserProvider())
                    .activationStrategyProvider(new DefaultActivationStrategyProvider())
                    .build();
        } finally {
            thread.setContextClassLoader(original);
        }
    }

    @Override
    public int priority() {
        return 30;
    }
}
