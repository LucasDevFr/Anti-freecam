package me.antifreecam.config;

import me.antifreecam.AntiFreecamMod;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Simple flat-file config for AntiFrecam.
 *
 * antifreecam.properties is created in the server's ./config/ directory on
 * first run with sensible defaults. Edit it and run /antifreecam reload.
 *
 * Options:
 *   enabled            – master switch (default: true)
 *   sectionRadius      – how many 16-tall chunk sections above AND below the
 *                        player's own section to reveal (default: 2 → ±32 blocks)
 *   surfaceThreshold   – Y level above which we consider a player "on the surface"
 *                        and send them full chunk data (default: 62)
 *   maskBlock          – the block ID to fill hidden sections with (default: stone)
 */
public class AntiFrecamConfig {

    private static final Path CONFIG_PATH = Paths.get("config", "antifreecam.properties");

    public boolean enabled          = true;
    public int     engineMode    = 1;
    /** Number of chunk sections (16 blocks each) revealed above and below the player. */
    public int     sectionRadius    = 2;
    /** Y level at or above which players get full unmasked chunk data. */
    public int     surfaceThreshold = 62;
    /** Block used to fill masked sections (must be a valid block ID, e.g. "stone"). */
    public String  maskBlock        = "air";

    private AntiFrecamConfig() {}

    public static AntiFrecamConfig load() {
        AntiFrecamConfig cfg = new AntiFrecamConfig();

        try {
            if (!Files.exists(CONFIG_PATH)) {
                Files.createDirectories(CONFIG_PATH.getParent());
                cfg.save();          // write defaults
                return cfg;
            }

            Properties props = new Properties();
            try (InputStream in = Files.newInputStream(CONFIG_PATH)) {
                props.load(in);
            }

            cfg.enabled           = Boolean.parseBoolean(props.getProperty("enabled",           "true"));
            cfg.engineMode        = Integer.parseInt(    props.getProperty("engineMode",     "1"));
            cfg.sectionRadius     = Integer.parseInt(    props.getProperty("sectionRadius",     "2"));
            cfg.surfaceThreshold  = Integer.parseInt(    props.getProperty("surfaceThreshold",  "62"));
            cfg.maskBlock         =                      props.getProperty("maskBlock",          "air");

        } catch (Exception e) {
            AntiFreecamMod.LOGGER.error("[AntiFreecam] Failed to load config, using defaults", e);
        }

        return cfg;
    }

    public void save() {
        try {
            Properties props = new Properties();
            props.setProperty("enabled",          String.valueOf(enabled));
            props.setProperty("engineMode",       String.valueOf(engineMode));
            props.setProperty("sectionRadius",    String.valueOf(sectionRadius));
            props.setProperty("surfaceThreshold", String.valueOf(surfaceThreshold));
            props.setProperty("maskBlock",        maskBlock);

            try (OutputStream out = Files.newOutputStream(CONFIG_PATH)) {
                props.store(out,
                    "AntiFrecam configuration\n" +
                    "# enabled           - master on/off switch\n" +
                    "# engineMode        - Switches how the masked chunks are hidden\n" +
                    "#                     1 = fill with maskblock, 2 = copy current chunk\n" +
                    "# sectionRadius     - chunk sections (16 blocks each) revealed above/below player\n" +
                    "#                     2 = ±32 blocks,  3 = ±48 blocks\n" +
                    "# surfaceThreshold  - Y at/above which players get full data (surface players)\n" +
                    "# maskBlock         - block used to fill hidden sections (vanilla block id)");
            }
        } catch (Exception e) {
            AntiFreecamMod.LOGGER.error("[AntiFreecam] Failed to save config", e);
        }
    }
}
