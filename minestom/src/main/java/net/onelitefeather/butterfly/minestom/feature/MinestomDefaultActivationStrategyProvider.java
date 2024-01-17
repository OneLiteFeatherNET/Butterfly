package net.onelitefeather.butterfly.minestom.feature;

import java.util.ServiceLoader;
import org.togglz.core.activation.DefaultActivationStrategyProvider;
import org.togglz.core.spi.ActivationStrategy;

public class MinestomDefaultActivationStrategyProvider extends DefaultActivationStrategyProvider {

    public MinestomDefaultActivationStrategyProvider(ClassLoader classLoader) {
        for (ActivationStrategy activationStrategy : ServiceLoader.load(ActivationStrategy.class, classLoader)) {
            addActivationStrategy(activationStrategy);
        }
    }

}
