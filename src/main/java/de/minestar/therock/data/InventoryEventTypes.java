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

package de.minestar.therock.data;

public enum InventoryEventTypes {
    PLAYER_TOOK(0), PLAYER_PLACED(1),

    UNKNOWN(-1);

    private final int eventID;

    private InventoryEventTypes(int ID) {
        this.eventID = ID;
    }

    public int getID() {
        return this.eventID;
    }

    public static InventoryEventTypes byID(int ID) {
        for (InventoryEventTypes type : InventoryEventTypes.values()) {
            if (type.getID() == ID)
                return type;
        }
        return UNKNOWN;
    }
}
