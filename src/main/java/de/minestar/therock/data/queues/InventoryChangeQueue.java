/*
 * Copyright (C) 2015 MineStar.de 
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

package de.minestar.therock.data.queues;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import de.minestar.therock.TheRockCore;
import de.minestar.therock.data.KeyHelper;
import de.minestar.therock.data.Value;
import de.minestar.therock.data.ValueList;
import de.minestar.therock.data.sqlElements.AbstractSQLElement;
import de.minestar.therock.data.sqlElements.InventoryChangeElement;

public class InventoryChangeQueue extends AbstractSQLUpdateQueue {
    private final String worldName;

    public InventoryChangeQueue(String worldName, int maxQueueSize) {
        super(maxQueueSize);
        this.worldName = worldName;
    }

    @Override
    protected void createTable() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("CREATE TABLE IF NOT EXISTS `");
        queryBuilder.append(this.worldName + "_inventory");
        queryBuilder.append("` (`ID` INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,");

        // APPEND VALUES
        ValueList values = new ValueList();
        values.addValue(new Value("timestamp", "BIGINT"));
        values.addValue(new Value("reason", "VARCHAR(255)"));
        values.addValue(new Value("eventType", "INTEGER"));
        values.addValue(new Value("blockX", "INTEGER"));
        values.addValue(new Value("blockY", "INTEGER"));
        values.addValue(new Value("blockZ", "INTEGER"));
        values.addValue(new Value("TypeID", "INTEGER"));
        values.addValue(new Value("Data", "INTEGER"));
        values.addValue(new Value("Amount", "INTEGER"));

        int i = 0;
        for (Value value : values.getValues()) {
            queryBuilder.append(value.getName());
            queryBuilder.append(value.getSqlTypeDefinition());
            queryBuilder.append(" NOT NULL");
            ++i;
            if (i != values.getSize()) {
                queryBuilder.append(", ");
            }
        }

        // APPEND KEYS
        ArrayList<ValueList> keyList = KeyHelper.getBlockAndTimeKey();
        for (ValueList list : keyList) {
            if (list.getSize() > 0) {
                queryBuilder.append(", KEY ");

                // append keyname
                queryBuilder.append(list.getName());
                queryBuilder.append(" (");
                i = 0;
                for (Value key : list.getValues()) {
                    queryBuilder.append(key.getName());
                    ++i;
                    if (i != list.getSize()) {
                        queryBuilder.append(", ");
                    }
                }
                queryBuilder.append(")");
            }
        }
        queryBuilder.append(") ENGINE=MYISAM DEFAULT CHARSET=utf8;");
        try {
            PreparedStatement statement = TheRockCore.databaseHandler.getConnection().prepareStatement(queryBuilder.toString());
            TheRockCore.databaseHandler.executeUpdateWithoutThread(statement);
        } catch (SQLException e) {
            e.printStackTrace();

        }
    }

    @Override
    protected PreparedStatement buildPreparedStatement() {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("INSERT INTO ");
        queryBuilder.append(this.worldName + "_inventory");
        queryBuilder.append(" ( timestamp, reason, eventType, blockX, blockY, blockZ, TypeID, Data, Amount ) VALUES");

        for (int index = 0; index < this.list.size(); index++) {
            queryBuilder.append(" ( ?, ?, ?, ?, ?, ?, ?, ?, ? )");
            if (index < this.list.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        PreparedStatement statement = null;
        try {
            statement = TheRockCore.databaseHandler.getConnection().prepareStatement(queryBuilder.toString());
            int currentIndex = 0;
            for (AbstractSQLElement abstractElement : this.list) {
                InventoryChangeElement element = (InventoryChangeElement) abstractElement;
                statement.setLong(1 + (currentIndex * 9), element.getTimestamp());
                statement.setString(2 + (currentIndex * 9), element.getReason());
                statement.setInt(3 + (currentIndex * 9), element.getEventType());
                statement.setInt(4 + (currentIndex * 9), element.getBlockX());
                statement.setInt(5 + (currentIndex * 9), element.getBlockY());
                statement.setInt(6 + (currentIndex * 9), element.getBlockZ());
                statement.setInt(7 + (currentIndex * 9), element.getID());
                statement.setInt(8 + (currentIndex * 9), element.getData());
                statement.setInt(9 + (currentIndex * 9), element.getAmount());
                currentIndex++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            statement = null;
        }
        return statement;
    }
}
