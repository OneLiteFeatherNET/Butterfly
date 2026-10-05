package net.onelitefeather.butterfly.minestom;

import org.slf4j.Logger;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Captures log calls in memory so tests can assert on them.
 */
final class RecordingLogger {

    private final List<String> errors = Collections.synchronizedList(new ArrayList<>());
    private final List<String> warnings = Collections.synchronizedList(new ArrayList<>());
    private final Logger logger = (Logger) Proxy.newProxyInstance(Logger.class.getClassLoader(), new Class<?>[]{Logger.class},
            (proxy, method, args) -> {
                String name = method.getName();
                if (name.equals("error") || name.equals("warn")) {
                    (name.equals("error") ? errors : warnings).add(Arrays.stream(args == null ? new Object[0] : args)
                            // varargs calls arrive as one Object[] argument
                            .flatMap(arg -> arg instanceof Object[] array ? Arrays.stream(array) : Stream.of(arg))
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

    List<String> warnings() {
        return List.copyOf(warnings);
    }

    List<String> errors() {
        return List.copyOf(errors);
    }
}
