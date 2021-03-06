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

package de.minestar.therock.tools;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import de.minestar.minestarlibrary.utils.PlayerUtils;
import de.minestar.therock.TheRockCore;

public class UndoLastChangeTool extends Tool {

    public UndoLastChangeTool(String toolName, Material toolType, String permission) {
        super(toolName, toolType, permission);
    }

    @Override
    public boolean onBlockInteract(Player player, Block block, BlockFace blockFace, boolean isLeftClick) {
        if (this.hasPermission(player)) {
            PlayerUtils.sendInfo(player, TheRockCore.NAME, "Resetting last changes...");
            if (isLeftClick) {
                TheRockCore.databaseHandler.getUndoLastBlockChanges(player, block);
            } else {
                TheRockCore.databaseHandler.getUndoLastBlockChanges(player, block.getRelative(blockFace));
            }
            return true;
        }
        return false;
    }
}
