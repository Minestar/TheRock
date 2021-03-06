/*
 * Copyright (C) 2012 MineStar.de 
 * 
 * This file is part of TheRock.
 * 
 * TheRock is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * TheRock is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with TheRock.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.therock.listener;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import de.minestar.therock.TheRockCore;
import de.minestar.therock.data.BlockEventTypes;
import de.minestar.therock.data.sqlElements.BlockChangeElement;
import de.minestar.therock.manager.MainConsumer;
import de.minestar.therock.manager.MainManager;

public class BlockChangeListener implements Listener {

    private MainManager mainManager;
    private MainConsumer mainConsumer;
    // private StringBuilder queueBuilder;

    private static final Set<Integer> nonFluidProofBlocks = new HashSet<Integer>(Arrays.asList(6, 26, 27, 28, 31, 32, 37, 38, 39, 40, 50, 51, 55, 59, 66, 69, 70, 72, 75, 76, 78, 83, 93, 94, 104, 105, 106, 115, 127, 131, 132));
    private static final Set<Material> signBlocks = new HashSet<Material>(Arrays.asList(Material.SIGN_POST, Material.WALL_SIGN));

    private final BlockFace[] faces = new BlockFace[]{BlockFace.DOWN, BlockFace.NORTH, BlockFace.WEST, BlockFace.EAST, BlockFace.SOUTH};

    public BlockChangeListener() {
        this.mainManager = TheRockCore.mainManager;
        this.mainConsumer = TheRockCore.mainConsumer;
        // this.queueBuilder = new StringBuilder();
    }

    private void handleSignBreak(String reason, Block block, BlockEventTypes eventType) {
        // /////////////////////////////////
        // create data
        // /////////////////////////////////
        Sign sign = (Sign) block.getState();
        String signData = sign.getLine(0) + "`" + sign.getLine(1) + "`" + sign.getLine(2) + "`" + sign.getLine(3);
        this.addBlockChange(reason, eventType.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), Material.AIR.getId(), (byte) Material.AIR.getId(), signData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreak(BlockBreakEvent event) {
        // /////////////////////////////////
        // event cancelled => return
        // /////////////////////////////////
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBlockBreak())
            return;

        // /////////////////////////////////
        // create data
        // /////////////////////////////////
        if (!signBlocks.contains(event.getBlock().getType())) {
            this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_BREAK.getID(), event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getTypeId(), event.getBlock().getData(), Material.AIR.getId(), (byte) Material.AIR.getId());
        } else {
            this.handleSignBreak(event.getPlayer().getName(), event.getBlock(), BlockEventTypes.PLAYER_BREAK);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlace(BlockPlaceEvent event) {
        // /////////////////////////////////
        // event cancelled => return
        // /////////////////////////////////
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBlockPlace())
            return;

        // /////////////////////////////////
        // create data : all, except signs
        // /////////////////////////////////
        if (event.getBlock().getType() != Material.WALL_SIGN && event.getBlock().getType() != Material.SIGN_POST) {
            this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_PLACE.getID(), event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlockReplacedState().getTypeId(), event.getBlockReplacedState().getRawData(), event.getBlockPlaced().getTypeId(), event.getBlockPlaced().getData());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockExplode(EntityExplodeEvent event) {
        // /////////////////////////////////
        // event cancelled => return
        // /////////////////////////////////
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getEntity().getWorld().getName()) || !this.mainManager.getWorld(event.getEntity().getWorld()).logEntityBlockExplode())
            return;

        for (Block block : event.blockList()) {
            // /////////////////////////////////
            // create data
            // /////////////////////////////////
            if (!signBlocks.contains(block.getType())) {
                this.addBlockChange(event.getEntityType().name(), BlockEventTypes.PHYSICS_DESTROY.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getTypeId(), block.getData(), Material.AIR.getId(), (byte) Material.AIR.getId());
            } else {
                this.handleSignBreak(event.getEntityType().name(), block, BlockEventTypes.PHYSICS_DESTROY);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        // /////////////////////////////////
        // event cancelled => return
        // /////////////////////////////////
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getEntity().getWorld().getName()) || !this.mainManager.getWorld(event.getEntity().getWorld()).logEntityBlockChange())
            return;

        // /////////////////////////////////
        // create data
        // /////////////////////////////////
        if (!signBlocks.contains(event.getBlock().getType())) {
            this.addBlockChange(event.getEntityType().name(), BlockEventTypes.PHYSICS_DESTROY.getID(), event.getBlock().getWorld().getName(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ(), event.getBlock().getTypeId(), event.getBlock().getData(), event.getTo().getId(), (byte) 0);
        } else {
            this.handleSignBreak(event.getEntityType().name(), event.getBlock(), BlockEventTypes.PHYSICS_DESTROY);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockFromTo(BlockFromToEvent event) {
        // /////////////////////////////////
        // event cancelled => return
        // /////////////////////////////////
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld().getName()))
            return;

        final Block toBlock = event.getToBlock();
        final Material fromType = event.getBlock().getType();
        final Material toType = toBlock.getState().getType();
        final byte replacedData = toBlock.getState().getRawData();
        final int newID = event.getBlock().getTypeId();
        final byte newData = (byte) (event.getBlock().getData() + 1);

        final boolean canFlow = (toType == Material.AIR || nonFluidProofBlocks.contains(toType.getId()));
        
        if (!canFlow) {
            return;
        }

        if (this.mainManager.getWorld(event.getBlock()).logLavaFlow() && (fromType == Material.LAVA || fromType == Material.STATIONARY_LAVA)) {
            // /////////////////////////////////
            // create data : lavaflow & blockcreation
            // /////////////////////////////////
            if (toType != Material.AIR) {
                // DONE!
                this.addBlockChange("Lavaflow", BlockEventTypes.PHYSICS_DESTROY.getID(), toBlock.getWorld().getName(), toBlock.getX(), toBlock.getY(), toBlock.getZ(), toType.getId(), replacedData, newID, newData);
            }

            for (BlockFace blockFace : faces) {
                // TODO: fix here
                final Block lower = toBlock.getRelative(blockFace);
                if (lower.getType() == Material.WATER || lower.getType() == Material.STATIONARY_WATER) {
                    if (lower.getData() <= 2) {
                        this.addBlockChange("Lavaflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), lower.getX(), lower.getY(), lower.getZ(), lower.getTypeId(), lower.getData(), Material.STONE.getId(), (byte) 0);
                    } else {
                        this.addBlockChange("Lavaflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), lower.getX(), lower.getY(), lower.getZ(), lower.getTypeId(), lower.getData(), Material.COBBLESTONE.getId(), (byte) 0);
                    }
                }
            }
        } else if (this.mainManager.getWorld(event.getBlock()).logWaterFlow() && (fromType == Material.WATER || fromType == Material.STATIONARY_WATER)) {
            // /////////////////////////////////
            // create data : waterflow & blockcreation
            // /////////////////////////////////
            if (toType != Material.AIR) {
                // DONE!
                this.addBlockChange("Waterflow", BlockEventTypes.PHYSICS_DESTROY.getID(), toBlock.getWorld().getName(), toBlock.getX(), toBlock.getY(), toBlock.getZ(), toType.getId(), replacedData, 8, newData);
            }

            for (BlockFace blockFace : faces) {
                // TODO: fix here
                final Block relative = toBlock.getRelative(blockFace);
                if (relative.getType() == Material.LAVA || relative.getType() == Material.STATIONARY_LAVA) {
                    if (relative.getData() == 0) {
                        this.addBlockChange("Waterflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), relative.getX(), relative.getY(), relative.getZ(), relative.getTypeId(), relative.getData(), Material.OBSIDIAN.getId(), (byte) 0);
                    } else {
                        if (relative.getData() < 6) {
                            this.addBlockChange("Waterflow", BlockEventTypes.PHYSICS_CREATE.getID(), toBlock.getWorld().getName(), relative.getX(), relative.getY(), relative.getZ(), relative.getTypeId(), relative.getData(), Material.COBBLESTONE.getId(), (byte) 0);
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        // event cancelled => return
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlockClicked().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBucketEmpty())
            return;

        // /////////////////////////////////
        // create data
        // /////////////////////////////////
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_PLACE.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getState().getTypeId(), block.getState().getRawData(), (event.getBucket() == Material.WATER_BUCKET ? 9 : 11), (byte) 0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        // event cancelled => return
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlockClicked().getWorld().getName()) || !this.mainManager.getWorld(event.getPlayer()).logBucketFill())
            return;

        // /////////////////////////////////
        // create data
        // /////////////////////////////////
        Block block = event.getBlockClicked().getRelative(event.getBlockFace());
        this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_BREAK.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getState().getTypeId(), block.getState().getRawData(), Material.AIR.getId(), (byte) 0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onSignChange(SignChangeEvent event) {
        // event cancelled => return
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld()) || !this.mainManager.getWorld(event.getPlayer()).logBlockPlace())
            return;

        // /////////////////////////////////
        // create data
        // /////////////////////////////////
        Block block = event.getBlock();
        String signData = event.getLine(0) + "`" + event.getLine(1) + "`" + event.getLine(2) + "`" + event.getLine(3);
        this.addBlockChange(event.getPlayer().getName(), BlockEventTypes.PLAYER_PLACE.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), 0, (byte) 0, block.getTypeId(), block.getData(), signData);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        // event cancelled => return
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld()))
            return;

        // /////////////////////////////////
        // create data
        // /////////////////////////////////
        if (event.isSticky()) {
            if (this.mainManager.getWorld(event.getBlock()).logPistonSticky()) {
                Block block = event.getRetractLocation().getBlock();
                this.addBlockChange("STICKY_PISTON", BlockEventTypes.PISTON_REPLACE.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getState().getTypeId(), block.getState().getRawData(), Material.AIR.getId(), (byte) 0);
                Block extension = event.getRetractLocation().getBlock().getRelative(event.getDirection().getOppositeFace());
                this.addBlockChange("STICKY_PISTON", BlockEventTypes.PISTON_REPLACE.getID(), extension.getWorld().getName(), extension.getX(), extension.getY(), extension.getZ(), extension.getState().getTypeId(), extension.getState().getRawData(), block.getState().getTypeId(), block.getState().getRawData());
            }
        } else {
            if (this.mainManager.getWorld(event.getBlock()).logPistonNormal()) {
                Block block = event.getRetractLocation().getBlock().getRelative(event.getDirection().getOppositeFace());
                this.addBlockChange("PISTON", BlockEventTypes.PISTON_REPLACE.getID(), block.getWorld().getName(), block.getX(), block.getY(), block.getZ(), block.getState().getTypeId(), block.getState().getRawData(), Material.AIR.getId(), (byte) 0);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        // event cancelled => return
        if (event.isCancelled() || !this.mainManager.isWorldWatched(event.getBlock().getWorld()))
            return;

        // is it a sticky piston?
        if (event.isSticky()) {
            // do we log sticky pistons?
            if (!this.mainManager.getWorld(event.getBlock()).logPistonSticky()) {
                return;
            }

            Block pushedBlock;
            for (Block block : event.getBlocks()) {
                // /////////////////////////////////
                // create data
                // /////////////////////////////////
                pushedBlock = block.getRelative(event.getDirection());
                this.addBlockChange("STICKY_PISTON", BlockEventTypes.PISTON_REPLACE.getID(), pushedBlock.getWorld().getName(), pushedBlock.getX(), pushedBlock.getY(), pushedBlock.getZ(), pushedBlock.getState().getTypeId(), pushedBlock.getState().getRawData(), block.getState().getTypeId(), block.getState().getRawData());
            }

            // create data for PistonExtension
            pushedBlock = event.getBlock().getRelative(event.getDirection());
            this.addBlockChange("STICKY_PISTON", BlockEventTypes.PISTON_REPLACE.getID(), pushedBlock.getWorld().getName(), pushedBlock.getX(), pushedBlock.getY(), pushedBlock.getZ(), pushedBlock.getState().getTypeId(), pushedBlock.getState().getRawData(), Material.PISTON_EXTENSION.getId(), event.getBlock().getState().getRawData());
            return;
        } else {
            // do we log normal pistons?
            if (!this.mainManager.getWorld(event.getBlock()).logPistonNormal()) {
                return;
            }

            Block pushedBlock;
            for (Block block : event.getBlocks()) {
                // /////////////////////////////////
                // create data
                // /////////////////////////////////
                pushedBlock = block.getRelative(event.getDirection());
                this.addBlockChange("PISTON", BlockEventTypes.PISTON_REPLACE.getID(), pushedBlock.getWorld().getName(), pushedBlock.getX(), pushedBlock.getY(), pushedBlock.getZ(), pushedBlock.getState().getTypeId(), pushedBlock.getState().getRawData(), block.getState().getTypeId(), block.getState().getRawData());
            }

            // create data for PistonExtension
            pushedBlock = event.getBlock().getRelative(event.getDirection());
            this.addBlockChange("PISTON", BlockEventTypes.PISTON_REPLACE.getID(), pushedBlock.getWorld().getName(), pushedBlock.getX(), pushedBlock.getY(), pushedBlock.getZ(), pushedBlock.getState().getTypeId(), pushedBlock.getState().getRawData(), Material.PISTON_EXTENSION.getId(), event.getBlock().getState().getRawData());
            return;
        }
    }

    private void addBlockChange(String reason, int eventType, String worldName, int blockX, int blockY, int blockZ, int fromID, byte fromData, int toID, byte toData) {
        this.addBlockChange(reason, eventType, worldName, blockX, blockY, blockZ, fromID, fromData, toID, toData, "");
    }

    private void addBlockChange(String reason, int eventType, String worldName, int blockX, int blockY, int blockZ, int fromID, byte fromData, int toID, byte toData, String extraData) {
        if (reason != null) {
            this.mainConsumer.appendBlockEvent(worldName, new BlockChangeElement(reason, eventType, worldName, blockX, blockY, blockZ, fromID, fromData, toID, toData, extraData));
        }
    }
}
