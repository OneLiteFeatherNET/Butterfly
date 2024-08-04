package net.onelitefeather.butterfly.bukkit.feature;

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
            featureManager = new FeatureManagerBuilder()
                    .featureEnum(ButterflyFeatures.class)
                    .stateRepository(new CompositeStateRepository(
                            new FileBasedStateRepository(FLAGS),
                            new InMemoryStateRepository()
                    ))
                    .userProvider(new ThreadLocalUserProvider())
                    .activationStrategyProvider(new DefaultActivationStrategyProvider())
                    .build();
        }

        return featureManager;
    }

    @Override
    public int priority() {
        return 30;
    }
}
