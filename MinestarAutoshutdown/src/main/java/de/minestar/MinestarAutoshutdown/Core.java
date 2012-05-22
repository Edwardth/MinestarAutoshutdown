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

package de.minestar.MinestarAutoshutdown;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.regex.Pattern;

import de.minestar.MinestarAutoshutdown.utils.AutoshutdownTask;
import de.minestar.minestarlibrary.AbstractCore;
import de.minestar.minestarlibrary.config.MinestarConfig;
import de.minestar.minestarlibrary.utils.ConsoleUtils;

public class Core extends AbstractCore {

    public static final String NAME = "MinestarAutoshutdown";
    private MinestarConfig config;
    private AutoshutdownTask autoshutdownTask;
    private Timer timer;
    private final Pattern pattern = Pattern.compile("(^[01]?[0-9]:[0-5]?[0-9]$)|(^2[0-3]:[0-5]?[0-9]$)");

    @Override
    protected boolean createThreads() {
        config = loadConfig();
        if(!checkConfig()) {
            return false;
        }
        autoshutdownTask = new AutoshutdownTask(config.getInt("warning", 5));
        timer = new Timer();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat sdf;
        Calendar[] time = new Calendar[3];
        for (int i = 1; i <= 3; i++) {
            String tmp = config.getString("downtime" + i, "00:00");
            try {
                sdf = new SimpleDateFormat("hh:mm");
                sdf.parse(tmp);
                time[i - 1] = sdf.getCalendar();
                time[i - 1].set(Calendar.YEAR, now.get(Calendar.YEAR));
                time[i - 1].set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR));
                time[i - 1].add(Calendar.MINUTE, -config.getInt("warning", 5));
            } catch (ParseException e) {
                ConsoleUtils.printException(e, NAME, "Unable to parse Time");
            }
        }
        Arrays.sort(time);
        for (int i = 0; i < 3; i++) {
            if (time[i].after(Calendar.getInstance())) {
                timer.scheduleAtFixedRate(autoshutdownTask, time[i].getTime(), config.getInt("warning", 5) * 60000);
                time[i].add(Calendar.MINUTE, config.getInt("warning", 5));
                ConsoleUtils.printInfo(NAME, String.format("Shutdown at %s.", time[i].getTime()));
                return true;
            }
        }
        time[0].add(Calendar.DAY_OF_YEAR, 1);
        timer.scheduleAtFixedRate(autoshutdownTask, time[0].getTime(), config.getInt("warning", 5) * 60000);
        time[0].add(Calendar.MINUTE, config.getInt("warning", 5));
        return true;
    }

    public MinestarConfig loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            // copy default one from .jar
            if (!configFile.exists()) {
                ConsoleUtils.printWarning(NAME, "Can't find " + configFile + ", creating a default configuration");
                MinestarConfig.copyDefault(this.getClass().getResourceAsStream("/config.yml"), configFile);
            }
            // load config and check version tag
            return new MinestarConfig(configFile, NAME, getDescription().getVersion());
        } catch (Exception e) {
            ConsoleUtils.printException(e, NAME, "Can't load configuration file!");
            return null;
        }
    }

    public boolean checkConfig() {
        boolean errorFree = true;
        for (int i = 1; i <= 3; i++) {
            if (!pattern.matcher(config.getString("downtime" + i)).matches()) {
                ConsoleUtils.printWarning(NAME, "Incorect downtime" + i + "! Please edit the Config.");
                errorFree = false;
            }
        }
        return errorFree;
    }
}
