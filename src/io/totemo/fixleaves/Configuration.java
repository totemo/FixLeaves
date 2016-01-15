package io.totemo.fixleaves;

import org.bukkit.plugin.Plugin;

// ----------------------------------------------------------------------------
/**
 * Configuration wrapper.
 */
public class Configuration {
    /**
     * True if debug messages hit the logs.
     */
    public boolean DEBUG;

    /**
     * If true, players with the fixleaves.notify permission will receive
     * broadcasts about FixLeaves progress every 10s.
     */
    public boolean NOTIFY;

    /**
     * Period in ticks between chunk conversions.
     */
    public int PERIOD;

    /**
     * Current index (position) of the next chunk to fix.
     *
     * Chunk (0,0) is index 0. Surrounding immediate neighbours are 1 through 8,
     * numbered left to right, then top top bottom. The SE diagonal (0,0),
     * (1,1), (2,2) etc is index (N^2 - 1).
     */
    public int INDEX;

    /**
     * Length of the side of the square to fix, in chunks.
     */
    public int SIDE;

    /**
     * Lowest Y level to process.
     */
    public int MIN_Y;

    /**
     * Name of world to fix.
     */
    public String WORLD;

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     */
    public Configuration(Plugin plugin) {
        _plugin = plugin;
    }

    // ------------------------------------------------------------------------
    /**
     * Load the plugin configuration.
     */
    public void reload() {
        _plugin.reloadConfig();
        DEBUG = _plugin.getConfig().getBoolean("debug");
        NOTIFY = _plugin.getConfig().getBoolean("notify");
        PERIOD = _plugin.getConfig().getInt("period");
        INDEX = _plugin.getConfig().getInt("index");
        SIDE = _plugin.getConfig().getInt("side");
        MIN_Y = _plugin.getConfig().getInt("min_y");
        WORLD = _plugin.getConfig().getString("world");
    }

    // ------------------------------------------------------------------------
    /**
     * Save the configuration to disk.
     */
    public void save() {
        _plugin.getConfig().set("debug", DEBUG);
        _plugin.getConfig().set("notify", NOTIFY);
        _plugin.getConfig().set("period", PERIOD);
        _plugin.getConfig().set("index", INDEX);
        _plugin.getConfig().set("side", SIDE);
        _plugin.getConfig().set("min_y", MIN_Y);
        _plugin.saveConfig();
    }

    // ------------------------------------------------------------------------
    /**
     * Owning plugin.
     */
    private final Plugin _plugin;

} // class Configuration