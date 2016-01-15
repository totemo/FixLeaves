FixLeaves
=========

WorldPainter'd custom trees can lead to non-decaying leaves in the world.  This plugin fixes that by clearing the no-decay bit of LEAVES and LEAVES_2 blocks to 0.  The check-decay bit is also cleared.

Leaves fixed in this manner will stay undecayed until a block update, even if they are not in range of a log.  Therefore, custom trees that have leaves outside of the range where a log would prevent decay will remain undamaged.

The plugin fixes leaves in a square centred on (0,0) defined by its side length in blocks.  The side length is converted into chunks, then rounded up to the nearest odd number to give the affected area.  One chunk is converted per `period` ticks, where `period` is specified by `/fixleaves period <num>`.

Player-placed leaves will be set to decayable, but again, won't decay until a block update.  In any case, this plugin was written to fix a problem early in the life of a new map, when the number of WorldPainter'd leaves vastly outnumbers the number of player-placed ones.

To fix player placed leaves, you can redo the players leaf edits within a selection, e.g. using LogBlock:
```
/lb redo player playername created block leaves leaves_2 sel
```


Commands
--------

 * `/fixleaves help` - Show usage help.
 * `/fixleaves reload` - Reload the configuration.
 * `/fixleaves notify` - Toggle notification broadcasts.
 * `/fixleaves debug` - Toggle debug logging.
 * `/fixleaves start` - Start the conversion.
 * `/fixleaves stop` - Stop the conversion.
 * `/fixleaves status` - Show current running state, side and index.
 * `/fixleaves period [<num>]` - Set or show period in ticks.
 * `/fixleaves side [<num>]` - Set or show side in blocks.
 * `/fixleaves index [<num>]` - Set or show next converted index.
 * `/fixleaves min_y [<num>]` - Set or show minimum converted Y coordinate.


Permissions
-----------

 * `fixleaves.admin` - permission to administer the plugin.
 * `fixleaves.notify` - tag the player to receive progress updates every 10 seconds.

