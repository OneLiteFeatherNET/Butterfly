package net.onelitefeather.butterfly.minestom;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Minimal interface stubs backed by {@link Proxy}. Unstubbed methods fail loudly.
 */
final class Stubs {

    private Stubs() {
    }

    static <T> T of(Class<T> type, Map<String, Function<Object[], Object>> answers) {
        Map<String, Function<Object[], Object>> table = new HashMap<>(answers);
        InvocationHandler handler = (proxy, method, args) -> {
            Function<Object[], Object> answer = table.get(method.getName());
            if (answer != null) return answer.apply(args == null ? new Object[0] : args);
            return switch (method.getName()) {
                case "toString" -> type.getSimpleName() + "Stub";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> throw new UnsupportedOperationException(type.getSimpleName() + "#" + method.getName() + " is not stubbed");
            };
        };
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, handler));
    }
}
