package net.onelitefeather.butterfly.minestom;

import org.slf4j.Logger;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Captures log calls in memory so tests can assert on them.
 */
final class RecordingLogger {

    private final List<String> errors = Collections.synchronizedList(new ArrayList<>());
    private final Logger logger = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class<?>[]{Logger.class},
            (proxy, method, args) -> {
                String name = method.getName();
                if (name.equals("error")) {
                    errors.add(Arrays.stream(args == null ? new Object[0] : args)
                            .map(arg -> arg instanceof Throwable t ? t.toString() : String.valueOf(arg))
                            .reduce("", (a, b) -> a.isEmpty() ? b : a + " " + b));
                }
                if (name.startsWith("is") && name.endsWith("Enabled")) return true;
                if (method.getReturnType() == boolean.class) return false;
                if (name.equals("getName")) return "test";
                return null;
            });

    Logger logger() {
        return logger;
    }

    List<String> errors() {
        return List.copyOf(errors);
    }
}
