/*
 * Copyright (C) 2012 MineStar.de 
 * 
 * This file is part of MinestarAutoshutdown.
 * 
 * MinestarAutoshutdown is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 * 
 * MinestarAutoshutdown is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MinestarAutoshutdown.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.minestar.MinestarAutoshutdown.utils;

import java.util.TimerTask;

import org.bukkit.Bukkit;

public class AutoshutdownTask extends TimerTask {
    private boolean warned = false;
    private int delay;

    public AutoshutdownTask(int delay) {
        this.delay = delay;
    }

    @Override
    public void run() {
        if (warned) {
            Bukkit.broadcastMessage("Server faehrt herrunter!");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "save-all");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop");
        } else {
            Bukkit.broadcastMessage(String.format("Server wird in %d Minuten herrunter fahren", delay));
            warned = true;
        }
    }
}