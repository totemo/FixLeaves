package io.totemo.fixleaves;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

// ----------------------------------------------------------------------------
/**
 * Fix leaves by clearing the no-decay (2^2) and check-decay (2^3) bits of leaf
 * data values.
 *
 * Leaves may be far enough away from logs that they ought to decay, but they
 * will not do so until they receive a block update.
 *
 * Player-placed leaves will be affected. Fix those with:
 *
 * <pre>
 * /lb redo player playername created block leaves leaves_2 sel
 * </pre>
 */
public class FixLeaves extends JavaPlugin {
    /**
     * Configuration instance.
     */
    public static Configuration CONFIG;

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender,
     *      org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("fixleaves")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
                // Let Bukkit show usage help from plugin.yml.
                return false;
            }

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                CONFIG.reload();
                sender.sendMessage(ChatColor.GREEN + "FixLeaves configuration reloaded.");
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("notify")) {
                CONFIG.NOTIFY = !CONFIG.NOTIFY;
                sender.sendMessage(ChatColor.GREEN + "FixLeaves notifications " + (CONFIG.NOTIFY ? "ENABLED." : "DISABLED."));
                CONFIG.save();
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("debug")) {
                CONFIG.DEBUG = !CONFIG.DEBUG;
                sender.sendMessage(ChatColor.GREEN + "FixLeaves debug logging " + (CONFIG.DEBUG ? "ENABLED." : "DISABLED."));
                CONFIG.save();
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
                start(sender);
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("stop")) {
                stop(sender);
                return true;
            }
            if (args.length == 1 && args[0].equalsIgnoreCase("status")) {
                String message = String.format("FixLeaves: %s, completed side dimension %d of %d, index %d.",
                    (_task.isRunning() ? "RUNNING" : "STOPPED"),
                    Math.max(0, FixTask.getSideOf(CONFIG.INDEX)), CONFIG.SIDE, CONFIG.INDEX);
                sender.sendMessage(ChatColor.GREEN + message);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("period")) {
                setPeriod(sender, args);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("index")) {
                setIndex(sender, args);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("side")) {
                setSide(sender, args);
                return true;
            }
            if (args.length >= 1 && args.length <= 2 && args[0].equalsIgnoreCase("min_y")) {
                setMinY(sender, args);
                return true;
            }
        }

        sender.sendMessage(ChatColor.RED + "Invalid command syntax.");
        return false;
    }// onCommand

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        CONFIG = new Configuration(this);
        CONFIG.reload();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, _task, 1, 1);
    }

    // ------------------------------------------------------------------------
    /**
     * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
     */
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        CONFIG.save();
    }

    // ------------------------------------------------------------------------
    /**
     * Handler /fixleaves start.
     *
     * @param sender the CommandSender.
     */
    protected void start(CommandSender sender) {
        if (_task.isRunning()) {
            sender.sendMessage(ChatColor.GREEN + "FixLeaves is already running.");
        } else {
            _task.setRunning(true);
            sender.sendMessage(ChatColor.GREEN + "FixLeaves STARTED at index " + CONFIG.INDEX + ".");
        }
        CONFIG.save();
    }

    // ------------------------------------------------------------------------
    /**
     * Handler /fixleaves stop.
     *
     * @param sender the CommandSender.
     */
    protected void stop(CommandSender sender) {
        if (!_task.isRunning()) {
            sender.sendMessage(ChatColor.GREEN + "FixLeaves is already stopped.");
        } else {
            _task.setRunning(false);
            sender.sendMessage(ChatColor.GREEN + "FixLeaves STOPPED at index " + CONFIG.INDEX + ".");
        }
        CONFIG.save();
    }

    // ------------------------------------------------------------------------
    /**
     * Handler /fixleaves period [<ticks>].
     *
     * @param sender the CommandSender.
     */
    protected void setPeriod(CommandSender sender, String[] args) {
        boolean changed = false;
        if (args.length == 2) {
            try {
                int newPeriod = Integer.parseInt(args[1]);
                if (newPeriod > 0) {
                    CONFIG.PERIOD = newPeriod;
                    changed = true;
                }
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be a positive integer.");
            }
        }
        sender.sendMessage(ChatColor.GREEN + "FixLeaves: the period is " +
                           (changed ? "now " : "") + CONFIG.PERIOD + " tick(s).");
        if (changed) {
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handler /fixleaves side [<blocks>].
     *
     * @param sender the CommandSender.
     */
    protected void setSide(CommandSender sender, String[] args) {
        boolean changed = false;
        if (args.length == 2) {
            try {
                int newSideBlocks = Integer.parseInt(args[1]);
                if (newSideBlocks > 0) {
                    // Convert blocks to chunks rounded up and forced odd.
                    int newSideChunks = (newSideBlocks + 15) / 16;
                    if ((newSideChunks & 1) == 0) {
                        ++newSideChunks;
                    }

                    CONFIG.SIDE = newSideChunks;
                    changed = true;
                }
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be a positive integer.");
            }
        }
        sender.sendMessage(ChatColor.GREEN +
                           String.format("FixLeaves: the side is %s%d chunk(s), %d blocks.",
                               (changed ? "now " : ""), CONFIG.SIDE, CONFIG.SIDE * 16));
        if (changed) {
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handler /fixleaves index [<blocks>].
     *
     * @param sender the CommandSender.
     */
    protected void setIndex(CommandSender sender, String[] args) {
        boolean changed = false;
        if (args.length == 2) {
            try {
                int newIndex = Integer.parseInt(args[1]);
                if (newIndex >= 0) {
                    CONFIG.INDEX = newIndex;
                    changed = true;
                }
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be a non-negative integer.");
            }
        }
        sender.sendMessage(ChatColor.GREEN + "FixLeaves: the index is " +
                           (changed ? "now " : "") + CONFIG.INDEX + ".");
        if (changed) {
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Handler /fixleaves min_y [<blocks>].
     *
     * @param sender the CommandSender.
     */
    protected void setMinY(CommandSender sender, String[] args) {
        boolean changed = false;
        if (args.length == 2) {
            try {
                int newMinY = Integer.parseInt(args[1]);
                if (newMinY >= 0 && newMinY <= 255) {
                    CONFIG.MIN_Y = newMinY;
                    changed = true;
                }
            } catch (NumberFormatException ex) {
            }
            if (!changed) {
                sender.sendMessage(ChatColor.RED + "The argument must be a valid Y coordinate.");
            }
        }
        sender.sendMessage(ChatColor.GREEN + "FixLeaves: the minimum Y coordinate is " +
                           (changed ? "now " : "") + CONFIG.MIN_Y + ".");
        if (changed) {
            CONFIG.save();
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Task to fix leaves. Runs every tick, irrespective of configured PERIOD.
     */
    protected FixTask _task = new FixTask(this);

} // class FixLeaves