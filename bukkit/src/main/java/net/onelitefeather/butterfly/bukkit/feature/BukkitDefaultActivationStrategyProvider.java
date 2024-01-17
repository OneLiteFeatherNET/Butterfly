package net.onelitefeather.butterfly.bukkit.feature;

import java.util.ServiceLoader;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.spi.ActivationStrategy;

public class BukkitDefaultActivationStrategyProvider extends DefaultActivationStrategyProvider {

    public BukkitDefaultActivationStrategyProvider(ClassLoader classLoader) {
        for (ActivationStrategy activationStrategy : ServiceLoader.load(ActivationStrategy.class, classLoader)) {
            addActivationStrategy(activationStrategy);
        }
    }

}
