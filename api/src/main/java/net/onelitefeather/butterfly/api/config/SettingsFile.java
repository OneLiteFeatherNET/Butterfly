package net.onelitefeather.butterfly.api.config;

import io.avaje.config.Configuration;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads {@link ButterflySettings} from the {@code config.yaml} in a platform's data folder.
 *
 * <p>Only that one file and the JVM system properties are read. avaje-config's own lookup of
 * {@code application.yaml} on the classpath or in the working directory is never switched on, so settings of
 * other software cannot leak in.
 */
public final class SettingsFile {

    static final String FILE_NAME = "config.yaml";
    private static final String SYSTEM_PROPERTY_PREFIX = "butterfly.";

    private SettingsFile() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Loads the settings, writing the bundled default file first when the data folder has none. A folder or
     * file that cannot be used is reported with a warning and the defaults apply.
     *
     * @param dataFolder      folder that holds {@code config.yaml}, created when missing
     * @param defaultResource classpath resource with the commented default file to write on first start
     * @param legacyFlagsFile where the previous version kept {@code flags.properties}; only checked for existence
     * @param logger          receives the warnings
     */
    public static ButterflySettings load(Path dataFolder, String defaultResource, Path legacyFlagsFile, Logger logger) {
        return load(dataFolder, defaultResource, legacyFlagsFile, logger, butterflySystemProperties());
    }

    static ButterflySettings load(Path dataFolder, String defaultResource, Path legacyFlagsFile, Logger logger,
                                  Map<String, String> systemProperties) {
        if (Files.exists(legacyFlagsFile)) {
            logger.warn("{} is no longer read, set {} in {} instead",
                    legacyFlagsFile, ButterflySettings.TEAM_COLLISION_KEY, dataFolder.resolve(FILE_NAME));
        }
        Path file = dataFolder.resolve(FILE_NAME);
        Configuration.Builder builder = Configuration.builder();
        try {
            writeDefaultFileIfAbsent(dataFolder, file, defaultResource);
            builder.load(file.toFile());
        } catch (IOException | RuntimeException e) {
            logger.warn("Cannot use data folder {}, settings stay at their defaults: {}", dataFolder, e.getMessage());
            builder = Configuration.builder();
        }
        return fromSystemProperties(builder, systemProperties, logger);
    }

    /**
     * Settings without any file: the defaults overridden only by the JVM system properties. Never touches the
     * file system.
     */
    public static ButterflySettings fromSystemProperties(Logger logger) {
        return fromSystemProperties(Configuration.builder(), butterflySystemProperties(), logger);
    }

    static ButterflySettings fromSystemProperties(Map<String, String> systemProperties, Logger logger) {
        return fromSystemProperties(Configuration.builder(), systemProperties, logger);
    }

    /** Applies the system properties after everything already in the builder, so they win. */
    private static ButterflySettings fromSystemProperties(Configuration.Builder builder, Map<String, String> systemProperties,
                                                          Logger logger) {
        systemProperties.forEach(builder::put);
        // the legacy property overrides a file value too, so existing launch scripts keep working
        String legacy = systemProperties.get(ButterflySettings.LEGACY_SORT_FORMAT_KEY);
        if (legacy != null && !systemProperties.containsKey(ButterflySettings.SORT_FORMAT_KEY)) {
            builder.put(ButterflySettings.SORT_FORMAT_KEY, legacy);
        }
        return ButterflySettings.from(builder.build(), logger);
    }

    private static void writeDefaultFileIfAbsent(Path dataFolder, Path file, String defaultResource) throws IOException {
        Files.createDirectories(dataFolder);
        if (Files.exists(file)) return;
        try (InputStream in = SettingsFile.class.getClassLoader().getResourceAsStream(defaultResource)) {
            if (in == null) throw new FileNotFoundException("Missing bundled resource " + defaultResource);
            Files.copy(in, file);
        }
    }

    private static Map<String, String> butterflySystemProperties() {
        Map<String, String> result = new HashMap<>();
        System.getProperties().forEach((key, value) -> {
            if (key instanceof String name && value instanceof String text && name.startsWith(SYSTEM_PROPERTY_PREFIX)) {
                result.put(name, text);
            }
        });
        return result;
    }
}
