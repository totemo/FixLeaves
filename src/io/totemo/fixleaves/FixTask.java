package io.totemo.fixleaves;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

// ----------------------------------------------------------------------------
/**
 * This task fixes the leaves in one chunk at a time until the specified square
 * centred on the origin of the map is done.
 */
public class FixTask implements Runnable {
    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param plugin the owning plugin.
     */
    public FixTask(FixLeaves plugin) {
        _plugin = plugin;
    }

    // ------------------------------------------------------------------------
    /**
     * Specify whether this task should do anything to chunks.
     *
     * @param running if true, leaves are fixed.
     */
    public void setRunning(boolean running) {
        _running = running;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if leaves are being fixed by this task.
     */
    public boolean isRunning() {
        return _running;
    }

    // ------------------------------------------------------------------------
    /**
     * Return true if all chunks have been fixed.
     *
     * @return true if all chunks have been fixed.
     */
    public boolean isComplete() {
        return FixLeaves.CONFIG.INDEX >= FixLeaves.CONFIG.SIDE * FixLeaves.CONFIG.SIDE;
    }

    // ------------------------------------------------------------------------
    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        ++_ticks;

        if (_running) {
            if (isComplete()) {
                statusUpdate();
                _running = false;
            } else {
                if (_ticks % FixLeaves.CONFIG.PERIOD == 0) {
                    fixChunk();
                }

                // 10 second status update.
                if (_ticks % 200 == 0) {
                    statusUpdate();
                }
            }
        }
    } // run

    // ------------------------------------------------------------------------
    /**
     * Return the odd side dimension in chunks of the fully complete area
     * corresponding to the specified index.
     *
     * @return the odd side dimension in chunks of the fully complete area
     *         corresponding to the specified index.
     */
    public static int getSideOf(int index) {
        int side = (int) Math.sqrt(index);
        return (side & 1) == 0 ? side - 1 : side;
    }

    // ------------------------------------------------------------------------
    /**
     * Fix leaves in the chunk corresponding to the current index.
     */
    protected void fixChunk() {
        if (FixLeaves.CONFIG.INDEX < 0 || FixLeaves.CONFIG.SIDE < 1) {
            _plugin.getLogger().severe("Index or side value is wonky. Giving up.");
            Bukkit.getServer().broadcast(
                ChatColor.GREEN + "FixLeaves: Index or side value is wonky. Giving up.",
                "fixleaves.notify");
            setRunning(false);
            return;
        }

        if (FixLeaves.CONFIG.INDEX == 0) {
            fixChunk(0, 0);
        } else {
            int completedSide = getSideOf(FixLeaves.CONFIG.INDEX);
            int newSide = completedSide + 2;

            // X/Z coordinate offset of NW corner of SIDE x SIDE square.
            int coordOffset = -newSide / 2;

            int relativeIndex = FixLeaves.CONFIG.INDEX - completedSide * completedSide;
            if (relativeIndex < newSide) {
                // North row of chunks running west to east.
                fixChunk(coordOffset + relativeIndex, coordOffset);
            } else if (relativeIndex < newSide + 2 * (newSide - 2)) {
                // West or east sides, running north to south.
                boolean west = ((relativeIndex - newSide) & 1) == 0;
                int row = 1 + (relativeIndex - newSide) / 2;
                if (west) {
                    // West column..
                    fixChunk(coordOffset, coordOffset + row);
                } else {
                    // East column.
                    fixChunk(coordOffset + newSide - 1, coordOffset + row);
                }
            } else {
                // South row of chunks running west to east.
                int column = relativeIndex - (newSide + 2 * (newSide - 2));
                fixChunk(coordOffset + column, coordOffset + newSide - 1);

            }
        }
        ++FixLeaves.CONFIG.INDEX;
    } // fixChunk

    // ------------------------------------------------------------------------
    /**
     * Fix the chunk with chunk coordinates (x,z) in the configured World.
     *
     * @param chunkX the chunk X coordinate.
     * @param chunkZ the chunk Z coordinate.
     */
    @SuppressWarnings("deprecation")
    protected void fixChunk(int chunkX, int chunkZ) {
        long now = System.currentTimeMillis();

        World world = Bukkit.getWorld(FixLeaves.CONFIG.WORLD);
        Chunk chunk = world.getChunkAt(chunkX, chunkZ);
        if (chunk.load(false)) {
            for (int xB = 0; xB < 16; ++xB) {
                for (int zB = 0; zB < 16; ++zB) {
                    for (int yB = FixLeaves.CONFIG.MIN_Y; yB < 256; ++yB) {
                        Block block = chunk.getBlock(xB, yB, zB);
                        if (block.getType() == Material.LEAVES || block.getType() == Material.LEAVES_2) {
                            block.setData((byte) (block.getData() & 3));
                        }
                    }
                }
            }
        } else {
            _plugin.getLogger().severe(String.format("Chunk %d at (%d, %d) could not be loaded.",
                FixLeaves.CONFIG.INDEX, chunkX, chunkZ));
        }
        if (FixLeaves.CONFIG.DEBUG) {
            _plugin.getLogger().info(String.format("Fixed index %d chunk (%d, %d) in %d ms.",
                FixLeaves.CONFIG.INDEX, chunkX, chunkZ, System.currentTimeMillis() - now));
        }
    }

    // ------------------------------------------------------------------------
    /**
     * Broadcast a progress update notification to players with the
     * fixleaves.notify permission.
     */
    protected void statusUpdate() {
        if (FixLeaves.CONFIG.NOTIFY) {
            int side = getSideOf(FixLeaves.CONFIG.INDEX);
            int blocks = side * 16;
            String message = String.format("&aFixLeaves:%s index %d, %d x %d chunks, %d x %d blocks.",
                (isComplete() ? " FINISHED" : ""), FixLeaves.CONFIG.INDEX, side, side, blocks, blocks);
            Bukkit.getServer().broadcast(ChatColor.translateAlternateColorCodes('&', message), "fixleaves.notify");
        }
    }

    // ------------------------------------------------------------------------

    /**
     * The owning plugin.
     */
    protected FixLeaves _plugin;

    /**
     * Number of ticks this task has run.
     */
    protected int _ticks;

    /**
     * True if the task is fixing chunks.
     *
     * Defaults to false (not running) on plugin initialisation.
     */
    protected boolean _running;

} // class FixTask